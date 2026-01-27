import { Component, AfterViewInit } from '@angular/core';
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
  private map!: L.Map;
  private vehicleMarkers: Map<string, L.Marker> = new Map();

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initMap();
    this.fetchVehicles();
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
        const icon = !vehicle.available ? this.activeIcon : this.inactiveIcon;
        existingMarker.setIcon(icon);
        existingMarker.bindPopup(`
          <strong>${vehicle.name}</strong><br/>
            <i>${vehicle.licensePlate}</i><br/>
            Status: <span style="color: ${!vehicle.available ? 'green' : 'red'}">
              ${!vehicle.available ? 'Available' : 'Unavailable'}
            </span>
        `);
      } else {
        const icon = !vehicle.available ? this.activeIcon : this.inactiveIcon;
        const marker = L.marker([vehicle.latitude, vehicle.longitude], { icon })
          .addTo(this.map)
          .bindPopup(`
            <strong>${vehicle.name}</strong><br/>
            <i>${vehicle.licensePlate}</i><br/>
            Status: <span style="color: ${!vehicle.available ? 'green' : 'red'}">
              ${!vehicle.available ? 'Available' : 'Unavailable'}
            </span>
          `);

        this.vehicleMarkers.set(vehicle.licensePlate, marker);
      }
    });
  }
}

