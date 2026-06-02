import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { formatRutInput, normalizeRut } from '../../core/rut.util';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';

@Component({
  selector: 'fw-landing',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  readonly auth = inject(AuthViewModel);

  readonly activeTab = signal<'login' | 'register'>('login');
  readonly showForgotPassword = signal(false);
  readonly recoveryMessage = signal<string | null>(null);

  rut = '';
  password = '';
  regRut = '';
  regEmail = '';
  regNombre = '';
  regPassword = '';
  forgotEmail = '';

  onRutInput(value: string, target: 'login' | 'register'): void {
    const formatted = formatRutInput(value);
    if (target === 'login') {
      this.rut = formatted;
    } else {
      this.regRut = formatted;
    }
  }

  openForgotPassword(): void {
    this.auth.clearMessages();
    this.recoveryMessage.set(null);
    this.showForgotPassword.set(true);
  }

  closeForgotPassword(): void {
    this.showForgotPassword.set(false);
    this.forgotEmail = '';
    this.recoveryMessage.set(null);
    this.auth.clearMessages();
  }

  submitLogin(): void {
    this.auth.login({ rut: normalizeRut(this.rut), password: this.password });
  }

  submitRegister(): void {
    this.auth.register({
      rut: normalizeRut(this.regRut),
      email: this.regEmail.trim(),
      password: this.regPassword,
      nombre: this.regNombre.trim() || undefined,
    });
  }

  submitForgotPassword(): void {
    const email = this.forgotEmail.trim();
    this.recoveryMessage.set(null);
    this.auth.forgotPassword(email, (message) => this.recoveryMessage.set(message));
  }
}
