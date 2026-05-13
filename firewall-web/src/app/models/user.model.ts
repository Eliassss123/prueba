/** Contrato con `LoginRequestDTO` / `RegisterRequestDTO` (ms-usuarios). */
export interface LoginRequest {
  rut: string;
  password: string;
}

export interface RegisterRequest {
  rut: string;
  email: string;
  password: string;
  nombre?: string;
}

/** Contrato con `LoginResponseDTO`. */
export interface LoginResponse {
  rut: string;
  nombre: string;
  mensaje: string;
  authenticated: boolean;
}

export interface UsuarioResponse {
  id: number;
  rut: string;
  email: string;
  nombre: string;
}

export interface Session {
  rut: string;
  nombre: string;
}
