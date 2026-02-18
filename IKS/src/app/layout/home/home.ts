import { Component, ViewChild, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MapView } from '../../map/map';
import { RideService } from '../../rides/services/ride.service';
import { AuthService } from '../../services/auth.service';
import { PanicService } from '../../services/panic.service';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { PublicService } from '../../services/public.service';

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
  private vehicleMarkers: Map<string, L.Marker> = new Map();

  private fetchVehicles(): void {
    this.publicService.getAvailableVehicles().subscribe({
      next: (data) => {
        this.mapComponent?.updateVehicleMarkers(data, this.vehicleMarkers);
      },
      error: (err) => console.error('Error fetching vehicles:', err)
    });
  }

  endRide() {
    if (!this.userRide) return;

    const requestPayload = {
      actualEndLocation: {
        latitude: 45.2671,
        longitude: 19.8335,
        address: 'Trg slobode 1, Novi Sad'
      },
      actualEndTime: new Date().toISOString()
    };

    this.rideService.endRide(this.userRide.id, requestPayload).subscribe({
      next: (response: any) => {
        this.showRideCard = false;
        this.userRide = null;
        this.showForm = true;
        this.cdr.detectChanges();

        if (response.hasNextRide) {
          alert(`Ride ended successfully! Next ride: ${response.nextRideFrom} → ${response.nextRideTo} at ${response.nextRideScheduledTime}`);
          this.router.navigate(['/assigned-rides']);
        } else {
          alert(`Ride ended successfully! Price: ${response.finalPrice} RSD, Duration: ${response.duration}`);
          this.router.navigate(['/assigned-rides']);
        }
      },
      error: (err) => {
        console.error(err);
        alert('Failed to end ride!');
      }
    });
  }

      stopRide() {
        if (!this.userRide) return;
        // For demo, use dummy location and time. Replace with real data if available.
        const stopRequest = {
          rideId: this.userRide.id,
          actualEndLocation: {
            latitude: 45.2671,
            longitude: 19.8335,
            address: 'Trg slobode 1, Novi Sad'
          },
          actualEndTime: new Date().toISOString()
        };
        this.rideService.stopRide(this.userRide.id, stopRequest).subscribe({
          next: () => {
            alert('Ride stopped successfully!');
            // Optionally update UI or refetch ride status
          },
          error: () => {
            alert('Failed to stop ride!');
          }
        });
      }

      quickCancelRide() {
        if (!this.userRide) {
          console.error('[Home] No ride to cancel');
          return;
        }
        
        console.log('[Home] Registered user cancelling ride:', this.userRide.id);
        
        this.rideService.cancelRide(this.userRide.id, 'User cancelled').subscribe({
          next: (response) => {
            console.log('[Home] Ride cancelled successfully:', response);
            alert('Ride cancelled successfully!');
            this.userRide = null;
            this.showRideCard = false;
            this.showForm = true;
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('[Home] Error cancelling ride:', error);
            console.error('[Home] Error status:', error.status);
            console.error('[Home] Error message:', error.message);
            alert(`Failed to cancel ride: ${error.message || 'Unknown error'}`);
            this.cdr.detectChanges();
          }
        });
      }

      confirmCancelRide() {
        if (!this.userRide) {
          console.error('[Home] No ride to cancel');
          return;
        }
        
        console.log('[Home] Driver cancelling ride:', this.userRide.id, 'with reason:', this.cancelReason);
        
        this.rideService.cancelRide(this.userRide.id, this.cancelReason).subscribe({
          next: (response) => {
            console.log('[Home] Ride cancelled successfully:', response);
            alert('Ride cancelled successfully!');
            this.showCancelForm = false;
            this.cancelReason = '';
            this.userRide = null;
            this.showRideCard = false;
            this.showForm = true;
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('[Home] Error cancelling ride:', error);
            console.error('[Home] Error status:', error.status);
            console.error('[Home] Error message:', error.message);
            alert(`Failed to cancel ride: ${error.message || 'Unknown error'}`);
            this.showCancelForm = false;
            this.cancelReason = '';
            this.cdr.detectChanges();
          }
        });
      }
      onCancelRide() {
        if (!this.userRide) return;
        if (this.currentUser?.userType === 'DRIVER') {
          this.showCancelForm = true;
          return;
        }
        // For user, instant cancel
        const reason = 'Cancelled by user';
        this.rideService.cancelRide(this.userRide.id, reason).subscribe({
          next: () => {
            alert('Ride cancelled successfully!');
            this.userRide = null;
            this.showRideCard = false;
            this.showForm = true;
            this.cdr.detectChanges();
          },
          error: () => {
            alert('Failed to cancel ride!');
          }
        });
      }
    showPanicToast = false;
  @ViewChild(MapView) mapComponent?: MapView;
  pickupLocation = '';
  destination = '';
  vehicleType = 'STANDARD';
  showEstimateButton = false;
  showForm = true;

  userRide: RideCard | null = null;
  currentUser: any = null;
  isDriver: boolean = false;
  isPassenger: boolean = false;
  showRideCard = false;
  isLoggedIn = false;
  showCancelForm = false;
  cancelReason = '';
  
  private subscriptions: Subscription = new Subscription();

  constructor(
    private rideService: RideService,
    private authService: AuthService,
    private panicService: PanicService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private router: Router,
    private notificationService: NotificationService,
    private publicService: PublicService
  ) {}
  
  onPanicClick() {
    if (!this.userRide) return;

    const payload = {
      rideId: this.userRide.id,
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

  ngOnInit(): void {
    this.fetchVehicles();
    const userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      console.log('[DEBUG] currentUser', user);
      this.isLoggedIn = !!user && !!user.email;
      this.isDriver = user?.userType === 'DRIVER';
      this.isPassenger = user?.userType === 'REGISTERED_USER';
      if (
        this.isLoggedIn && user && user.email &&
        (user.userType === 'DRIVER' || user.userType === 'REGISTERED_USER')
      ) {
        // Dobija samo aktivne voznje (IN_PROGRESS, REQUESTED) - backend filtrira
        const rideObservable =
          user.userType === 'DRIVER'
            ? this.rideService.getActiveAssignedRides(user.email)
            : this.rideService.getActiveUserRides(user.email);

        const rideSub = rideObservable.subscribe({
          next: (rides) => {
            console.log('[DEBUG] active rides for user', rides);
            this.processRides(rides);
          },
          error: (error) => {
            console.error('[Home] Error fetching active rides:', error);
            // Ako endpoint ne postoji, fallback na stari sa filtriranjem
            let fallbackObservable =
              user.userType === 'DRIVER'
                ? this.rideService.getAssignedRides(user.email)
                : this.rideService.getUserRides(user.email);
            
            const fallbackSub = fallbackObservable.subscribe({
              next: (rides) => {
                console.log('[DEBUG] all rides (fallback), filtering active...');
                const activeRides = rides.filter((r: any) => {
                  const status = r.status?.toUpperCase();
                  return status === 'IN_PROGRESS' || status === 'ONGOING' || 
                         status === 'REQUESTED' || status === 'PENDING' || status === 'ACCEPTED';
                });
                this.processRides(activeRides);
              },
              error: (fallbackError) => {
                console.error('[Home] Error fetching rides (fallback failed):', fallbackError);
                this.userRide = null;
                this.showRideCard = true;
                this.showForm = false;
                this.cdr.detectChanges();
              }
            });
            
            this.subscriptions.add(fallbackSub);
          }
        });

        this.subscriptions.add(rideSub);
      } else {
        // Prikaz za gosta: samo forma za procenu vožnje
        this.userRide = null;
        this.showRideCard = false;
        this.showForm = true;
        this.cdr.detectChanges();
      }
    });

    const rideFinishedSub = this.notificationService.rideFinished$.subscribe(() => {
      if (this.currentUser?.email && this.currentUser?.userType === 'REGISTERED_USER') {
        this.userRide = null;
        this.showRideCard = true;
        this.showForm = false;
        this.cdr.detectChanges();
        
        this.rideService.getActiveUserRides(this.currentUser.email).subscribe({
          next: (rides) => {
            this.processRides(rides);
          },
          error: () => {
            this.cdr.detectChanges();
          }
        });
      }
    });

    this.subscriptions.add(rideFinishedSub);
    
    this.subscriptions.add(userSub);
  }

  private processRides(rides: any[]): void {
    let rideToShow = null;
    // Check for in-progress rides (support multiple status values)
    const inProgress = rides.find((r: any) => 
      r.status === 'IN_PROGRESS' || 
      r.status === 'ONGOING' || 
      r.status === 'In progress'
    );
    if (inProgress) {
      rideToShow = inProgress;
    } else {
      // If no in-progress ride, look for requested/pending rides
      const requestedRides = rides.filter((r: any) => 
        r.status === 'REQUESTED' || 
        r.status === 'PENDING' ||
        r.status === 'Requested' ||
        r.status === 'ACCEPTED'
      );
      if (requestedRides.length > 0) {
        rideToShow = requestedRides.sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0];
      }
    }
    if (rideToShow) {
      // Map status values to display format
      const isInProgress = 
        rideToShow.status === 'IN_PROGRESS' || 
        rideToShow.status === 'ONGOING' || 
        rideToShow.status === 'In progress';
      this.userRide = {
        id: rideToShow.id,
        startTime: rideToShow.startTime,
        from: rideToShow.startLocation,
        to: rideToShow.endLocation,
        status: isInProgress ? 'In progress' : 'Requested',
      };
    } else {
      this.userRide = null;
    }
    this.showRideCard = true;
    this.showForm = false;
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onInputChange() {
    this.showEstimateButton = !!this.pickupLocation && !!this.destination;
  }

  estimateRideTime() {
    this.showForm = false;
    this.mapComponent?.setPickupDestinationAndVehicleType(
      this.pickupLocation,
      this.destination,
      this.vehicleType
    );
    this.mapComponent?.estimateRideTime();
  }

  onMapClick() {
    this.showForm = true;
  }

  openRide() {
    if (!this.userRide) return;
    this.router.navigate(['/track-ride', this.userRide.id]);
  }
}
