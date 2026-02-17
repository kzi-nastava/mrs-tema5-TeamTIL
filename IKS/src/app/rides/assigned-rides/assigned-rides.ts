import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { RideService } from '../services/ride.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

interface Ride {
  id: number;
  startTime: string;
  approximatedEndTime: string;
  from: string;
  to: string;
  price: string;
  distance: string;
  duration: string;
  status: 'In progress' | 'Upcoming' | 'Completed' | string;
  date: string;
  nextRideIn?: string;
  passenger: { name: string; phone: string };
}

type TabFilter = 'Today' | 'Next 7 days' | 'All upcoming';

@Component({
  selector: 'app-assigned-rides',
  imports: [CommonModule, FormsModule],
  templateUrl: './assigned-rides.html',
  styleUrl: './assigned-rides.css',
})
export class AssignedRides implements OnInit, OnDestroy {
    showCancelModal = false;
    cancelReason = '';
    rideToCancel: Ride | null = null;
    tabs: TabFilter[] = ['Today', 'Next 7 days', 'All upcoming'];
    activeTab: TabFilter = 'Today';
    rides: Ride[] = [];
    selectedRide: Ride | null = null;
    private subscriptions: Subscription = new Subscription();
    
    constructor(private rideService: RideService, private authService: AuthService, private cdr: ChangeDetectorRef, private router: Router) {}

    openCancelModal(ride: Ride) {
      this.rideToCancel = ride;
      this.cancelReason = '';
      this.showCancelModal = true;
    }

    closeCancelModal() {
      this.showCancelModal = false;
      this.rideToCancel = null;
      this.cancelReason = '';
    }

    confirmCancelRide() {
      if (!this.rideToCancel) return;
      this.rideService.cancelRide(this.rideToCancel.id, this.cancelReason).subscribe({
        next: () => {
          this.closeCancelModal();
          this.loadRides();
        },
        error: (err: any) => {
          alert('Greška pri otkazivanju vožnje!');
        }
      });
    }

    ngOnInit(): void {
      const userSub = this.authService.currentUser$.subscribe(user => {
        if (user && user.userType === 'DRIVER') {
          this.loadRides();
        } else {
          this.rides = [];
          this.cdr.detectChanges();
        }
      });
      
      this.subscriptions.add(userSub);
    }

    private loadRides(): void {
      const currentUserSub = this.authService.currentUser$.subscribe(user => {
        if (!user || !user.email) return;
        
        // /assigned endpoint već vraća samo aktivne voznje (IN_PROGRESS, REQUESTED)
        const ridesSub = this.rideService.getActiveAssignedRides(user.email).subscribe({
          next: (ridesFromBackend) => {
            console.log('[AssignedRides] Active assigned rides loaded:', ridesFromBackend);
            this.formatAndDisplayRides(ridesFromBackend);
          },
          error: (error) => {
            console.error('[AssignedRides] Error fetching active rides:', error);
            this.rides = [];
            this.cdr.detectChanges();
          }
        });
        
        this.subscriptions.add(ridesSub);
      });
      
      this.subscriptions.add(currentUserSub);
    }

