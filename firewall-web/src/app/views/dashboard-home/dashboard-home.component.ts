import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';

@Component({
  selector: 'fw-dashboard-home',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './dashboard-home.component.html',
})
export class DashboardHomeComponent {
  readonly auth = inject(AuthViewModel);
}
