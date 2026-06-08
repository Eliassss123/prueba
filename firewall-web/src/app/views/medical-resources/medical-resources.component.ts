import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';

delete (L.Icon.Default.prototype as L.Icon.Default & { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: 'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png',
});

type ResourceType = 'pharmacy' | 'supermarket';

interface ResourcePoint {
  id: number;
  type: ResourceType;
  name: string;
  lat: number;
  lng: number;
}

const MAP_TILE_URL = 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
const MAP_TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';
const DEFAULT_CENTER: [number, number] = [-33.45, -70.67];

const SAMPLE_RESOURCES: ResourcePoint[] = [
  { id: 1, type: 'pharmacy', name: 'Farmacia Central', lat: -33.4429, lng: -70.6539 },
  { id: 2, type: 'pharmacy', name: 'Farmacia Salud Norte', lat: -33.4354, lng: -70.6678 },
  { id: 3, type: 'pharmacy', name: 'Farmacia Vida Sur', lat: -33.4628, lng: -70.6486 },
  { id: 4, type: 'supermarket', name: 'Supermercado Plaza', lat: -33.4497, lng: -70.6614 },
  { id: 5, type: 'supermarket', name: 'Mercado Familiar', lat: -33.4579, lng: -70.6812 },
  { id: 6, type: 'supermarket', name: 'Supermercado Oriente', lat: -33.4371, lng: -70.6428 },
  { id: 7, type: 'pharmacy', name: 'Botica Popular', lat: -33.4711, lng: -70.6703 },
  { id: 8, type: 'supermarket', name: 'Super Barrio', lat: -33.4287, lng: -70.687 },
];

@Component({
  selector: 'fw-medical-resources',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './medical-resources.component.html',
  styleUrl: './medical-resources.component.scss',
})
export class MedicalResourcesComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapHost', { static: true }) mapHost!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private layerGroup?: L.LayerGroup;
  private resizeObserver?: ResizeObserver;

  readonly resources = signal<ResourcePoint[]>(SAMPLE_RESOURCES);

  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      this.initMap();
      this.bindMapResize();
      this.renderMarkers();
    });
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.map?.remove();
    this.map = undefined;
  }

  refresh(): void {
    this.resources.set(this.shuffleResources());
    this.renderMarkers();
  }

  private initMap(): void {
    if (this.map) {
      return;
    }
    this.map = L.map(this.mapHost.nativeElement, {
      zoomControl: true,
      attributionControl: true,
    }).setView(DEFAULT_CENTER, 13);

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

  private renderMarkers(): void {
    if (!this.map || !this.layerGroup) {
      return;
    }
    this.layerGroup.clearLayers();

    const rows = this.resources();
    const points: L.LatLngExpression[] = [];
    for (const item of rows) {
      points.push([item.lat, item.lng]);
      L.marker([item.lat, item.lng], { icon: this.resourceIcon(item.type) })
        .bindPopup(`<strong>${this.escape(item.name)}</strong><br>${this.labelFor(item.type)}`)
        .addTo(this.layerGroup);
    }

    this.map.fitBounds(L.latLngBounds(points), { padding: [44, 44], maxZoom: 14 });
  }

  private shuffleResources(): ResourcePoint[] {
    return SAMPLE_RESOURCES.map((item) => ({
      ...item,
      lat: item.lat + (Math.random() - 0.5) * 0.01,
      lng: item.lng + (Math.random() - 0.5) * 0.01,
    }));
  }

  private resourceIcon(type: ResourceType): L.DivIcon {
    const icon = type === 'pharmacy' ? 'local_pharmacy' : 'storefront';
    const colorClass = type === 'pharmacy' ? 'fw-resource-pin-pharmacy' : 'fw-resource-pin-market';
    return L.divIcon({
      className: 'fw-resource-pin',
      html: `<span class="${colorClass} material-icons">${icon}</span>`,
      iconSize: [34, 34],
      iconAnchor: [17, 17],
    });
  }

  private labelFor(type: ResourceType): string {
    return type === 'pharmacy' ? 'Farmacia de ejemplo' : 'Supermercado de ejemplo';
  }

  private invalidateMapSize(): void {
    this.map?.invalidateSize({ animate: false });
    setTimeout(() => this.map?.invalidateSize({ animate: false }), 150);
  }

  private escape(s: string): string {
    return (s ?? '').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
}
