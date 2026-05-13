import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  effect,
  inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import * as L from 'leaflet';
import { ReportesViewModel } from '../../viewmodels/reportes.viewmodel';

@Component({
  selector: 'fw-operator-map',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './operator-map.component.html',
  styleUrl: './operator-map.component.scss',
})
export class OperatorMapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapHost', { static: true }) mapHost!: ElementRef<HTMLDivElement>;

  readonly vm = inject(ReportesViewModel);

  private map?: L.Map;
  private layerGroup?: L.LayerGroup;

  constructor() {
    effect(() => {
      this.vm.list();
      queueMicrotask(() => this.syncMarkers());
    });
  }

  ngAfterViewInit(): void {
    this.vm.refresh();
    this.initMap();
    queueMicrotask(() => this.syncMarkers());
  }

  ngOnDestroy(): void {
    this.map?.remove();
    this.map = undefined;
  }

  refresh(): void {
    this.vm.refresh();
    queueMicrotask(() => this.syncMarkers());
  }

  private initMap(): void {
    const el = this.mapHost.nativeElement;
    this.map = L.map(el, {
      zoomControl: true,
      attributionControl: true,
    }).setView([-33.45, -70.67], 11);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap &copy; CARTO',
    }).addTo(this.map);

    this.layerGroup = L.layerGroup().addTo(this.map);
    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  private syncMarkers(): void {
    if (!this.map || !this.layerGroup) {
      return;
    }
    this.layerGroup.clearLayers();
    const rows = this.vm.list();
    const pts: L.LatLngExpression[] = [];
    for (const r of rows) {
      const lat = Number(r.ubicacion?.latitud);
      const lng = Number(r.ubicacion?.longitud);
      if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        continue;
      }
      pts.push([lat, lng]);
      const icon = L.divIcon({
        className: 'fw-leaflet-pin',
        html: `<div class="fw-map-pin-pulse" title="${this.escapeAttr(r.descripcion)}"></div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14],
      });
      const m = L.marker([lat, lng], { icon });
      m.bindPopup(
        `<strong>#${r.id}</strong><br/>${this.escape(r.descripcion)}<br/><span class="text-muted">${this.escape(
          r.estado,
        )}</span>`,
      );
      m.addTo(this.layerGroup);
    }
    if (pts.length) {
      this.map.fitBounds(L.latLngBounds(pts), { padding: [40, 40], maxZoom: 14 });
    }
  }

  private escapeAttr(s: string): string {
    return this.escape(s).replace(/"/g, '&quot;');
  }

  private escape(s: string): string {
    return (s ?? '').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
}
