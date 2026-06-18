import { Injectable, signal } from '@angular/core';

export type OperatorSection = 'mapa' | 'reporte';

@Injectable({ providedIn: 'root' })
export class LayoutViewModel {
  readonly sidebarOpen = signal(true);
  readonly activeSection = signal<OperatorSection>('mapa');

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  setSidebarOpen(isOpen: boolean): void {
    this.sidebarOpen.set(isOpen);
  }

  openSidebar(): void {
    this.sidebarOpen.set(true);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  setSection(s: OperatorSection): void {
    this.activeSection.set(s);
  }
}
