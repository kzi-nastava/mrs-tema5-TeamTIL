import { Component, OnInit } from '@angular/core';
import { RideService } from '../services/ride.service';
import { AuthService } from '../../services/auth.service';

interface Ride {
  id: number;
  startTime: string;
  approximatedEndTime: string;
  from: string;
  to: string;
  price: string;
  distance: string;
  duration: string;
  status: 'In progress' | 'Upcoming';
  date: string;
  nextRideIn?: string;
  passenger: { name: string; phone: string };
}

type TabFilter = 'Today' | 'Next 7 days' | 'All upcoming';

@Component({
  selector: 'app-assigned-rides',
  imports: [],
  templateUrl: './assigned-rides.html',
  styleUrl: './assigned-rides.css',
})
export class AssignedRides implements OnInit {
  tabs: TabFilter[] = ['Today', 'Next 7 days', 'All upcoming'];
  activeTab: TabFilter = 'Today';
  
  rides: Ride[] = [];
  constructor(private rideService: RideService, private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userType === 'DRIVER') {
        this.rideService.getAssignedRides(user.email).subscribe({
          next: (ridesFromBackend) => {
            this.rides = ridesFromBackend.map((ride: any) => ({
              id: ride.id,
              from: ride.startLocation || '-',
              to: ride.endLocation || '-',
              price: ride.price || '-',
              status: ride.status === 'IN_PROGRESS' ? 'In progress' : 'Upcoming',
              date: ride.date || '',
              startTime: ride.startTime || '',
              approximatedEndTime: ride.approximatedEndTime || '',
              distance: ride.distance || '-',
              duration: ride.duration || '-',
              nextRideIn: ride.nextRideIn || '',
              passenger: { name: ride.passengerEmail || '-', phone: ride.passengerPhone || '' }
            }));
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
