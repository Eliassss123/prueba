import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, of, tap } from 'rxjs';
import { ApiConfigService } from '../core/api-config.service';
import { CreateReporteRequest, ReporteResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ReportesViewModel {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiConfigService);

  readonly list = signal<ReporteResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly lastCreated = signal<ReporteResponse | null>(null);

  /** GET lista de reportes (mismo base path que enviar). */
  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<ReporteResponse[]>(this.api.reportesListUrl())
      .pipe(
        tap((rows) => {
          this.list.set(rows ?? []);
          this.loading.set(false);
        }),
        catchError((err) => {
          this.loading.set(false);
          this.error.set(err.error?.message ?? 'Error al cargar reportes');
          this.list.set([]);
          return of([] as ReporteResponse[]);
        }),
      )
      .subscribe();
  }

  enviar(payload: CreateReporteRequest): Observable<ReporteResponse> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.post<ReporteResponse>(`${this.api.reportesBase}/enviar`, payload).pipe(
      tap({
        next: (r) => {
          this.lastCreated.set(r);
          this.loading.set(false);
          this.refresh();
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(err.error?.message ?? 'No se pudo enviar el reporte');
        },
      }),
    );
  }
}
