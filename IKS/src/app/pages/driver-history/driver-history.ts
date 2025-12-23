import { Component } from '@angular/core';

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
  passenger?: { name: string; phone: string; address: string };
}

@Component({
  selector: 'app-driver-history',
  imports: [],
  templateUrl: './driver-history.html',
  styleUrl: './driver-history.css',
})
export class DriverHistory {
  rides: Ride[] = [
    {
      id: 1,
      date: '14 March 2025',
      startTime: '15:32',
      endTime: '16:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,480 RSD',
      status: 'Completed'
    },
    {
      id: 2,
      date: '14 March 2025',
      startTime: '14:32',
      endTime: '15:05',
      from: 'Stražilovska',
      to: 'Bulevar Kralja Petra I',
      price: '1,480 RSD',
      status: 'Canceled'
    }
  ]

  selectedRide: Ride | null = this.rides[0];

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }
}
