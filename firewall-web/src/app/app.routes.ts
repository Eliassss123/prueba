import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./views/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'app',
    loadComponent: () => import('./views/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        loadComponent: () =>
          import('./views/dashboard-home/dashboard-home.component').then((m) => m.DashboardHomeComponent),
      },
      {
        path: 'mapa',
        loadComponent: () =>
          import('./views/operator-map/operator-map.component').then((m) => m.OperatorMapComponent),
      },
      {
        path: 'reporte',
        loadComponent: () =>
          import('./views/reporte-ciudadano/reporte-ciudadano.component').then((m) => m.ReporteCiudadanoComponent),
      },
      {
        path: 'suministros',
        loadComponent: () =>
          import('./views/placeholder-panel/placeholder-panel.component').then((m) => m.PlaceholderPanelComponent),
        data: {
          title: 'Monitoreo de Suministros Médicos',
          description: 'Visualización de stock, pedidos urgentes y centros de distribución.',
          icon: 'medical_services',
        },
      },
      {
        path: 'alertas',
        loadComponent: () =>
          import('./views/placeholder-panel/placeholder-panel.component').then((m) => m.PlaceholderPanelComponent),
        data: {
          title: 'Configuración de Alertas Críticas',
          description: 'Defina reglas, severidad y destinatarios de notificaciones automáticas.',
          icon: 'notifications_active',
        },
      },
      {
        path: 'perfil',
        loadComponent: () => import('./views/profile/profile.component').then((m) => m.ProfileComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
