import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';

@Component({
  selector: 'fw-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent {
  readonly auth = inject(AuthViewModel);

  readonly showPasswordForm = signal(false);
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordMessage = signal<string | null>(null);
  passwordError = signal<string | null>(null);

  togglePasswordForm(): void {
    this.showPasswordForm.update((v) => !v);
    this.passwordMessage.set(null);
    this.passwordError.set(null);
  }

  submitChangePassword(): void {
    const rut = this.auth.session()?.rut;
    if (!rut) {
      return;
    }
    this.passwordMessage.set(null);
    this.passwordError.set(null);
    this.auth
      .changePassword({
        rut,
        currentPassword: this.currentPassword,
        newPassword: this.newPassword,
        confirmNewPassword: this.confirmPassword,
      })
      .subscribe({
        next: (msg) => {
          this.passwordMessage.set(msg);
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmPassword = '';
        },
        error: (err) => {
          this.passwordError.set(err);
        },
      });
  }
}
