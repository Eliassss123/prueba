import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

export type GeoStatus = 'idle' | 'pending' | 'ready' | 'error';

export interface GeoFix {
  lat: number;
  lng: number;
  accuracyM: number;
}

@Injectable({ providedIn: 'root' })
export class GeolocationViewModel {
  private readonly platformId = inject(PLATFORM_ID);

  readonly status = signal<GeoStatus>('idle');
  readonly error = signal<string | null>(null);
  readonly lastFix = signal<GeoFix | null>(null);

  /**
   * Solicita una posición al navegador (dispara el diálogo de permisos si aún no se concedió).
   * `enableHighAccuracy` pide GPS cuando el dispositivo lo permite.
   */
  requestCurrentPosition(): Observable<GeoFix> {
    this.error.set(null);

    if (!isPlatformBrowser(this.platformId) || typeof navigator === 'undefined' || !navigator.geolocation) {
      this.status.set('error');
      const msg = 'Este entorno no permite geolocalización en el navegador.';
      this.error.set(msg);
      return new Observable((sub) => sub.error(new Error(msg)));
    }

    this.status.set('pending');

    return new Observable<GeoFix>((sub) => {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const fix: GeoFix = {
            lat: pos.coords.latitude,
            lng: pos.coords.longitude,
            accuracyM: pos.coords.accuracy,
          };
          this.lastFix.set(fix);
          this.status.set('ready');
          sub.next(fix);
          sub.complete();
        },
        (err: GeolocationPositionError) => {
          const msg = this.mapError(err);
          this.status.set('error');
          this.error.set(msg);
          sub.error(err);
        },
        {
          enableHighAccuracy: false,
          timeout: 8_000,
          maximumAge: 120_000,
        },
      );
    });
  }

  private mapError(err: GeolocationPositionError): string {
    switch (err.code) {
      case err.PERMISSION_DENIED:
        return 'Permiso de ubicación denegado. Actívalo en la barra del navegador o en ajustes del sitio.';
      case err.POSITION_UNAVAILABLE:
        return 'No se pudo determinar la posición (GPS no disponible o señal débil).';
      case err.TIMEOUT:
        return 'Tiempo de espera agotado al obtener la ubicación. Prueba en exterior o revisa el GPS.';
      default:
        return err.message || 'Error al obtener la ubicación.';
    }
  }
}
