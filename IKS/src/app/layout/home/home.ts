import { Component, ViewChild, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MapView } from '../../map/map';
import { RouteService } from '../../services/route.service';
import { GeocodingService } from '../../services/geocoding.service';
import { RideService } from '../../rides/services/ride.service';
import { AuthService } from '../../services/auth.service';
import { PanicService } from '../../services/panic.service';

interface RideCard {
  id: number;
  startTime: string;
  from: string;
  to: string;
  status: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MapView, FormsModule, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home implements OnInit {
    showPanicToast = false;
  @ViewChild(MapView) mapComponent?: MapView;
  pickupLocation = '';
  destination = '';
  vehicleType = 'STANDARD';
  showEstimateButton = false;
  showForm = true;

  userRide: RideCard | null = null;
  currentUser: any = null;
  showRideCard = false;
  isLoggedIn = false;

  constructor(
    private routeService: RouteService,
    private geocodingService: GeocodingService,
    private rideService: RideService,
    private authService: AuthService,
    private panicService: PanicService,
    private cdr: ChangeDetectorRef
  ) {}
  onPanicClick() {
    if (!this.userRide) return;
    const payload = {
      rideId: this.userRide.id,
      locationId: 1, // TODO: Replace with real locationId if available
      registeredUserId: null, // TODO: Set if user is registered user
      driverId: this.currentUser?.userType === 'DRIVER' ? this.currentUser?.email : null // Use email as identifier for now
    };
    this.panicService.triggerPanic(payload).subscribe({
      next: () => {
        alert('Panic sent!');
      },
      error: () => {
        alert('Failed to send panic!');
      }
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      console.log('[DEBUG] currentUser', user);
      this.isLoggedIn = !!user && !!user.email;
      if (
        this.isLoggedIn && user && user.email &&
        (user.userType === 'DRIVER' || user.userType === 'REGISTERED_USER')
      ) {
        // Odredi koji servis pozivaš na osnovu tipa korisnika
        const rideObservable =
          user.userType === 'DRIVER'
            ? this.rideService.getAssignedRides(user.email)
            : this.rideService.getUserRides(user.email); // Dodaj getUserRides za registrovanog korisnika

        rideObservable.subscribe(rides => {
          console.log('[DEBUG] rides for user', rides);
          // Prikazujemo samo jednu karticu:
          // 1. Ako postoji IN_PROGRESS, prikazujemo samo nju
          // 2. Ako nema IN_PROGRESS, prikazujemo najbližu REQUESTED (po startTime)
          let rideToShow = null;
          const inProgress = rides.find((r: any) => r.status === 'IN_PROGRESS');
          if (inProgress) {
            rideToShow = inProgress;
          } else {
            const requestedRides = rides.filter((r: any) => r.status === 'REQUESTED');
            if (requestedRides.length > 0) {
              rideToShow = requestedRides.sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0];
            }
          }
          if (rideToShow) {
            this.userRide = {
              id: rideToShow.id,
              startTime: rideToShow.startTime,
              from: rideToShow.startLocation,
              to: rideToShow.endLocation,
              status: rideToShow.status === 'IN_PROGRESS' ? 'In progress' : 'Requested',
            };
          } else {
            this.userRide = null;
          }
          this.showRideCard = true;
          this.showForm = false;
          this.cdr.detectChanges();
        });
      } else {
        this.userRide = null;
        this.showRideCard = false;
        this.showForm = true;
        this.cdr.detectChanges();
      }
    });
  }

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
                  if (result.routeCoordinates) {
                    // Convert [lon, lat] to [lat, lon] for Leaflet
                    const leafletRoute = result.routeCoordinates.map(([lon, lat]: [number, number]) => [lat, lon]);
                    this.mapComponent.showRoute(leafletRoute, result.estimatedTime);
                  } else if (result.route) {
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
