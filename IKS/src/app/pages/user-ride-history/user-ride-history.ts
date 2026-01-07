import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Ride {
  id: number;
  startTime: string;
  endTime: string;
  from: string;
  to: string;
  price: string;
  status: 'Completed' | 'Canceled';
  date: string;
  duration?: string;
  distance?: string;
  driver?: { name: string; phone: string };
}

@Component({
  selector: 'app-user-ride-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-ride-history.html',
  styleUrl: './user-ride-history.css',
})
export class UserRideHistory {
  filterOptions = ['Last 7 days', 'Last month', 'Completed only', 'Cancelled only'];
  activeFilter = 'Last 7 days';

  rides: Ride[] = [
    {
      id: 1,
      date: '14 March 2025',
      startTime: '15:32',
      endTime: '16:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,480',
      status: 'Completed',
      duration: '33 min',
      distance: '8.2 km',
      driver: { name: 'John Pork', phone: '+381 125 456 789' },
    },
    {
      id: 2,
      date: '14 March 2025',
      startTime: '14:32',
      endTime: '15:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,480',
      status: 'Canceled',
      duration: '33 min',
      distance: '8.2 km',
      driver: { name: 'John Pork', phone: '+381 125 456 789' },
    },
    {
      id: 3,
      date: '12 March 2025',
      startTime: '15:32',
      endTime: '16:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,480',
      status: 'Completed',
      duration: '33 min',
      distance: '8.2 km',
      driver: { name: 'John Pork', phone: '+381 125 456 789' },
    },
  ];

  selectedRide: Ride | null = this.rides[0];

  setActiveFilter(option: string) {
    this.activeFilter = option;
  }

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }
}
