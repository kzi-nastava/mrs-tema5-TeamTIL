import { Component, ViewChild, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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
import { NotificationService } from '../../services/notification.service';
import { take } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { RideTrackingService } from '../services/ride-tracking.service';
import { RideService } from '../services/ride.service';
import { ActivatedRoute } from '@angular/router';
import { RideTrackingDTO } from '../../models/ride-tracking-dto.model';
import { MatDialog } from '@angular/material/dialog';
import { ReportDriver } from '../modals/report-driver/report-driver';


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

export class TrackRide implements OnInit, OnDestroy {
  rideId?: number;

  selectedRide?: RideTrackingDTO;
  rideDetails?: any;
  isPanicActive: boolean = false;
  private subscriptions = new Subscription();

  @ViewChild(MapView) mapComponent?: MapView;

  currentUser: any = null;
  isLoggedIn = false;
  isDriver = false;
  isPassenger = false;
  isAdmin = false;

  currentPrice?: number;
  eta?: string;
  etaMin?: number;
  currentLat = 0;
  currentLng = 0;

  // when ride is over (driver called endRide / stopRide)
  rideEnded = false;
  // when this user has no access to this ride
  accessDenied = false;
  // while loading initial ride data
  isLoading = true;

  constructor(
    private panicService: PanicService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private rideTrackingService: RideTrackingService,
    private rideService: RideService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    const rideIdParam = this.route.snapshot.paramMap.get('id');
    this.rideId = rideIdParam ? parseInt(rideIdParam, 10) : undefined;

    // Validate rideId was parsed correctly
    if (!this.rideId || isNaN(this.rideId)) {
      console.error('[TrackRide] Invalid ride ID:', rideIdParam);
      this.accessDenied = true;
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    // Resolve current user
    this.authService.currentUser$.pipe(take(1)).subscribe(user => {
      this.currentUser = user;
      this.isLoggedIn = !!user?.email;
      this.isDriver = user?.userType === 'DRIVER';
      this.isPassenger = user?.userType === 'REGISTERED_USER';
      this.isAdmin = user?.userType === 'ADMINISTRATOR';
    });

    // Load ride tracking info – backend returns 403 if ride not IN_PROGRESS or user has no access
    this.rideTrackingService.getRideTracking(this.rideId)
      .pipe(take(1))
      .subscribe({
        next: (tracking) => {
          this.selectedRide = tracking;
          this.isLoading = false;
          this.cdr.detectChanges();

          this.mapComponent?.setPickupDestinationAndVehicleType(
            tracking?.startAddress,
            tracking?.endAddress,
            tracking?.vehicleType
          );
          this.mapComponent?.trackRoute(
            [tracking?.startLatitude || 0, tracking?.startLongitude || 0],
            [tracking?.endLatitude || 0, tracking?.endLongitude || 0]
          );

          if (tracking?.startLatitude && tracking?.startLongitude) {
            this.currentLat = tracking.startLatitude;
            this.currentLng = tracking.startLongitude;
            this.mapComponent?.updateVehiclePosition(
              [tracking.startLatitude, tracking.startLongitude], false);
          }

          // Connect WebSocket only after we know the ride is active and accessible
          this.rideTrackingService.connectToRideTracking(this.rideId!);
          this.subscribeToLiveUpdates();
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 403 || err.status === 401) {
            this.accessDenied = true;
          } else {
            // Unexpected error – still deny access gracefully
            this.accessDenied = true;
            console.error('[TrackRide] Error loading tracking:', err);
          }
          this.cdr.detectChanges();
        }
      });
    
    // Load ride details (for vehicle info / panic)
    this.rideService.getRideDetails(this.rideId)
      .pipe(take(1))
      .subscribe({
        next: (details) => { this.rideDetails = details; this.cdr.detectChanges(); },
        error: (err) => console.warn('[TrackRide] Could not load ride details:', err)
      });
  }

  private subscribeToLiveUpdates(): void {
    // Live position updates
    const liveSub = this.rideTrackingService.liveRideInfo$.subscribe(
      ([lat, lng, remainingDuration, price]) => {
        this.currentPrice = price;
        this.etaMin = remainingDuration;
        this.eta = this.calculateETA(remainingDuration);
        this.currentLat = lat;
        this.currentLng = lng;
        this.mapComponent?.updateVehiclePosition([lat, lng], this.isPanicActive);
        this.cdr.detectChanges();
      }
    );
    this.subscriptions.add(liveSub);

    // Ride ended signal
    const endedSub = this.rideTrackingService.rideEnded$.subscribe(() => {
      this.rideEnded = true;
      this.isPanicActive = false;
      this.cdr.detectChanges();
    });
    this.subscriptions.add(endedSub);

    // Ride not active (connected before start or after end)
    const notActiveSub = this.rideTrackingService.rideNotActive$.subscribe(() => {
      this.accessDenied = true;
      this.cdr.detectChanges();
    });
    this.subscriptions.add(notActiveSub);

    // Panic vehicle tracking
    const panicSub = this.notificationService.vehiclesInPanic$.subscribe(panicVehicles => {
      if (this.rideDetails?.vehicleLicensePlate) {
        const isNowInPanic = panicVehicles.has(this.rideDetails.vehicleLicensePlate);
        if (!isNowInPanic && this.isPanicActive) {
          this.isPanicActive = false;
          this.mapComponent?.updateVehiclePosition([this.currentLat, this.currentLng], false);
        }
      }
    });
    this.subscriptions.add(panicSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.rideTrackingService.disconnect();
  }

  onPanicClick(): void {
    if (!this.selectedRide || !this.rideId || isNaN(this.rideId)) {
      alert('Error: Unable to send panic alert. Ride information is missing.');
      return;
    }
    if (!this.currentUser?.email || !this.currentUser?.userType) {
      alert('Error: User information is missing.');
      return;
    }

    this.isPanicActive = true;

    const payload = {
      rideId: this.rideId,
      locationId: 0,
      userType: this.currentUser.userType,
      accountEmail: this.currentUser.email,
      // Stvarna pozicija vozila iz WebSocket toka
      latitude: this.currentLat,
      longitude: this.currentLng
    };

    this.panicService.triggerPanic(payload).subscribe({
      next: () => {
        alert('🚨 Panic signal sent! Administrators have been notified.');
      },
      error: (err) => {
        console.error('[TrackRide] Failed to send panic:', err);
        this.isPanicActive = false;
        alert('Failed to send panic signal. Please make sure you are in an active ride.');
      }
    });
  }

  openReportDriver(ride: RideTrackingDTO): void {
    this.dialog.open(ReportDriver, {
      width: '480px',
      data: { rideId: this.rideId, passengerEmail: this.currentUser?.email }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  calculateETA(remainingDuration: number): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() + remainingDuration);
    return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
  }

  shortenAddress(address?: string): string {
    return address ? address.split(',')[0] : '';
  }
}
