import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AdminViewModel } from '../../viewmodels/admin.viewmodel';

@Component({
  selector: 'fw-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, RouterLink],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminComponent {
  readonly vm = inject(AdminViewModel);

  readonly filtroTexto = signal('');
  readonly filtroEstado = signal('todos');

  readonly filteredReportes = computed(() => {
    const query = this.filtroTexto().trim().toLowerCase();
    const estado = this.filtroEstado().trim().toLowerCase();
    return this.vm.recentReportes().filter((r) => {
      const okEstado = estado === 'todos' || (r.estado ?? '').trim().toLowerCase() === estado;
      if (!okEstado) {
        return false;
      }
      if (!query) {
        return true;
      }
      const id = String(r.id);
      const rut = (r.usuario?.rut ?? '').toLowerCase();
      const descripcion = (r.descripcion ?? '').toLowerCase();
      return id.includes(query) || rut.includes(query) || descripcion.includes(query);
    });
  });

  constructor() {
    this.vm.refresh();
  }

  refresh(): void {
    this.vm.refresh();
  }
}
