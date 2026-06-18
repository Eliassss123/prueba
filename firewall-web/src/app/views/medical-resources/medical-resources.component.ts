import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import * as L from 'leaflet';
import { GeolocationViewModel } from '../../viewmodels/geolocation.viewmodel';

delete (L.Icon.Default.prototype as L.Icon.Default & { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: 'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png',
});

type ResourceType = 'pharmacy' | 'supermarket';
type ResourceFilter = 'all' | ResourceType;
type SortMode = 'distance' | 'name';

interface ResourcePoint {
  id: string;
  type: ResourceType;
  name: string;
  lat: number;
  lng: number;
  address: string;
  distanceKm: number;
  googleMapsUrl: string;
}

interface NominatimPlace {
  place_id: number;
  lat: string;
  lon: string;
  display_name: string;
  name?: string;
}

const MAP_TILE_URL = 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
const MAP_TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';
const DEFAULT_CENTER: [number, number] = [-33.45, -70.67];
const DEFAULT_RADIUS_KM = 6;
const FALLBACK_SEED: Array<{
  type: ResourceType;
  name: string;
  latOffset: number;
  lngOffset: number;
  address: string;
}> = [
  {
    type: 'pharmacy',
    name: 'Farmacia de turno',
    latOffset: 0.006,
    lngOffset: -0.004,
    address: 'Referencia local para contingencia',
  },
  {
    type: 'pharmacy',
    name: 'Botica comunal',
    latOffset: -0.005,
    lngOffset: 0.005,
    address: 'Referencia local para contingencia',
  },
  {
    type: 'supermarket',
    name: 'Supermercado vecinal',
    latOffset: 0.008,
    lngOffset: 0.004,
    address: 'Referencia local para contingencia',
  },
  {
    type: 'supermarket',
    name: 'Minimarket de apoyo',
    latOffset: -0.007,
    lngOffset: -0.005,
    address: 'Referencia local para contingencia',
  },
];

