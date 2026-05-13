import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiConfigService } from '../core/api-config.service';
import { LoginRequest, LoginResponse, RegisterRequest, Session, UsuarioResponse } from '../models';

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

  login(body: LoginRequest): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.post<LoginResponse>(`${this.api.usuariosBase}/login`, body).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.authenticated) {
          const s: Session = { rut: res.rut, nombre: res.nombre ?? '' };
          this.persist(s);
          void this.router.navigate(['/app/mapa']);
        } else {
          this.error.set(res.mensaje ?? 'No autorizado');
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message ?? 'No se pudo conectar con el servicio de usuarios');
      },
    });
  }

  register(body: RegisterRequest): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.post<UsuarioResponse>(`${this.api.usuariosBase}/register`, body).subscribe({
      next: () => {
        this.loading.set(false);
        this.error.set(null);
        this.login({ rut: body.rut, password: body.password });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message ?? 'Error al registrar');
      },
    });
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
