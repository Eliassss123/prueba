import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateReporteRequest } from '../../models';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';
import { GeolocationViewModel } from '../../viewmodels/geolocation.viewmodel';
import { ReportesViewModel } from '../../viewmodels/reportes.viewmodel';

@Component({
  selector: 'fw-reporte-ciudadano',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
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

  ngAfterViewInit(): void {
    queueMicrotask(() => this.solicitarUbicacion(false));
  }

  /**
   * @param showSnackOnSuccess mensajes solo si el usuario pulsó el botón (evita snack molesto al auto-inicio).
   */
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
        const msg = this.geo.error();
        if (msg) {
          this.snack.open(msg, 'OK', { duration: 7000 });
        }
      },
    });
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
      this.snack.open('Indica una ubicación válida (usa “Obtener mi ubicación” o escribe lat/lng).', 'OK', {
        duration: 5000,
      });
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
          urlS3: `https://placehold.co/800x600/1c1917/f97316?text=Firewall+simulado`,
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
