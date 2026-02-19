import { Component, ViewChild, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
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
  isLoggedIn: boolean = false;
  isDriver: boolean = false;
  isPassenger: boolean = false;
  currentPrice?: number;
  eta?: string;
  etaMin?: number; // minutes
  currentLat: number = 0; // Current vehicle latitude
  currentLng: number = 0; // Current vehicle longitude

  constructor(
    private panicService: PanicService, 
    private authService: AuthService, 
    private notificationService: NotificationService,
    private rideTrackingService: RideTrackingService, 
    private rideService: RideService, 
    private cdr: ChangeDetectorRef, 
    private route: ActivatedRoute, 
    private dialog: MatDialog
  ) { }
  
  ngOnInit(): void {
    const rideIdParam = this.route.snapshot.paramMap.get('id');
    this.rideId = rideIdParam ? parseInt(rideIdParam, 10) : undefined;

    // Validate rideId was parsed correctly
    if (!this.rideId || isNaN(this.rideId)) {
      console.error('[TrackRide] Invalid or missing ride ID:', rideIdParam);
      alert('Error: Invalid ride ID. Please navigate back and try again.');
      return;
    }

    console.log('[TrackRide] Loaded ride ID:', this.rideId);

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isLoggedIn = !!user && !!user.email;
      this.isDriver = user?.userType === 'DRIVER';
      this.isPassenger = user?.userType === 'REGISTERED_USER';
    });

    // Load ride details for vehicle information
    this.rideService.getRideDetails(this.rideId!)
      .pipe(take(1))
      .subscribe({
        next: (details) => {
          this.rideDetails = details;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading ride details:', err)
      });

    this.rideTrackingService.getRideTracking(this.rideId)
      .pipe(take(1))
      .subscribe(tracking => {
        console.log('[TrackRide] getRideTracking response:', tracking);
        this.selectedRide = tracking;
        this.cdr.detectChanges();

        this.mapComponent?.setPickupDestinationAndVehicleType(
          tracking?.startAddress,
          tracking?.endAddress,
          tracking?.vehicleType
        );

        this.mapComponent?.trackRoute([tracking?.startLatitude || 0, tracking?.startLongitude || 0], [tracking?.endLatitude || 0, tracking?.endLongitude || 0]);

        // Initialize vehicle position at pickup location
        if (tracking?.startLatitude && tracking?.startLongitude) {
          console.log('[TrackRide] Initializing vehicle position at start:', [tracking.startLatitude, tracking.startLongitude]);
          this.currentLat = tracking.startLatitude;
          this.currentLng = tracking.startLongitude;
          this.mapComponent?.updateVehiclePosition([tracking.startLatitude, tracking.startLongitude], this.isPanicActive);
        }

        console.log('[TrackRide] Connecting to ride tracking for rideId:', this.rideId);
        this.rideTrackingService.connectToRideTracking(this.rideId!);
        
        const liveSubscription = this.rideTrackingService.liveRideInfo$.subscribe(data => {
          console.log('[TrackRide] liveRideInfo$ received:', data);
          if (!data) {
            console.warn('[TrackRide] ⚠️ Received empty data from liveRideInfo$');
            return;
          }
          
          const [ lat, lng, remainingDuration, price ] = data;
          this.currentPrice = price;
          this.etaMin = remainingDuration;
          this.eta = this.calculateETA(remainingDuration);
          // Store current vehicle position
          this.currentLat = lat;
          this.currentLng = lng;
          console.log('[TrackRide] Updating vehicle position:', { lat, lng, isPanicActive: this.isPanicActive });
          this.mapComponent?.updateVehiclePosition([lat, lng], this.isPanicActive);
          this.cdr.detectChanges();
        });
        this.subscriptions.add(liveSubscription);
      });

    // Subscribe to panic vehicles to reset icon when panic is handled
    const panicSub = this.notificationService.vehiclesInPanic$.subscribe(panicVehicles => {
      console.log('[TrackRide] Panic vehicles changed:', Array.from(panicVehicles));
      // If current ride's vehicle was in panic but is now removed, reset the icon
      if (this.rideDetails?.vehicleLicensePlate) {
        const isNowInPanic = panicVehicles.has(this.rideDetails.vehicleLicensePlate);
        if (!isNowInPanic && this.isPanicActive) {
          console.log('[TrackRide] Panic was handled for this vehicle, resetting icon');
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

  calculateETA(remainingDuration: number): string {
    const now = new Date();

    now.setMinutes(now.getMinutes() + remainingDuration);

    const formattedHours = now.getHours().toString().padStart(2, '0');
    const formattedMinutes = now.getMinutes().toString().padStart(2, '0');

    return `${formattedHours}:${formattedMinutes}`;
  }

  shortenAddress(address? : string): string {
    return address ? address.split(',')[0] : '';
  }

  openReportDriver(ride: RideTrackingDTO) {
    const dialogRef = this.dialog.open(ReportDriver, {
      width: '480px',
      data: { rideId: this.rideId, passengerEmail: this.currentUser?.email }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Report submitted:', result);
      }
    });
  }

  onPanicClick() {
    if (!this.selectedRide || !this.rideId || isNaN(this.rideId)) {
      alert('Error: Unable to send panic alert. Ride information is missing or invalid.');
      console.error('[TrackRide] onPanicClick: Invalid ride data', { rideId: this.rideId, selectedRide: this.selectedRide });
      return;
    }

    if (!this.currentUser?.email || !this.currentUser?.userType) {
      alert('Error: User information is missing.');
      console.error('[TrackRide] Missing user information', { email: this.currentUser?.email, userType: this.currentUser?.userType });
      return;
    }

    // Mark panic as active
    this.isPanicActive = true;
    console.log('[TrackRide] 🚨 PANIC ACTIVATED - setting vehicle to panic mode');

    // Create location for panic alert using current GPS coordinates
    // Backend might create location from coordinates or we may need to get locationId separately
    const payload = {
      rideId: this.rideId,
      locationId: 0, // Placeholder - backend may handle location creation from coordinates
      userType: this.currentUser.userType, // DRIVER or REGISTERED_USER
      accountEmail: this.currentUser.email,
      latitude: this.currentLat,
      longitude: this.currentLng
    };

    console.log('[TrackRide] Sending panic alert with payload:', payload);
    console.log('[TrackRide] Current GPS location:', { latitude: this.currentLat, longitude: this.currentLng });

    this.panicService.triggerPanic(payload).subscribe({
      next: () => {
        console.log('[TrackRide] ✅ Panic signal sent successfully');
        alert('🚨 Panic signal sent! Administrators have been notified.');
      },
      error: (err) => {
        console.error('Failed to send panic:', err);
        console.error('Error details:', err.error);
        this.isPanicActive = false; // Reset if panic fails
        alert('Failed to send panic signal. Please make sure you are in an active ride.');
      }
    });
  }
}
