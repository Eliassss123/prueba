import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateReporteRequest } from '../../models';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';
import { GeolocationViewModel } from '../../viewmodels/geolocation.viewmodel';
import { ReportesViewModel } from '../../viewmodels/reportes.viewmodel';

@Component({
  selector: 'fw-reporte-ciudadano',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatSnackBarModule, MatProgressSpinnerModule],
  templateUrl: './reporte-ciudadano.component.html',
  styleUrl: './reporte-ciudadano.component.scss',
})
export class ReporteCiudadanoComponent implements AfterViewInit {
  readonly auth = inject(AuthViewModel);
  readonly vm = inject(ReportesViewModel);
  readonly geo = inject(GeolocationViewModel);
  private readonly snack = inject(MatSnackBar);

  descripcion = '';
  latitud = '';
  longitud = '';
  referencia = '';
  archivoNombre = '';

  private readonly fallbackFix = {
    lat: -33.45,
    lng: -70.67,
    accuracyM: 0,
  };

  ngAfterViewInit(): void {
    queueMicrotask(() => this.solicitarUbicacion(false));
  }

  solicitarUbicacion(showSnackOnSuccess: boolean): void {
    this.geo.requestCurrentPosition().subscribe({
      next: (fix) => {
        this.latitud = fix.lat.toFixed(7);
        this.longitud = fix.lng.toFixed(7);
        if (!this.referencia.trim()) {
          this.referencia = `Ubicación del dispositivo (precisión ~${Math.round(fix.accuracyM)} m)`;
        }
        if (showSnackOnSuccess) {
          this.snack.open('Ubicación GPS aplicada al reporte', 'OK', { duration: 3000 });
        }
      },
      error: () => {
        this.aplicarUbicacionDemo();
        const msg = this.geo.error();
        if (msg) {
          this.snack.open(`${msg} Se aplico una ubicacion de ejemplo.`, 'OK', { duration: 7000 });
        }
      },
    });
  }

  aplicarUbicacionDemo(): void {
    this.latitud = this.fallbackFix.lat.toFixed(7);
    this.longitud = this.fallbackFix.lng.toFixed(7);
    if (!this.referencia.trim()) {
      this.referencia = 'Ubicacion de ejemplo para pruebas';
    }
  }

  onFile(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input.files?.[0];
    this.archivoNombre = f?.name ?? '';
  }

  enviar(): void {
    const rut = this.auth.session()?.rut?.trim();
    if (!rut) {
      this.snack.open('Sesión sin RUT', 'OK', { duration: 3000 });
      return;
    }
    const lat = Number(this.latitud.replace(',', '.'));
    const lng = Number(this.longitud.replace(',', '.'));
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      this.snack.open('Indica una ubicación válida (usa GPS o escribe lat/lng).', 'OK', { duration: 5000 });
      return;
    }
    const body: CreateReporteRequest = {
      usuario: { rut },
      descripcion: this.descripcion.trim(),
      ubicacion: {
        latitud: lat,
        longitud: lng,
        direccionReferencial: this.referencia.trim() || undefined,
      },
    };
    if (this.archivoNombre) {
      body.multimedia = [
        {
          urlS3: `https://placehold.co/800x600/f8fafc/ea580c?text=Firewall`,
          tipoArchivo: 'image/jpeg',
        },
      ];
    }
    this.vm.enviar(body).subscribe({
      next: (r) => {
        this.snack.open(`Reporte #${r.id} registrado`, 'OK', { duration: 4000 });
        this.descripcion = '';
        this.archivoNombre = '';
      },
      error: () => {
        const msg = this.vm.error();
        if (msg) {
          this.snack.open(msg, 'Cerrar', { duration: 6000 });
        }
      },
    });
  }
}
