import { Injectable, signal } from '@angular/core';

export type OperatorSection = 'mapa' | 'reporte';

@Injectable({ providedIn: 'root' })
export class LayoutViewModel {
  readonly sidebarOpen = signal(true);
  readonly activeSection = signal<OperatorSection>('mapa');

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  setSection(s: OperatorSection): void {
    this.activeSection.set(s);
  }
}
