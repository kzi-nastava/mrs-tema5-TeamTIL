import { Component, ChangeDetectorRef } from '@angular/core';
import { OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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
  driver?: { 
    name: string; 
    phone: string;
    firstName?: string;
    lastName?: string;
    profilePictureUrl?: string;
  };
  passenger?: { 
    name: string; 
    phone: string;
    firstName?: string;
    lastName?: string;
    profilePictureUrl?: string;
  };
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
  filterOptions = ['All', 'Last 7 days', 'Last month', 'Completed only', 'Canceled only', 'PANIC'];
  activeFilter = 'All';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

  allRides: Ride[] = [];
  rides: Ride[] = [];
  selectedRide: Ride | null = null;

  constructor(private rideService: RideService, private router: Router, private cdr: ChangeDetectorRef) {}
  
  ngOnInit(): void {
      this.rideService.getAdminRideHistory().subscribe({
        next: (ridesFromBackend) => {
          console.log('Backend response:', ridesFromBackend);
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
            driver: { 
              name: ride.driverFirstName && ride.driverLastName 
                ? `${ride.driverFirstName} ${ride.driverLastName}` 
                : ride.driverEmail || '-',
              phone: ride.driverPhoneNumber || '-',
              firstName: ride.driverFirstName,
              lastName: ride.driverLastName,
              profilePictureUrl: ride.driverProfilePictureUrl
            },
            passenger: { 
              name: ride.passengerFirstName && ride.passengerLastName 
                ? `${ride.passengerFirstName} ${ride.passengerLastName}` 
                : ride.passengerEmail || '-',
              phone: ride.passengerPhoneNumber || '-',
              firstName: ride.passengerFirstName,
              lastName: ride.passengerLastName,
              profilePictureUrl: ride.passengerProfilePictureUrl
            }
          }));
          this.rides = [...this.allRides];
          this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
          this.cdr.detectChanges();
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

    viewFullRideDetails(ride: Ride) {
      this.router.navigate(['/ride-details', ride.id]);
    }

    onImageError(event: Event) {
      const imgElement = event.target as HTMLImageElement;
      imgElement.style.display = 'none';
      const iconElement = imgElement.nextElementSibling as HTMLElement;
      if (iconElement) {
        iconElement.style.display = 'flex';
      }
    }

    formatPhoneNumber(phone: string): string {
      if (!phone || phone === '-') return phone;
      
      const digitsOnly = phone.replace(/\D/g, '');
      const length = digitsOnly.length;
      
      if (length <= 3) return digitsOnly;
      
      const remainder = length % 3;
      
      if (remainder === 1) {
        const groups: string[] = [];
        let i = 0;
        while (i < length - 4) {
          groups.push(digitsOnly.slice(i, i + 3));
          i += 3;
        }
        groups.push(digitsOnly.slice(i));
        return groups.join(' ');
      } else {
        const groups: string[] = [];
        for (let i = 0; i < length; i += 3) {
          groups.push(digitsOnly.slice(i, i + 3));
        }
        return groups.join(' ');
      }
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
      const shortMonths: { [key: string]: number } = {
        'Jan': 0, 'Feb': 1, 'Mar': 2, 'Apr': 3, 'May': 4, 'Jun': 5,
        'Jul': 6, 'Aug': 7, 'Sep': 8, 'Oct': 9, 'Nov': 10, 'Dec': 11
      };
      const longMonths: { [key: string]: number } = {
        'January': 0, 'February': 1, 'March': 2, 'April': 3, 'May': 4, 'June': 5,
        'July': 6, 'August': 7, 'September': 8, 'October': 9, 'November': 10, 'December': 11
      };
      const parts = dateString.split(' ');
      const day = parseInt(parts[0]);
      const monthStr = parts[1];
      // Proba skraćene mesece prvo, zatim puno ime
      const month = shortMonths[monthStr] !== undefined ? shortMonths[monthStr] : longMonths[monthStr];
      const year = parseInt(parts[2]);
      return new Date(year, month, day);
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
        const endOfToday = new Date(today);
        endOfToday.setHours(23, 59, 59, 999);
        this.dateFrom = sevenDaysAgo;
        this.dateTo = endOfToday;
        this.selectedStatus = '';
        this.applyFilters();
        break;
        
      case 'Last month':
        const thirtyDaysAgo = new Date(today);
        thirtyDaysAgo.setDate(today.getDate() - 30);
        const endOfTodayMonth = new Date(today);
        endOfTodayMonth.setHours(23, 59, 59, 999);
        this.dateFrom = thirtyDaysAgo;
        this.dateTo = endOfTodayMonth;
        this.selectedStatus = '';
        this.applyFilters();
        break;
        
      case 'Completed only':
        this.selectedStatus = 'completed';
        this.applyFilters();
        break;
        
      case 'Canceled only':
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
