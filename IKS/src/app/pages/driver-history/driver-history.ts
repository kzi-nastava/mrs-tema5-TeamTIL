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
      from: 'Stražilovska 32',
      to: 'Bulevar Kralja Petra 7',
      price: '1,480 RSD',
      status: 'Completed',
      duration: '33 min',
      passenger: { name: 'Marko Marković', phone: '+381 64 1234567', address: 'Stražilovska 32' },
    },
    {
      id: 2,
      date: '14 March 2025',
      startTime: '14:32',
      endTime: '14:52',
      from: 'Gospodska 10',
      to: 'Jevrejska 2',
      price: '1,250 RSD',
      status: 'Canceled',
      duration: '20 min',
      passenger: { name: 'Jelena Jovanović', phone: '+381 65 7654321', address: 'Gospodska 10' },
    }
  ]

  selectedRide: Ride | null = this.rides[0];

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }

  filterOptions = ['Last 7 days', 'Last month', 'Completed only', 'Canceled only'];
  
  activeFilter: string = this.filterOptions[0];

  setActiveFilter(filter: string) {
    this.activeFilter = filter;
  }
}
