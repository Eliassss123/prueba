import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'fw-placeholder-panel',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="bg-white rounded-xl shadow-md p-8 max-w-lg text-center mx-auto">
      <mat-icon class="!w-12 !h-12 !text-[3rem] text-orange-500 mb-4">{{ icon }}</mat-icon>
      <h1 class="text-xl font-display font-bold text-slate-900">{{ title }}</h1>
      <p class="text-slate-600 mt-2 text-sm">{{ description }}</p>
      <p class="text-xs text-slate-400 mt-4">Módulo en preparación para la siguiente iteración.</p>
    </div>
  `,
})
export class PlaceholderPanelComponent {
  private readonly route = inject(ActivatedRoute);
  readonly title = this.route.snapshot.data['title'] as string;
  readonly description = this.route.snapshot.data['description'] as string;
  readonly icon = (this.route.snapshot.data['icon'] as string) ?? 'construction';
}
