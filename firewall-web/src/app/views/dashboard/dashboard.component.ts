import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthViewModel } from '../../viewmodels/auth.viewmodel';
import { LayoutViewModel } from '../../viewmodels/layout.viewmodel';

const PAGE_TITLES: Record<string, string> = {
  '/app/inicio': 'Inicio',
  '/app/mapa': 'Reportes y emergencias',
  '/app/suministros': 'Recursos utiles',
  '/app/alertas': 'Alertas críticas',
  '/app/perfil': 'Mi perfil',
  '/app/reporte': 'Nuevo reporte',
};

@Component({
  selector: 'fw-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  readonly auth = inject(AuthViewModel);
  readonly layout = inject(LayoutViewModel);
  private readonly router = inject(Router);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly pageTitle = computed(() => {
    const path = this.url().split('?')[0];
    return PAGE_TITLES[path] ?? 'Panel operador';
  });

  userInitials(): string {
    const name = this.auth.session()?.nombre?.trim();
    if (!name) {
      return 'OP';
    }
    const parts = name.split(/\s+/).filter(Boolean);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }
}
