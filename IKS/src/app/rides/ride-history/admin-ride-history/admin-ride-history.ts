import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
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
import { RideService } from '../../services/ride.service';

interface Ride {
  id: number;
  startTime: string;
  endTime: string;
  from: string;
  to: string;
  price: string;
  status: 'Completed' | 'Canceled' | 'Panic';
  date: string;
  duration?: string;
  distance?: string;
  driver?: { name: string; phone: string };
  passenger?: { name: string; phone: string };
  hasPanic?: boolean;
}

@Component({
  selector: 'app-admin-ride-history',
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
  templateUrl: './admin-ride-history.html',
  styleUrl: './admin-ride-history.css',
})
export class AdminRideHistory implements OnInit {
      constructor(private rideService: RideService) {}
    ngOnInit(): void {
      this.rideService.getAdminRideHistory().subscribe({
        next: (ridesFromBackend) => {
          this.allRides = ridesFromBackend.map(ride => ({
            id: ride.id,
            date: ride.startTime?.split(',')[0]?.trim() || '-',
            startTime: ride.startTime?.split(',')[1]?.trim() || '-',
            endTime: ride.estimatedEndTime?.split(',')[1]?.trim() || '-',
            from: ride.startLocation || '-',
            to: ride.endLocation || '-',
            price: ride.price ? ride.price.toFixed(0) : '-',
            status: ride.status === 'FINISHED' ? 'Completed' : (ride.status === 'CANCELED' ? 'Canceled' : ride.status),
            duration: ride.duration ? `${Math.round(ride.duration)} min` : '-',
            distance: ride.distance ? `${ride.distance.toFixed(1)} km` : '-',
            hasPanic: ride.panicSent || false,
            driver: { name: ride.driverEmail || '-', phone: '-' },
            passenger: { name: ride.passengerEmail || '-', phone: '-' }
          }));
          this.rides = [...this.allRides];
          this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
        },
        error: (err) => {
          console.error('Error fetching admin ride history:', err);
        }
      });
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

    selectRide(ride: Ride) {
      this.selectedRide = ride;
    }

    resetFilters() {
      this.dateFrom = null;
      this.dateTo = null;
      this.selectedStatus = '';
      this.activeFilter = 'All';
      this.rides = [...this.allRides];
      if (this.selectedRide && !this.rides.find(r => r.id === this.selectedRide?.id)) {
        this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
      }
    }

    parseRideDate(dateString: string): Date {
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
  filterOptions = ['All', 'Last 7 days', 'Last month', 'Completed only', 'Cancelled only', 'PANIC'];
  activeFilter = 'All';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

  allRides: Ride[] = [];
  rides: Ride[] = [];
  selectedRide: Ride | null = null;

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
        
      case 'PANIC':
        this.dateFrom = null;
        this.dateTo = null;
        this.selectedStatus = '';
        this.rides = this.allRides.filter(ride => ride.hasPanic === true);
        if (this.selectedRide && !this.rides.find(r => r.id === this.selectedRide?.id)) {
          this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
        }
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
      filtered = filtered.filter(ride => {
        if (this.selectedStatus === 'completed') {
          return ride.status === 'Completed';
        } else if (this.selectedStatus === 'canceled') {
          return ride.status === 'Canceled';
        }
        return true;
      });
    }
    
    this.rides = filtered;
    
    // Update selected ride if it's not in filtered results
    if (this.selectedRide && !this.rides.find(r => r.id === this.selectedRide?.id)) {
      this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
    }
  }
}
