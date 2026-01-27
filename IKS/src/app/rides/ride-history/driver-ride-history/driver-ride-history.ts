import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
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
import { AuthService } from '../../../services/auth.service';

interface Ride {
  id: number;
  startTime: string;
  endTime: string;
  from: string;
  to: string;
  price: string;
  status: 'Completed' | 'Canceled';
  date: string;
  canceledBy?: 'Driver' | 'Passenger';
  panicActivated?: boolean;
  duration?: string;
  distance?: string;
  passengers?: { name: string; phone: string }[];
}

@Component({
  selector: 'app-driver-ride-history',
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
  templateUrl: './driver-ride-history.html',
  styleUrl: './driver-ride-history.css',
})
export class DriverHistory {
  filterOptions = ['All','Last 7 days', 'Last month', 'Completed only', 'Canceled only'];
  activeFilter: string = this.filterOptions[0];
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

  allRides: Ride[] = [];
  rides: Ride[] = [];
  selectedRide: Ride | null = null;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef, private authService: AuthService) { }
  
  ngOnInit(): void {
    this.loadRides();
  }

  loadRides() {
    const driverEmail = this.authService.getEmail();
    this.http.get<Ride[]>(`http://localhost:8080/api/rides/driver/history?driverEmail=${driverEmail}`)
      .subscribe({
        next: (data) => {
          this.allRides = data;
          this.rides = [...this.allRides];
          this.selectedRide = this.rides[0] ?? null;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error fetching rides:', err)
      });
  }

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }

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
        this.selectedStatus = 'Completed';
        this.applyFilters();
        break;
        
      case 'Canceled only':
        this.selectedStatus = 'Canceled';
        this.applyFilters();
        break;
    }
  }

  applyFilters() {
    let filtered = [...this.allRides];

    if (this.dateFrom || this.dateTo) {
      filtered = filtered.filter(ride => {
        const rideDateParts = ride.date.split(' ');
        const months: { [key: string]: number } = {
          'January': 0, 'February': 1, 'March': 2, 'April': 3,
          'May': 4, 'June': 5, 'July': 6, 'August': 7,
          'September': 8, 'October': 9, 'November': 10, 'December': 11
        };
        const day = parseInt(rideDateParts[0]);
        const month = months[rideDateParts[1]];
        const year = parseInt(rideDateParts[2]);

        const timeParts = ride.startTime.split(':');
        const hours = parseInt(timeParts[0]);
        const minutes = parseInt(timeParts[1]);

        const rideDate = new Date(year, month, day, hours, minutes);

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

    if (this.selectedStatus) {
      filtered = filtered.filter(ride => 
        ride.status.toLowerCase() === this.selectedStatus.toLowerCase()
      );
    }
    
    this.rides = filtered;
    
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
