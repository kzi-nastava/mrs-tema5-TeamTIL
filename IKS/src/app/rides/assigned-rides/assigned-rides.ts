import { Component } from '@angular/core';

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
export class AssignedRides {
  tabs: TabFilter[] = ['Today', 'Next 7 days', 'All upcoming'];
  activeTab: TabFilter = 'Today';
  
  rides: Ride[] = [
    {
      id: 1,
      date: '23 Dec 2025',
      startTime: '15:32',
      approximatedEndTime: '16:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,230',
      distance: '7.5',
      duration: '28 min',
      status: 'In progress',
      passenger: { name: 'John Doe', phone: '+381 123 456 789' },
    },
    {
      id: 2,
      date: '23 Dec 2025',
      startTime: '16:15',
      approximatedEndTime: '16:45',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,230',
      distance: '7.5',
      duration: '28 min',
      status: 'Upcoming',
      nextRideIn: '15 minutes',
      passenger: { name: 'John Doe', phone: '+381 123 456 789' },
    },
    {
      id: 3,
      date: '23 Dec 2025',
      startTime: '17:32',
      approximatedEndTime: '18:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,230',
      distance: '7.5',
      duration: '28 min',
      status: 'Upcoming',
      passenger: { name: 'John Doe', phone: '+381 123 456 789' },
    },
  ];

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
