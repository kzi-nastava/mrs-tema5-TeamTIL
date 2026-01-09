import { Component } from '@angular/core';
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
import { FormsModule } from '@angular/forms';

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
    FormsModule
  ],
  templateUrl: './user-ride-history.html',
  styleUrl: './user-ride-history.css',
})
export class UserRideHistory {
  filterOptions = ['Last 7 days', 'Last month', 'Completed only', 'Cancelled only'];
  activeFilter = 'Last 7 days';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

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
