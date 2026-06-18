import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiConfigService } from '../core/api-config.service';
import { ReporteResponse } from '../models';

interface GeoMonitoreoResponse {
  id: number;
}

type ModuleStatus = 'ok' | 'error';

export interface AdminKpi {
  label: string;
  value: string;
  detail: string;
  icon: string;
}

@Injectable({ providedIn: 'root' })
export class AdminViewModel {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiConfigService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly lastUpdated = signal<Date | null>(null);

  readonly reportes = signal<ReporteResponse[]>([]);
  readonly geoRows = signal<GeoMonitoreoResponse[]>([]);

  readonly reportesStatus = signal<ModuleStatus>('error');
  readonly geolocalizacionStatus = signal<ModuleStatus>('error');

  readonly totalReportes = computed(() => this.reportes().length);
  readonly reportesHoy = computed(() => {
    const today = new Date();
    const y = today.getFullYear();
    const m = today.getMonth();
    const d = today.getDate();
    return this.reportes().filter((r) => {
      const date = new Date(r.fechaHora);
      return (
        Number.isFinite(date.getTime()) &&
        date.getFullYear() === y &&
        date.getMonth() === m &&
        date.getDate() === d
      );
    }).length;
  });
  readonly usuariosConReportes = computed(() => {
    const unique = new Set(
      this.reportes()
        .map((r) => (r.usuario?.rut ?? '').trim())
        .filter((rut) => rut.length > 0),
    );
    return unique.size;
  });
  readonly reportesConUbicacion = computed(
    () =>
      this.reportes().filter(
        (r) => Number.isFinite(Number(r.ubicacion?.latitud)) && Number.isFinite(Number(r.ubicacion?.longitud)),
      ).length,
  );
  readonly reportesAbiertos = computed(
    () =>
      this.reportes().filter((r) => {
        const estado = (r.estado ?? '').trim().toLowerCase();
        return estado !== 'cerrado' && estado !== 'resuelto' && estado !== 'finalizado';
      }).length,
  );
  readonly totalMonitoreo = computed(() => this.geoRows().length);

  readonly estadosResumen = computed(() => {
    const counter = new Map<string, number>();
    for (const row of this.reportes()) {
      const key = (row.estado ?? 'Sin estado').trim() || 'Sin estado';
      counter.set(key, (counter.get(key) ?? 0) + 1);
    }
    return Array.from(counter.entries())
      .map(([estado, cantidad]) => ({ estado, cantidad }))
      .sort((a, b) => b.cantidad - a.cantidad);
  });

  readonly kpis = computed<AdminKpi[]>(() => [
    {
      label: 'Total reportes',
      value: String(this.totalReportes()),
      detail: `${this.reportesHoy()} reportes hoy`,
      icon: 'assignment',
    },
    {
      label: 'Reportes abiertos',
      value: String(this.reportesAbiertos()),
      detail: 'Estados no cerrados',
      icon: 'crisis_alert',
    },
    {
      label: 'Usuarios con reportes',
      value: String(this.usuariosConReportes()),
      detail: 'RUT únicos detectados',
      icon: 'group',
    },
    {
      label: 'Cobertura georreferenciada',
      value: `${this.reportesConUbicacion()}/${this.totalReportes() || 0}`,
      detail: `${this.totalMonitoreo()} registros en monitoreo`,
      icon: 'map',
    },
  ]);

  readonly recentReportes = computed(() =>
    [...this.reportes()]
      .sort((a, b) => this.safeDateValue(b.fechaHora) - this.safeDateValue(a.fechaHora))
      .slice(0, 12),
  );

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);

    const reportesRequest = this.http.get<ReporteResponse[]>(this.api.reportesListUrl()).pipe(catchError(() => of(null)));
    const geoRequest = this.http.get<GeoMonitoreoResponse[]>(this.api.geoBase).pipe(catchError(() => of(null)));

    forkJoin({
      reportes: reportesRequest,
      geolocalizacion: geoRequest,
    }).subscribe({
      next: ({ reportes, geolocalizacion }) => {
        const reportesOk = Array.isArray(reportes);
        const geoOk = Array.isArray(geolocalizacion);

        this.reportes.set(reportesOk ? reportes : []);
        this.geoRows.set(geoOk ? geolocalizacion : []);
        this.reportesStatus.set(reportesOk ? 'ok' : 'error');
        this.geolocalizacionStatus.set(geoOk ? 'ok' : 'error');

        this.lastUpdated.set(new Date());
        if (!reportesOk && !geoOk) {
          this.error.set('No se pudo conectar a ms-reportes ni ms-geolocalización.');
        } else if (!reportesOk) {
          this.error.set('No se pudo cargar la fuente de reportes.');
        } else if (!geoOk) {
          this.error.set('No se pudo cargar la fuente de geolocalización.');
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar datos del panel de administración.');
      },
    });
  }

  severityClass(estado: string | null | undefined): string {
    const normalized = (estado ?? '').trim().toLowerCase();
    if (!normalized) {
      return 'fw-admin-pill-neutral';
    }
    if (['critico', 'crítico', 'urgente', 'emergencia', 'activo', 'abierto'].includes(normalized)) {
      return 'fw-admin-pill-high';
    }
    if (['en revision', 'en revisión', 'pendiente', 'nuevo'].includes(normalized)) {
      return 'fw-admin-pill-medium';
    }
    if (['resuelto', 'cerrado', 'finalizado'].includes(normalized)) {
      return 'fw-admin-pill-low';
    }
    return 'fw-admin-pill-neutral';
  }

  private safeDateValue(input: string | null | undefined): number {
    const value = new Date(input ?? '').getTime();
    return Number.isFinite(value) ? value : 0;
  }
}
