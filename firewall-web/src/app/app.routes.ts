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
      { path: '', pathMatch: 'full', redirectTo: 'mapa' },
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
    ],
  },
  { path: '**', redirectTo: '' },
];
