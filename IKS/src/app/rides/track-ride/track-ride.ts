import { Component, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatNativeDateModule } from '@angular/material/core';
import { MapView } from '../../map/map';
import { PanicService } from '../../services/panic.service';
import { AuthService } from '../../services/auth.service';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { RideLiveUpdateDTO } from '../../models/ride-live-update-dto.model';
import { RideWebSocketService } from '../../services/ride-websocket.service';
import { interval } from 'rxjs';

interface Ride {
  rideId: number;
  from: string;
  to: string;
  startTime: string;
  price: string;
  driver: {
    name: string;
    phone: string;
    vehicleType: string;
  };
  estimatedEndTime: string;
  eta: string;
}

@Component({
  selector: 'app-track-ride',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatToolbarModule,
    MatIconModule,
    MatNativeDateModule,
    MapView
  ],
  templateUrl: './track-ride.html',
  styleUrl: './track-ride.css',
})

export class TrackRide implements AfterViewInit {
  selectedRide: Ride = {
    rideId: 1,
    from: 'Strazilovska 3',
    to: 'Preradoviceva 39',
    startTime: '12:03',
    estimatedEndTime: '12:34',
    eta: '31 mins',
    price: '1200',
    driver: {
      name: 'Boris Brkić',
      phone: '+381 64 1234567',
      vehicleType: 'STANDARD'
    },
  };
  @ViewChild(MapView) mapComponent?: MapView;
  currentUser: any = null;
  isLoggedIn: boolean = false;
  liveUpdate$ = new BehaviorSubject<RideLiveUpdateDTO | null>(null);

  constructor(private panicService: PanicService, private authService: AuthService, private rideWs: RideWebSocketService) { }
  
  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isLoggedIn = !!user && !!user.email;
    });
    
    this.startFakeLiveRide();
    //this.connectToRideWS();
  }

  startFakeLiveRide() {
    const path = [
      [44.7866, 20.4489],
      [44.7875, 20.4570],
      [44.7900, 20.4600],
      [44.7950, 20.4630],
      [44.8080, 20.4651],
    ];

    let index = 0;
    let distance = 0;

    interval(1000).subscribe(() => {
      if (index >= path.length) return;

      distance += 0.3;

      this.liveUpdate$.next({
        latitude: path[index][0],
        longitude: path[index][1],
        distanceTraveled: distance,
        estimatedTimeSec: Math.max(0, 1800 - index * 240),
        currentPrice: 400 + distance * 120
      });

      this.selectedRide.price = (400 + distance * 120).toFixed(0);
      this.selectedRide.eta = `${Math.ceil((1800 - index * 240) / 60)} min`;

      index++;
    });
  }


  connectToRideWS() {
    this.rideWs.connect(this.selectedRide.rideId)
      .subscribe(update => {
        this.liveUpdate$.next(update);

        this.selectedRide.price = update.currentPrice.toFixed(2);
        this.selectedRide.eta = `${Math.ceil(update.estimatedTimeSec / 60)} min`;
      });
  }

  ngOnDestroy(): void {
    this.rideWs.disconnect();
  }

  ngAfterViewInit(): void {
    this.mapComponent?.showRoute(
      [
        [44.7866, 20.4489],
        [44.7875, 20.4570],
        [44.8080, 20.4651],
      ],
      this.selectedRide.eta
    );

    this.mapComponent?.setPickupDestinationAndVehicleType(
      this.selectedRide.from,
      this.selectedRide.to,
      this.selectedRide.driver.vehicleType
    );
  }

  openReportDriver(ride: Ride) {
    // add driver report endpoint later
    console.log('Reporting driver:', ride.driver.name);
  }

  onPanicClick() {
    if (!this.selectedRide) return;
    const payload = {
      rideId: this.selectedRide.rideId,
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
}