    private formatAndDisplayRides(ridesFromBackend: any[]): void {
      this.rides = ridesFromBackend.map((ride: any) => {
        // Formatiranje datuma/vremena
        let formattedDate = '';
        let formattedStartTime = '';
        if (ride.startTime) {
          try {
            const dateObj = new Date(ride.startTime);
            formattedDate = dateObj.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
            formattedStartTime = dateObj.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
          } catch {}
        }
        // Formatiranje distance
        let formattedDistance = '-';
        if (ride.distance !== undefined && ride.distance !== null) {
          formattedDistance = Number(ride.distance).toFixed(2) + ' km';
        }
        // Formatiranje cene
        let formattedPrice = '-';
        if (ride.price !== undefined && ride.price !== null) {
          formattedPrice = Math.round(Number(ride.price)) + ' RSD';
        }
        // Formatiranje trajanja
        let formattedDuration = '-';
        if (ride.duration !== undefined && ride.duration !== null) {
          formattedDuration = Number(ride.duration).toFixed(2);
        }
        // Formatiranje estimated end time
        let formattedEndTime = '';
        if (ride.estimatedEndTime) {
          try {
            const endDateObj = new Date(ride.estimatedEndTime);
            formattedEndTime = endDateObj.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) + ', ' + endDateObj.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
          } catch {}
        }
        // Formatiranje statusa
        let status = 'Upcoming';
        const statusValue = ride.status?.toUpperCase();
        if (statusValue === 'IN_PROGRESS' || statusValue === 'ONGOING') status = 'In progress';
        else if (statusValue === 'COMPLETED') status = 'Completed';
        else if (statusValue === 'CANCELLED') status = 'Cancelled';
        else if (statusValue === 'REQUESTED' || statusValue === 'PENDING') status = 'Requested';
        // Passenger
        let passengerName = ride.passengerName || ride.passengerEmail || '-';
        let passengerPhone = ride.passengerPhone || '';
        return {
          id: ride.id,
          from: ride.startLocation || '-',
          to: ride.endLocation || '-',
          price: formattedPrice,
          status: status,
          date: formattedDate,
          startTime: formattedStartTime,
          approximatedEndTime: formattedEndTime,
          distance: formattedDistance,
          duration: formattedDuration,
          nextRideIn: ride.nextRideIn || '',
          passenger: { name: passengerName, phone: passengerPhone }
        };
      });
      this.cdr.detectChanges();
    }

    setActiveTab(tab: TabFilter) {
      this.activeTab = tab;
      // TODO: Filter rides based on selected tab
    }

    selectRide(ride: Ride) {
      this.selectedRide = ride;
    }

    openRide(ride: Ride) {
      console.log('Opening ride:', ride);
      if (!ride) return;
      this.router.navigate(['/track-ride', ride.id]);
    }

    startRide(ride: Ride) {
  this.rideService.startRide(ride.id).subscribe({
    next: () => {
      alert('Ride started successfully!');
      this.router.navigate(['/track-ride', ride.id]);
    },
    error: (err: any) => {
      alert('Failed to start ride: ' + (err?.error?.error || 'error'));
    }
  });
}

  cancelRide(ride: Ride) {
    console.log('Canceling ride:', ride);
    // TODO: Cancel ride logic
  }

  pauseRide(ride: Ride) {
    if (!ride) return;
    const stopRequest = {
      rideId: ride.id,
      actualEndLocation: {
        latitude: 45.2671,
        longitude: 19.8335,
        address: 'Trg slobode 1, Novi Sad'
      },
      actualEndTime: new Date().toISOString()
    };
    this.rideService.stopRide(ride.id, stopRequest).subscribe({
      next: () => {
        alert('Ride stopped successfully!');
        this.loadRides();
      },
      error: () => {
        alert('Failed to stop ride!');
      }
    });
  }

  endRide(ride: Ride) {
    if (!ride) return;

    const requestPayload = {
      actualEndLocation: {
        latitude: 45.2671,
        longitude: 19.8335,
        address: 'Trg slobode 1, Novi Sad'
      },
      actualEndTime: new Date().toISOString()
    };

    this.rideService.endRide(ride.id, requestPayload).subscribe({
      next: (response: any) => {
        this.selectedRide = null;

        if (response.hasNextRide) {
          alert(`Ride ended successfully! Next ride: ${response.nextRideFrom} → ${response.nextRideTo} at ${response.nextRideScheduledTime}`);
          this.loadRides(); // Učitaj novu vožnju
        } else {
          alert(`Ride ended successfully! Price: ${response.finalPrice} RSD, Duration: ${response.duration}`);
          this.loadRides();
        }
      },
      error: (err) => {
        console.error(err);
        alert('Failed to end ride!');
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