@Component({
  selector: 'fw-medical-resources',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './medical-resources.component.html',
  styleUrl: './medical-resources.component.scss',
})
export class MedicalResourcesComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapHost', { static: true }) mapHost!: ElementRef<HTMLDivElement>;

  private readonly http = inject(HttpClient);
  private readonly geolocation = inject(GeolocationViewModel);
  private map?: L.Map;
  private layerGroup?: L.LayerGroup;
  private resizeObserver?: ResizeObserver;
  private readonly markerById = new Map<string, L.Marker>();

  readonly resources = signal<ResourcePoint[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly userLocationLabel = signal('Ubicación por defecto (Santiago centro)');
  readonly lastUpdated = signal<Date | null>(null);
  readonly userPosition = signal<{ lat: number; lng: number } | null>(null);

  readonly selectedType = signal<ResourceFilter>('all');
  readonly searchText = signal('');
  readonly sortMode = signal<SortMode>('distance');
  readonly radiusKm = signal(DEFAULT_RADIUS_KM);

  readonly totalFarmacias = computed(() => this.resources().filter((r) => r.type === 'pharmacy').length);
  readonly totalSupermercados = computed(() => this.resources().filter((r) => r.type === 'supermarket').length);
  readonly nearestResource = computed(() => {
    const rows = this.resources();
    if (rows.length === 0) {
      return null;
    }
    return [...rows].sort((a, b) => a.distanceKm - b.distanceKm)[0] ?? null;
  });
  readonly filteredResources = computed(() => {
    const query = this.searchText().trim().toLowerCase();
    const selectedType = this.selectedType();
    const mode = this.sortMode();
    const rows = this.resources().filter((item) => {
      const matchesType = selectedType === 'all' || item.type === selectedType;
      if (!matchesType) {
        return false;
      }
      if (!query) {
        return true;
      }
      return item.name.toLowerCase().includes(query) || item.address.toLowerCase().includes(query);
    });
    return [...rows].sort((a, b) => {
      if (mode === 'name') {
        return a.name.localeCompare(b.name, 'es', { sensitivity: 'base' });
      }
      return a.distanceKm - b.distanceKm;
    });
  });
  readonly visibleCount = computed(() => this.filteredResources().length);

  constructor() {
    effect(() => {
      this.filteredResources();
      queueMicrotask(() => this.renderMarkers());
    });
  }

  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      this.initMap();
      this.bindMapResize();
      this.refresh();
    });
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.map?.remove();
    this.map = undefined;
    this.markerById.clear();
  }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.geolocation.requestCurrentPosition().subscribe({
      next: (pos) => {
        this.userPosition.set({ lat: pos.lat, lng: pos.lng });
        this.userLocationLabel.set(`Tu ubicación aproximada: ${pos.lat.toFixed(4)}, ${pos.lng.toFixed(4)}`);
        this.loadRealResources(pos.lat, pos.lng);
      },
      error: () => {
        this.userPosition.set({ lat: DEFAULT_CENTER[0], lng: DEFAULT_CENTER[1] });
        this.userLocationLabel.set('Sin permiso de GPS: usando Santiago centro');
        this.loadRealResources(DEFAULT_CENTER[0], DEFAULT_CENTER[1]);
      },
    });
  }

  openInGoogleMaps(resource: ResourcePoint): void {
    window.open(resource.googleMapsUrl, '_blank', 'noopener,noreferrer');
  }

  focusResource(resource: ResourcePoint): void {
    if (!this.map) {
      return;
    }
    this.map.setView([resource.lat, resource.lng], 15, { animate: true });
    this.markerById.get(resource.id)?.openPopup();
  }

  focusUserLocation(): void {
    if (!this.map || !this.userPosition()) {
      return;
    }
    const pos = this.userPosition();
    if (!pos) {
      return;
    }
    this.map.setView([pos.lat, pos.lng], 14, { animate: true });
  }

  setRadius(km: number): void {
    if (this.radiusKm() === km) {
      return;
    }
    this.radiusKm.set(km);
    this.refresh();
  }

  setTypeFilter(type: ResourceFilter): void {
    this.selectedType.set(type);
  }

  setSortMode(mode: string): void {
    this.sortMode.set(mode === 'name' ? 'name' : 'distance');
  }

  setSearchText(value: string): void {
    this.searchText.set(value ?? '');
  }

  clearFilters(): void {
    this.selectedType.set('all');
    this.searchText.set('');
    this.sortMode.set('distance');
  }

  resourceTypeLabel(type: ResourceType): string {
    return type === 'pharmacy' ? 'Farmacia' : 'Supermercado';
  }

  resourceTypeClass(type: ResourceType): string {
    return type === 'pharmacy' ? 'fw-resource-pill-pharmacy' : 'fw-resource-pill-market';
  }

  distanceLabel(distanceKm: number): string {
    return `${distanceKm.toFixed(1)} km`;
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
    this.markerById.clear();

    const rows = this.filteredResources();
    const points: L.LatLngExpression[] = [];
    const user = this.userPosition();
    if (user) {
      points.push([user.lat, user.lng]);
      L.circleMarker([user.lat, user.lng], {
        radius: 8,
        color: '#1d4ed8',
        fillColor: '#3b82f6',
        fillOpacity: 0.85,
        weight: 2,
      })
        .bindPopup('Tu ubicación aproximada')
        .addTo(this.layerGroup);
    }

    for (const item of rows) {
      points.push([item.lat, item.lng]);
      const marker = L.marker([item.lat, item.lng], { icon: this.resourceIcon(item.type) })
        .bindPopup(
          `<strong>${this.escape(item.name)}</strong><br>${this.escape(this.labelFor(item.type))}<br>${this.escape(item.distanceKm.toFixed(1))} km<br><a href="${item.googleMapsUrl}" target="_blank" rel="noopener">Abrir en Google Maps</a>`,
        )
        .addTo(this.layerGroup);
      this.markerById.set(item.id, marker);
    }

    if (points.length > 0) {
      this.map.fitBounds(L.latLngBounds(points), { padding: [44, 44], maxZoom: 14 });
    } else if (user) {
      this.map.setView([user.lat, user.lng], 13);
    } else {
      this.map.setView(DEFAULT_CENTER, 13);
    }
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
    return type === 'pharmacy' ? 'Farmacia' : 'Supermercado';
  }

  private invalidateMapSize(): void {
    this.map?.invalidateSize({ animate: false });
    setTimeout(() => this.map?.invalidateSize({ animate: false }), 150);
  }

  private escape(s: string): string {
    return (s ?? '').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  private loadRealResources(lat: number, lng: number): void {
    forkJoin({
      pharmacies: this.searchNearby('farmacia', lat, lng).pipe(catchError(() => of([] as NominatimPlace[]))),
      pharmaciesAlt: this.searchNearby('botica', lat, lng).pipe(catchError(() => of([] as NominatimPlace[]))),
      supermarkets: this.searchNearby('supermercado', lat, lng).pipe(catchError(() => of([] as NominatimPlace[]))),
      supermarketsAlt: this.searchNearby('minimarket', lat, lng).pipe(catchError(() => of([] as NominatimPlace[]))),
    }).subscribe({
      next: ({ pharmacies, pharmaciesAlt, supermarkets, supermarketsAlt }) => {
        const merged = [
          ...this.mapPlaces([...pharmacies, ...pharmaciesAlt], 'pharmacy', lat, lng),
          ...this.mapPlaces([...supermarkets, ...supermarketsAlt], 'supermarket', lat, lng),
        ];
        const deduped = this.dedupeResources(merged);
        if (deduped.length === 0) {
          this.resources.set(this.buildFallbackResources(lat, lng));
          this.error.set('No se encontraron resultados en línea. Se muestran recursos de referencia cercanos.');
          this.searchText.set('');
          this.selectedType.set('all');
        } else {
          this.resources.set(deduped);
        }
        this.lastUpdated.set(new Date());
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar lugares reales. Se muestran recursos de referencia.');
        this.resources.set(this.buildFallbackResources(lat, lng));
        this.lastUpdated.set(new Date());
      },
    });
  }

  private searchNearby(query: string, lat: number, lng: number) {
    const radiusKm = this.radiusKm();
    const latDelta = radiusKm / 111;
    const lngDelta = radiusKm / (111 * Math.max(0.2, Math.abs(Math.cos((lat * Math.PI) / 180))));

    const params = new HttpParams()
      .set('q', query)
      .set('format', 'jsonv2')
      .set('limit', '18')
      .set('addressdetails', '1')
      .set('countrycodes', 'cl')
      .set('bounded', '1')
      .set('viewbox', `${lng - lngDelta},${lat + latDelta},${lng + lngDelta},${lat - latDelta}`)
      .set('accept-language', 'es');

    return this.http.get<NominatimPlace[]>('https://nominatim.openstreetmap.org/search', {
      params,
      headers: { 'Accept-Language': 'es' },
    });
  }

  private mapPlaces(rows: NominatimPlace[], type: ResourceType, originLat: number, originLng: number): ResourcePoint[] {
    return rows
      .map((row) => {
        const lat = Number(row.lat);
        const lng = Number(row.lon);
        if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
          return null;
        }
        const displayName = (row.name ?? row.display_name ?? '').trim();
        return {
          id: `${type}-${row.place_id}`,
          type,
          name: displayName || (type === 'pharmacy' ? 'Farmacia' : 'Supermercado'),
          lat,
          lng,
          address: row.display_name,
          distanceKm: this.distanceKm(originLat, originLng, lat, lng),
          googleMapsUrl: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${lat},${lng}`)}`,
        } satisfies ResourcePoint;
      })
      .filter((item): item is ResourcePoint => item !== null);
  }

  private dedupeResources(rows: ResourcePoint[]): ResourcePoint[] {
    const seen = new Set<string>();
    const out: ResourcePoint[] = [];
    for (const row of rows) {
      const key = `${row.type}|${row.name.toLowerCase()}|${row.lat.toFixed(4)}|${row.lng.toFixed(4)}`;
      if (seen.has(key)) {
        continue;
      }
      seen.add(key);
      out.push(row);
    }
    return out;
  }

  private buildFallbackResources(originLat: number, originLng: number): ResourcePoint[] {
    return FALLBACK_SEED.map((seed, index) => {
      const lat = originLat + seed.latOffset;
      const lng = originLng + seed.lngOffset;
      return {
        id: `fallback-${seed.type}-${index}`,
        type: seed.type,
        name: seed.name,
        lat,
        lng,
        address: seed.address,
        distanceKm: this.distanceKm(originLat, originLng, lat, lng),
        googleMapsUrl: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${lat},${lng}`)}`,
      } satisfies ResourcePoint;
    }).sort((a, b) => a.distanceKm - b.distanceKm);
  }

  private distanceKm(fromLat: number, fromLng: number, toLat: number, toLng: number): number {
    const toRad = (deg: number) => (deg * Math.PI) / 180;
    const dLat = toRad(toLat - fromLat);
    const dLng = toRad(toLng - fromLng);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(fromLat)) * Math.cos(toRad(toLat)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return 6371 * c;
  }
}
