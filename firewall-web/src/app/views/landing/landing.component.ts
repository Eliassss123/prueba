import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';

@Component({
  selector: 'fw-landing',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTabsModule,
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  readonly auth = inject(AuthViewModel);

  rut = '';
  password = '';
  regRut = '';
  regEmail = '';
  regNombre = '';
  regPassword = '';

  readonly tilt = signal({ x: 0, y: 0 });
  readonly sparkSeed = signal(0);

  constructor() {
    this.sparkSeed.set(Math.floor(Math.random() * 360));
  }

  @HostListener('mousemove', ['$event'])
  onMove(ev: MouseEvent): void {
    const cx = window.innerWidth / 2;
    const cy = window.innerHeight / 2;
    const x = ((ev.clientX - cx) / cx) * 6;
    const y = ((ev.clientY - cy) / cy) * 6;
    this.tilt.set({ x: y, y: -x });
  }

  submitLogin(): void {
    this.auth.login({ rut: this.rut.trim(), password: this.password });
  }

  submitRegister(): void {
    this.auth.register({
      rut: this.regRut.trim(),
      email: this.regEmail.trim(),
      password: this.regPassword,
      nombre: this.regNombre.trim() || undefined,
    });
  }
}
