import { Component, OnInit } from '@angular/core';
import { RideService } from '../services/ride.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
export class AssignedRides implements OnInit {
    showCancelModal = false;
    cancelReason = '';
    rideToCancel: Ride | null = null;
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
          this.ngOnInit(); // refresh rides
        },
        error: (err: any) => {
          alert('Greška pri otkazivanju vožnje!');
        }
      });
    }
  tabs: TabFilter[] = ['Today', 'Next 7 days', 'All upcoming'];
  activeTab: TabFilter = 'Today';
  
  rides: Ride[] = [];
  constructor(private rideService: RideService, private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userType === 'DRIVER') {
        this.rideService.getAssignedRides(user.email).subscribe({
          next: (ridesFromBackend) => {
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
              if (ride.status === 'IN_PROGRESS') status = 'In progress';
              else if (ride.status === 'COMPLETED') status = 'Completed';
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
          },
          error: (err) => console.error('Greška pri dohvatanju vožnji:', err)
        });
      }
    });
  }

  selectedRide: Ride | null = null;

  setActiveTab(tab: TabFilter) {
    this.activeTab = tab;
    // TODO: Filter rides based on selected tab
  }

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }

  openRide(ride: Ride) {
    console.log('Opening ride:', ride);
    // TODO: Navigate to ride tracking/map view
  }

  startRide(ride: Ride) {
    console.log('Starting ride:', ride);
    // TODO: Start ride logic
  }

  cancelRide(ride: Ride) {
    console.log('Canceling ride:', ride);
    // TODO: Cancel ride logic
  }

  pauseRide(ride: Ride) {
    console.log('Pausing ride:', ride);
    // TODO: Pause ride logic
  }

  endRide(ride: Ride) {
    console.log('Ending ride:', ride);
    // TODO: End ride logic
  }
}
