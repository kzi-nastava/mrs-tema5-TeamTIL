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
  filterOptions = ['All', 'Last 7 days', 'Last month', 'Completed only', 'Cancelled only'];
  activeFilter = 'All';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

  allRides: Ride[] = [
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
    {
      id: 4,
      date: '10 March 2025',
      startTime: '10:15',
      endTime: '10:45',
      from: 'Futoška',
      to: 'Trg Slobode',
      price: '980',
      status: 'Completed',
      duration: '30 min',
      distance: '5.5 km',
      driver: { name: 'Jane Smith', phone: '+381 125 456 780' },
    },
    {
      id: 5,
      date: '5 March 2025',
      startTime: '18:20',
      endTime: '18:55',
      from: 'Bulevar Oslobođenja',
      to: 'Petrovaradin',
      price: '1,250',
      status: 'Canceled',
      duration: '35 min',
      distance: '7.8 km',
      driver: { name: 'Mike Johnson', phone: '+381 125 456 781' },
    },
  ];

  rides: Ride[] = [...this.allRides];
  selectedRide: Ride | null = this.rides[0];

  setActiveFilter(option: string) {
    this.activeFilter = option;
    
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    
    switch (option) {
      case 'All':
        this.resetFilters();
        break;
        
      case 'Last 7 days':
        const sevenDaysAgo = new Date(today);
        sevenDaysAgo.setDate(today.getDate() - 7);
        this.dateFrom = sevenDaysAgo;
        this.dateTo = today;
        this.selectedStatus = '';
        this.applyFilters();
        break;
        
      case 'Last month':
        const thirtyDaysAgo = new Date(today);
        thirtyDaysAgo.setDate(today.getDate() - 30);
        this.dateFrom = thirtyDaysAgo;
        this.dateTo = today;
        this.selectedStatus = '';
        this.applyFilters();
        break;
        
      case 'Completed only':
        this.selectedStatus = 'completed';
        this.applyFilters();
        break;
        
      case 'Cancelled only':
        this.selectedStatus = 'canceled';
        this.applyFilters();
        break;
    }
  }

  applyFilters() {
    let filtered = [...this.allRides];
    
    // Filter by date range
    if (this.dateFrom || this.dateTo) {
      filtered = filtered.filter(ride => {
        const rideDate = this.parseRideDate(ride.date);
        
        if (this.dateFrom && this.dateTo) {
          return rideDate >= this.dateFrom && rideDate <= this.dateTo;
        } else if (this.dateFrom) {
          return rideDate >= this.dateFrom;
        } else if (this.dateTo) {
          return rideDate <= this.dateTo;
        }
        return true;
      });
    }
    
    // Filter by status
    if (this.selectedStatus) {
      filtered = filtered.filter(ride => 
        ride.status.toLowerCase() === this.selectedStatus.toLowerCase()
      );
    }
    
    this.rides = filtered;
    
    // Update selected ride if it's not in filtered results
    if (this.selectedRide && !this.rides.find(r => r.id === this.selectedRide?.id)) {
      this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
    }
  }

  parseRideDate(dateString: string): Date {
    // Parse "14 March 2025" format
    const months: { [key: string]: number } = {
      'January': 0, 'February': 1, 'March': 2, 'April': 3, 'May': 4, 'June': 5,
      'July': 6, 'August': 7, 'September': 8, 'October': 9, 'November': 10, 'December': 11
    };
    
    const parts = dateString.split(' ');
    const day = parseInt(parts[0]);
    const month = months[parts[1]];
    const year = parseInt(parts[2]);
    
    return new Date(year, month, day);
  }

  resetFilters() {
    this.dateFrom = null;
    this.dateTo = null;
    this.selectedStatus = '';
    this.activeFilter = 'All';
    this.rides = [...this.allRides];
    
    // Keep selected ride if still in results
    if (this.selectedRide && !this.rides.find(r => r.id === this.selectedRide?.id)) {
      this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
    }
  }

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }

  getUniqueDates(): string[] {
    const dates = new Set(this.rides.map(ride => ride.date));
    return Array.from(dates).sort((a, b) => {
      return this.parseRideDate(b).getTime() - this.parseRideDate(a).getTime();
    });
  }

  getRidesByDate(date: string): Ride[] {
    return this.rides.filter(ride => ride.date === date);
  }
}
