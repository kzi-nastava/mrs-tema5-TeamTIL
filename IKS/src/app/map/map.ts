import { Component, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouteService } from '../services/route.service';
import { GeocodingService } from '../services/geocoding.service';
import * as L from 'leaflet';
import 'leaflet.marker.slideto';

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
  pickupLocation = '';
  destination = '';
  vehicleType = 'STANDARD';

  ngAfterViewInit(): void {
    this.initMap();
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

  private panicIcon = L.icon({
    iconUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxNCIgZmlsbD0iI0ZGMjUyNSIvPjxjaXJjbGUgY3g9IjE2IiBjeT0iMTYiIHI9IjEwIiBmaWxsPSIjRkY0NDQ0Ii8+PHRleHQgeD0iMTYiIHk9IjIyIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSJ3aGl0ZSIgZm9udC1zaXplPSIxNiIgZm9udC13ZWlnaHQ9ImJvbGQiPiE8L3RleHQ+PC9zdmc+',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
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
  
  public updateVehicleMarkers(vehicles: VehicleDTO[], vehicleMarkers: Map<string, L.Marker>, panicVehicles?: Set<string>): void {
    console.log('[Map] updateVehicleMarkers called for', vehicles.length, 'vehicles');
    if (panicVehicles && panicVehicles.size > 0) {
      console.log('[Map] Panic vehicles to preserve:', Array.from(panicVehicles));
    }
    
    vehicles.forEach(vehicle => {
      const existingMarker = vehicleMarkers.get(vehicle.licensePlate);
      const isInPanic = panicVehicles?.has(vehicle.licensePlate) || false;

      if (existingMarker) {
        existingMarker.setLatLng([vehicle.latitude, vehicle.longitude]);
        
        // Only update icon if vehicle is NOT in panic mode
        if (!isInPanic) {
          const icon = vehicle.available ? this.activeIcon : this.inactiveIcon;
          existingMarker.setIcon(icon);
        } else {
          console.log('[Map] Skipping icon update for panic vehicle:', vehicle.licensePlate);
        }
        
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
    
    console.log('[Map] updateVehicleMarkers completed. Total markers:', vehicleMarkers.size);
  }

  public markVehicleInPanic(licensePlate: string, vehicleMarkers: Map<string, L.Marker>, available: boolean = false): void {
    console.log('[Map] markVehicleInPanic called for:', licensePlate);
    console.log('[Map] Current vehicle markers:', Array.from(vehicleMarkers.keys()));
    
    const marker = vehicleMarkers.get(licensePlate);
    if (marker) {
      console.log('[Map] ✅ Found marker for', licensePlate, '- setting panic icon');
      marker.setIcon(this.panicIcon);
      marker.setZIndexOffset(1000); // Bring panic vehicle to front
    } else {
      console.warn('[Map] ⚠️ Marker NOT FOUND for license plate:', licensePlate);
    }
  }

  public unmarkVehicleInPanic(licensePlate: string, vehicleMarkers: Map<string, L.Marker>, available: boolean = true): void {
    const marker = vehicleMarkers.get(licensePlate);
    if (marker) {
      const icon = available ? this.activeIcon : this.inactiveIcon;
      marker.setIcon(icon);
      marker.setZIndexOffset(0); // Reset z-index
    }
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
            
            this.estimateRoute(req);
          },
          error: () => alert('Failed to geocode destination address!')
        });
      },
      error: () => alert('Failed to geocode pickup address!')
    });
  }

  estimateRoute(req : { pickupAddress: string; destinationAddress: string; vehicleType: string; pickupLat: number; pickupLon: number; destinationLat: number; destinationLon: number }) {
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
                    [req.pickupLat, req.pickupLon],
                    [req.destinationLat, req.destinationLon]
                  ], result.estimatedTime);
                }
              },
              () => {
                alert('Failed to estimate route.');
              }
            );
  }

  setPickupDestinationAndVehicleType(pickup?: string, destination?: string, vehicleType?  : string) {
    this.pickupLocation = pickup || '';
    this.destination = destination || '';
    this.vehicleType = vehicleType || 'STANDARD';
  }

  updateVehiclePosition(latlng: [number, number], isPanicMode: boolean = false) {
    if (!this.map) return;

    const icon = isPanicMode ? this.panicIcon : this.activeIcon;

    if (!this.vehicleMarker) {
      console.log('[Map] Creating vehicle marker at:', latlng);
      this.vehicleMarker = L.marker(latlng, { icon }).addTo(this.map);
    } else {
      console.log('[Map] Updating vehicle marker position to:', latlng);
      (this.vehicleMarker as any).slideTo(latlng, {
        duration: 200,
        keepAtCenter: false
      });
      
      // Update icon if panic mode changed
      this.vehicleMarker.setIcon(icon);
    }
  }

  async trackRoute(start: [number, number], end: [number, number]) {
    if (!this.map) return;

    const [startLat, startLng] = start;
    const [endLat, endLng] = end;
    
    const url = `http://router.project-osrm.org/route/v1/driving/${startLng},${startLat};${endLng},${endLat}?overview=full&geometries=geojson`;

    const response = await fetch(url);
    const data = await response.json();

    if (!data.routes || data.routes.length === 0) {
      console.error("Route not found");
      return;
    }

    const routeCoordinates: [number, number][] = data.routes[0].geometry.coordinates.map(
      ([lng, lat]: [number, number]) => [lat, lng] // Leaflet koristi [lat, lng]
    );

    L.polyline(routeCoordinates, { color: 'blue', weight: 5 }).addTo(this.map);
  }  
}

