import { Component, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';


@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.html',
  styleUrls: ['./map.css']
})
export class Map implements AfterViewInit {
  @Output() mapClicked = new EventEmitter<void>();
  private map!: L.Map;
  private routeLayer?: L.Polyline;
  private startMarker?: L.Marker;
  private endMarker?: L.Marker;

  private vehicles = [
    { id: 1, lat: 45.250147, lng: 19.874772, status: 'active' },
    { id: 2, lat: 45.237199, lng: 19.827093, status: 'inactive' },
    { id: 3, lat: 45.237641, lng: 19.841512, status: 'active' },
    { id: 4, lat: 45.244073, lng: 19.832924, status: 'active' },
    { id: 5, lat: 45.252339, lng: 19.819532, status: 'inactive' },
    { id: 6, lat: 45.261022, lng: 19.841963, status: 'inactive' },
    { id: 7, lat: 45.259013, lng: 19.810785, status: 'active' },
    { id: 8, lat: 45.285512, lng: 19.762302, status: 'active' }
  ];

  ngAfterViewInit(): void {
    this.initMap();
    this.addVehicleMarkers();
  }

  public showRoute(routeCoords: [number, number][], duration?: string) {
    if (!this.map) return;
    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
    }
    if (this.startMarker) {
      this.map.removeLayer(this.startMarker);
    }
    if (this.endMarker) {
      this.map.removeLayer(this.endMarker);
    }
    this.routeLayer = L.polyline(routeCoords, { color: 'blue', weight: 5 }).addTo(this.map);
    this.map.fitBounds(this.routeLayer.getBounds(), { padding: [50, 50] });
    // Dodaj markere za start i end
    if (routeCoords.length > 1) {
      this.startMarker = L.marker(routeCoords[0], { icon: L.icon({ iconUrl: 'https://cdn-icons-png.flaticon.com/512/684/684908.png', iconSize: [32, 32], iconAnchor: [16, 32] }) })
        .addTo(this.map)
        .bindTooltip('Start', { permanent: true, direction: 'top', offset: [0, -10] });
      this.endMarker = L.marker(routeCoords[routeCoords.length - 1], { icon: L.icon({ iconUrl: 'https://cdn-icons-png.flaticon.com/512/149/149059.png', iconSize: [32, 32], iconAnchor: [16, 32] }) })
        .addTo(this.map)
        .bindTooltip('Destination', { permanent: true, direction: 'top', offset: [0, -10] });
    }
    // Popup za estimated time na sredini rute
    if (duration && routeCoords.length > 1) {
      const midIdx = Math.floor(routeCoords.length / 2);
      const midPoint = routeCoords[midIdx];
      L.popup({ closeButton: false, autoClose: false, closeOnClick: false, className: 'route-time-popup' })
        .setLatLng(midPoint)
        .setContent(`<b>Estimated time: ${duration}</b>`)
        .openOn(this.map);
    }
  }

  private activeIcon = L.icon({
    iconUrl: 'assets/active_driver.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  private inactiveIcon = L.icon({
    iconUrl: 'assets/inactive_driver.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  private initMap(): void {
    const noviSadLat = 45.265;
    const noviSadLng = 19.800;

    this.map = L.map('map').setView([noviSadLat, noviSadLng], 13);
    this.map.on('click', () => {
      this.mapClicked.emit();
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private addVehicleMarkers(): void {
    this.vehicles.forEach(vehicle => {
      const icon =
        vehicle.status === 'active'
          ? this.activeIcon
          : this.inactiveIcon;

      L.marker([vehicle.lat, vehicle.lng], { icon })
        .addTo(this.map)
        .bindPopup(`
          <strong>Vehicle #${vehicle.id}</strong><br/>
          Status: ${vehicle.status}
        `);
    });
  }
}

