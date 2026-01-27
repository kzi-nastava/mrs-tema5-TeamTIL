
import { Component, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Map } from '../../map/map';
import { RouteService } from '../../services/route.service';
import { GeocodingService } from '../../services/geocoding.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [Map, FormsModule, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home {
  @ViewChild(Map) mapComponent?: Map;
  pickupLocation = '';
  destination = '';
  vehicleType = 'STANDARD';
  showEstimateButton = false;
  showForm = true;

  constructor(private routeService: RouteService, private geocodingService: GeocodingService) {}

  onInputChange() {
    this.showEstimateButton = !!this.pickupLocation && !!this.destination;
  }

  estimateRideTime() {
    this.showForm = false;
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
                if (this.mapComponent) {
                  if (result.route) {
                    this.mapComponent.showRoute(result.route, result.estimatedTime);
                  } else {
                    // Ako nema rute, nacrtaj liniju od pickup do destination
                    this.mapComponent.showRoute([
                      [pickupLat, pickupLon],
                      [destinationLat, destinationLon]
                    ], result.estimatedTime);
                  }
                }
                // info o ceni se više ne prikazuje
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

  onMapClick() {
    this.showForm = true;
  }
}
