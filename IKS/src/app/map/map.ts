import { Component, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';

interface VehicleDTO {
  name: string;
  type: string;
  licensePlate: string;
  available: boolean;
  latitude: number;
  longitude: number;
}

@Component({
  selector: 'app-map-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.html',
  styleUrls: ['./map.css']
})
export class MapView implements AfterViewInit {
  @Output() mapClicked = new EventEmitter<void>();
  private map!: L.Map;
  private routeLayer?: L.Polyline;
  private startMarker?: L.Marker;
  private endMarker?: L.Marker;

  private vehicleMarkers: Map<string, L.Marker> = new Map();

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initMap();
    this.fetchVehicles();
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

  private fetchVehicles(): void {
    this.http.get<VehicleDTO[]>('http://localhost:8080/api/public/vehicles')
      .subscribe({
        next: (data) => {
          this.updateVehicleMarkers(data);
        },
        error: (err) => console.error('Error fetching vehicles:', err)
      });
  }
  
  private updateVehicleMarkers(vehicles: VehicleDTO[]): void {
    vehicles.forEach(vehicle => {
      const existingMarker = this.vehicleMarkers.get(vehicle.licensePlate);

      if (existingMarker) {
        existingMarker.setLatLng([vehicle.latitude, vehicle.longitude]);
        const icon = vehicle.available ? this.activeIcon : this.inactiveIcon;
        existingMarker.setIcon(icon);
        existingMarker.bindPopup(`
          <strong>${vehicle.name}</strong><br/>
            <i>${vehicle.licensePlate}</i><br/>
            Status: <span style="color: ${vehicle.available ? 'green' : 'red'}">
              ${vehicle.available ? 'Available' : 'Unavailable'}
            </span>
        `);
      } else {
        const icon = vehicle.available ? this.activeIcon : this.inactiveIcon;
        const marker = L.marker([vehicle.latitude, vehicle.longitude], { icon })
          .addTo(this.map)
          .bindPopup(`
            <strong>${vehicle.name}</strong><br/>
            <i>${vehicle.licensePlate}</i><br/>
            Status: <span style="color: ${vehicle.available ? 'green' : 'red'}">
              ${vehicle.available ? 'Available' : 'Unavailable'}
            </span>
          `);

        this.vehicleMarkers.set(vehicle.licensePlate, marker);
      }
    });
  }
}

