import { Component, ViewChild, ChangeDetectorRef } from '@angular/core';
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
import { take } from 'rxjs/operators';
import { RideTrackingService } from '../services/ride-tracking.service';
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

export class TrackRide {
  rideId?: number;

  selectedRide?: RideTrackingDTO;

  @ViewChild(MapView) mapComponent?: MapView;
  currentUser: any = null;
  isLoggedIn: boolean = false;
  isDriver: boolean = false;
  isPassenger: boolean = false;
  currentPrice?: number;
  eta?: string;
  etaMin?: number; // minutes

  constructor(private panicService: PanicService, private authService: AuthService, private rideTrackingService: RideTrackingService, private cdr: ChangeDetectorRef, private route: ActivatedRoute, private dialog: MatDialog) { }
  
  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('id');
    this.rideId = Number(rideId);

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isLoggedIn = !!user && !!user.email;
      this.isDriver = user?.userType === 'DRIVER';
      this.isPassenger = user?.userType === 'REGISTERED_USER';
    });

    this.rideTrackingService.getRideTracking(this.rideId)
      .pipe(take(1))
      .subscribe(tracking => {
        this.selectedRide = tracking;
        this.cdr.detectChanges();

        this.mapComponent?.setPickupDestinationAndVehicleType(
          tracking?.startAddress,
          tracking?.endAddress,
          tracking?.vehicleType
        );

        this.mapComponent?.trackRoute([tracking?.startLatitude || 0, tracking?.startLongitude || 0], [tracking?.endLatitude || 0, tracking?.endLongitude || 0]);

        this.rideTrackingService.connectToRideTracking(this.rideId!);
        this.rideTrackingService.liveRideInfo$.subscribe(data => {
          const [ lat, lng, remainingDuration, price ] = data;
          this.currentPrice = price;
          this.etaMin = remainingDuration;
          this.eta = this.calculateETA(remainingDuration);
          this.mapComponent?.updateVehiclePosition([lat, lng]);
          this.cdr.detectChanges();
        });
      });
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

  ngOnDestroy(): void {
    this.rideTrackingService.disconnect();
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
    if (!this.selectedRide) return;

    const payload = {
      rideId: this.rideId,
      locationId: 1, // TODO: Replace with real locationId
      userType: this.isDriver ? 'DRIVER' : 'REGISTERED_USER',
      accountEmail: this.currentUser?.email
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
