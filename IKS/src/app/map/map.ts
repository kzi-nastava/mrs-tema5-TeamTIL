import { Component, AfterViewInit, Output, EventEmitter, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouteService } from '../services/route.service';
import { GeocodingService } from '../services/geocoding.service';
import { RideLiveUpdateDTO } from '../models/ride-live-update-dto.model';
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
  private vehicleMarker?: L.Marker;
  private liveRouteLayer?: L.Polyline;
  private traveledCoords: [number, number][] = [];
  pickupLocation = '';
  destination = '';
  vehicleType = 'STANDARD';

  ngAfterViewInit(): void {
    this.initMap();
  }

  @Input() liveUpdate: RideLiveUpdateDTO | null = null

  ngOnChanges() {
    if(!this.liveUpdate) return;

    this.moveVehicle(
      this.liveUpdate.latitude,
      this.liveUpdate.longitude
    );
  }

  constructor(private geocodingService: GeocodingService, private routeService: RouteService) { }

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
  
  public updateVehicleMarkers(vehicles: VehicleDTO[], vehicleMarkers: Map<string, L.Marker>): void {
    vehicles.forEach(vehicle => {
      const existingMarker = vehicleMarkers.get(vehicle.licensePlate);

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

        vehicleMarkers.set(vehicle.licensePlate, marker);
      }
    });
  }

  estimateRideTime() {
    // Prvo geokodiraj pickup
    this.geocodingService.geocode(this.pickupLocation).subscribe({
      next: (pickupResults) => {
        if (!pickupResults || pickupResults.length === 0) {
          alert('Pickup address not found!');
          return;
        }
        const pickupLat = parseFloat(pickupResults[0].lat);
        const pickupLon = parseFloat(pickupResults[0].lon);
        // Sada geokodiraj destination
        this.geocodingService.geocode(this.destination).subscribe({
          next: (destResults) => {
            if (!destResults || destResults.length === 0) {
              alert('Destination address not found!');
              return;
            }
            const destinationLat = parseFloat(destResults[0].lat);
            const destinationLon = parseFloat(destResults[0].lon);
            // Sada šalji zahtev backendu
            const req = {
              pickupAddress: this.pickupLocation,
              destinationAddress: this.destination,
              vehicleType: this.vehicleType,
              pickupLat,
              pickupLon,
              destinationLat,
              destinationLon
            };
            this.routeService.estimateRouteFull(req).subscribe(
              (result) => {
                // Očekuje se: { estimatedTime, estimatedDistance, estimatedPrice, vehicleType, route? }
                // Prikaz na mapi i info korisniku
                if (result.routeCoordinates) {
                  // Convert [lon, lat] to [lat, lon] for Leaflet
                  const leafletRoute = result.routeCoordinates.map(([lon, lat]: [number, number]) => [lat, lon]);
                  this.showRoute(leafletRoute, result.estimatedTime);
                } else if (result.route) {
                  this.showRoute(result.route, result.estimatedTime);
                } else {
                  // Ako nema rute, nacrtaj liniju od pickup do destination
                  this.showRoute([
                    [pickupLat, pickupLon],
                    [destinationLat, destinationLon]
                  ], result.estimatedTime);
                }
              },
              () => {
                alert('Failed to estimate route.');
              }
            );
          },
          error: () => alert('Failed to geocode destination address!')
        });
      },
      error: () => alert('Failed to geocode pickup address!')
    });
  }

  setPickupDestinationAndVehicleType(pickup: string, destination: string, vehicleType: string) {
    this.pickupLocation = pickup;
    this.destination = destination;
    this.vehicleType = vehicleType;
  }

  moveVehicle(lat: number, lng: number) {
    if (!this.map) return;

    if (!this.vehicleMarker) {
      this.vehicleMarker = L.marker([lat, lng], {
        icon: L.icon({
          iconUrl: 'assets/active_driver.png',
          iconSize: [32, 32],
          iconAnchor: [16, 32]
        })
      }).addTo(this.map);
    } else {
      this.vehicleMarker.setLatLng([lat, lng]);
    }
  }

  // private moveVehicle(lat: number, lng: number) {
  //  if (!this.map) return;

   // const coord: [number, number] = [lat, lng];
  //  this.traveledCoords.push(coord);

  //  if (!this.vehicleMarker) {
  //    this.vehicleMarker = L.marker(coord, {
  //      icon: this.activeIcon
 //     }).addTo(this.map);
  //  } else {
  //    this.vehicleMarker.setLatLng(coord);
  //  }

  //  if (!this.liveRouteLayer) {
  //    this.liveRouteLayer = L.polyline(this.traveledCoords, {
  //      color: 'lime',
  //      weight: 6,
  //      dashArray: '10 12'
  //    }).addTo(this.map);
  //  } else {
  //    this.liveRouteLayer.setLatLngs(this.traveledCoords);
  //  }
  //}
}

