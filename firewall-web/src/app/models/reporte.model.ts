/** Contrato con DTOs de ms-reportes. */
export interface UsuarioReporteRequest {
  rut: string;
}

export interface UbicacionReporteRequest {
  latitud: number;
  longitud: number;
  direccionReferencial?: string;
}

export interface MultimediaReporteRequest {
  urlS3: string;
  tipoArchivo: string;
}

export interface CreateReporteRequest {
  usuario: UsuarioReporteRequest;
  descripcion: string;
  ubicacion: UbicacionReporteRequest;
  multimedia?: MultimediaReporteRequest[];
}

export interface UbicacionReporteResponse {
  latitud: number;
  longitud: number;
  direccionReferencial?: string;
}

export interface UsuarioReporteResponse {
  rut?: string;
  id?: number;
}

export interface MultimediaReporteResponse {
  id: number;
  urlS3: string;
  tipoArchivo: string;
}

export interface ReporteResponse {
  id: number;
  usuario: UsuarioReporteResponse;
  descripcion: string;
  fechaHora: string;
  estado: string;
  ubicacion: UbicacionReporteResponse;
  multimedia?: MultimediaReporteResponse[];
}
