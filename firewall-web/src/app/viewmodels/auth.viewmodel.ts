import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { ApiConfigService } from '../core/api-config.service';
import {
  ChangePasswordRequest,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  RegisterRequest,
  Session,
  UsuarioResponse,
} from '../models';

const SESSION_KEY = 'fw_session';

@Injectable({ providedIn: 'root' })
export class AuthViewModel {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiConfigService);
  private readonly router = inject(Router);

  readonly session = signal<Session | null>(this.readSession());
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly isAuthenticated = computed(() => !!this.session());

  constructor() {
    this.session.set(this.readSession());
  }

  clearMessages(): void {
    this.error.set(null);
  }

  login(body: LoginRequest): void {
    this.loading.set(true);
    this.clearMessages();
    this.http.post<LoginResponse>(`${this.api.usuariosBase}/login`, body).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.authenticated) {
          const s: Session = { rut: res.rut, nombre: res.nombre ?? '' };
          this.persist(s);
          void this.router.navigate(['/app/inicio']);
        } else {
          this.error.set(res.mensaje ?? 'No autorizado');
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.resolveHttpError(err, 'No se pudo conectar con el servicio de usuarios'));
      },
    });
  }

  register(body: RegisterRequest): void {
    this.loading.set(true);
    this.clearMessages();
    this.http.post<UsuarioResponse>(`${this.api.usuariosBase}/register`, body).subscribe({
      next: () => {
        this.loading.set(false);
        this.login({ rut: body.rut, password: body.password });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message ?? 'Error al registrar');
      },
    });
  }

  forgotPassword(email: string, onSuccess?: (message: string) => void): void {
    this.loading.set(true);
    this.clearMessages();
    const params = new HttpParams().set('email', email);
    this.http.post<MessageResponse>(this.api.forgotPasswordUrl(), null, { params }).subscribe({
      next: (res) => {
        this.loading.set(false);
        onSuccess?.(res.message ?? 'Revise su bandeja de entrada.');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.resolveHttpError(err, 'No se pudo procesar la recuperación de contraseña'));
      },
    });
  }

  private resolveHttpError(err: { status?: number; error?: unknown; message?: string }, fallback: string): string {
    const body = err.error;
    if (body && typeof body === 'object' && 'message' in body && typeof (body as { message: unknown }).message === 'string') {
      return (body as { message: string }).message;
    }
    if (typeof body === 'string' && !body.trimStart().startsWith('<')) {
      return body;
    }
    if (err.status === 0) {
      return 'Sin conexión al servidor. Verifique que Docker y el API Gateway (:8080) estén activos.';
    }
    if (err.status === 404) {
      return 'Servicio no encontrado. Reconstruya ms-usuarios: docker compose up -d --build ms-usuarios api-gateway';
    }
    if (err.status === 503) {
      return 'Correo no configurado en el servidor (MAIL_USERNAME / MAIL_APP_PASSWORD).';
    }
    return fallback;
  }

  changePassword(body: ChangePasswordRequest): Observable<string> {
    this.loading.set(true);
    this.clearMessages();
    return this.http.put<MessageResponse>(`${this.api.usuariosBase}/change-password`, body).pipe(
      tap({
        next: () => this.loading.set(false),
        error: () => this.loading.set(false),
      }),
      map((res) => res.message ?? 'Contraseña actualizada'),
      catchError((err) => {
        const msg = err.error?.message ?? 'No se pudo cambiar la contraseña';
        return throwError(() => msg);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.session.set(null);
    void this.router.navigate(['/']);
  }

  private persist(s: Session): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(s));
    this.session.set(s);
  }

  private readSession(): Session | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) {
        return null;
      }
      return JSON.parse(raw) as Session;
    } catch {
      return null;
    }
  }
}
