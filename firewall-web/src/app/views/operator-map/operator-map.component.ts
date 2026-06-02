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
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import * as L from 'leaflet';
import { ReportesViewModel } from '../../viewmodels/reportes.viewmodel';

// Rutas de iconos servidas desde `angular.json` (copia de node_modules/leaflet/dist/images).
delete (L.Icon.Default.prototype as L.Icon.Default & { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: 'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png',
});

const MAP_TILE_URL = 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
const MAP_TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';

@Component({
  selector: 'fw-operator-map',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './operator-map.component.html',
  styleUrl: './operator-map.component.scss',
})
export class OperatorMapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapHost', { static: true }) mapHost!: ElementRef<HTMLDivElement>;

  readonly vm = inject(ReportesViewModel);

  private map?: L.Map;
  private layerGroup?: L.LayerGroup;
  private resizeObserver?: ResizeObserver;

  constructor() {
    effect(() => {
      this.vm.list();
      queueMicrotask(() => this.syncMarkers());
    });
  }

  ngAfterViewInit(): void {
    this.vm.refresh();
    requestAnimationFrame(() => {
      this.initMap();
      this.bindMapResize();
      queueMicrotask(() => this.syncMarkers());
    });
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.resizeObserver = undefined;
    this.map?.remove();
    this.map = undefined;
  }

  refresh(): void {
    this.vm.refresh();
    queueMicrotask(() => this.syncMarkers());
  }

  private initMap(): void {
    if (this.map) {
      return;
    }
    const el = this.mapHost.nativeElement;
    this.map = L.map(el, {
      zoomControl: true,
      attributionControl: true,
    }).setView([-33.45, -70.67], 11);

    L.tileLayer(MAP_TILE_URL, {
      subdomains: 'abcd',
      maxZoom: 20,
      attribution: MAP_TILE_ATTRIBUTION,
    }).addTo(this.map);

    this.layerGroup = L.layerGroup().addTo(this.map);
    this.invalidateMapSize();
  }

  private bindMapResize(): void {
    const el = this.mapHost?.nativeElement;
    if (!el || typeof ResizeObserver === 'undefined') {
      return;
    }
    this.resizeObserver = new ResizeObserver(() => this.invalidateMapSize());
    this.resizeObserver.observe(el);
  }

  private invalidateMapSize(): void {
    if (!this.map) {
      return;
    }
    this.map.invalidateSize({ animate: false });
    setTimeout(() => this.map?.invalidateSize({ animate: false }), 150);
    setTimeout(() => this.map?.invalidateSize({ animate: false }), 500);
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
        `<strong>#${r.id}</strong><br/>${this.escape(r.descripcion)}<br/><span style="color:#71717a;font-size:12px">${this.escape(
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
