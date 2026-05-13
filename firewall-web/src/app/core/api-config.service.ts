import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

/**
 * Resuelve URLs según `environment`: gateway (`/api/reports`, `/api/geolocation`) o servicios directos.
 * Alineado con `ApiGatewayApplication` del monorepo.
 */
@Injectable({ providedIn: 'root' })
export class ApiConfigService {
  private root(): string {
    return (environment.apiRoot ?? '').replace(/\/$/, '');
  }

  /** Base `/api/usuarios` */
  readonly usuariosBase = this.resolveUsuarios();
  /** Base reportes: gateway `/api/reports` o directo `/api/reportes` */
  readonly reportesBase = this.resolveReportes();
  /** Base monitoreo: gateway `/api/geolocation` o directo `/api/monitoreo` */
  readonly geoBase = this.resolveGeo();

  private resolveUsuarios(): string {
    if (environment.useDirectServices) {
      return `${environment.direct.usuarios.replace(/\/$/, '')}/api/usuarios`;
    }
    return `${this.root()}/api/usuarios`;
  }

  private resolveReportes(): string {
    if (environment.useDirectServices) {
      return `${environment.direct.reportes.replace(/\/$/, '')}/api/reportes`;
    }
    return `${this.root()}/api/reports`;
  }

  private resolveGeo(): string {
    if (environment.useDirectServices) {
      return `${environment.direct.geo.replace(/\/$/, '')}/api/monitoreo`;
    }
    return `${this.root()}/api/geolocation`;
  }

  /**
   * Lista reportes (`GET` mismo prefijo que `POST .../enviar`).
   * En gateway: rutas exactas `/api/reports` y wildcard `/api/reports/**` (ver `ApiGatewayApplication`).
   */
  reportesListUrl(): string {
    return this.reportesBase;
  }
}
