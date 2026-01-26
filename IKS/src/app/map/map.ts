import { Component, AfterViewInit } from '@angular/core';
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
  private map!: L.Map;

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

