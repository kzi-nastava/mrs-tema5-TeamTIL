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
import { MatDialog } from '@angular/material/dialog';
import { RateRideComponent } from '../../modals/rate-ride/rate-ride';
import { RideService } from '../../services/ride.service';
import { RouteService } from '../../../services/route.service';
import { AuthService } from '../../../services/auth.service';

interface Ride {
  id: number;
  routeId?: number;
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
  vehicle?: { model: string; plate: string };
  isFavorite?: boolean; 
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
export class UserRideHistory implements OnInit {
  filterOptions = ['All', 'Last 7 days', 'Last month', 'Completed only', 'Canceled only'];
  activeFilter = 'All';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedStatus = '';

  allRides: Ride[] = [];
  rides: Ride[] = [];
  selectedRide: Ride | null = null;

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private rideService: RideService,
    private routeService: RouteService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const passengerEmail = this.authService.getEmail();
    if (!passengerEmail) {
      console.error('User not authenticated');
      return;
    }

    this.rideService.getUserRides(passengerEmail).subscribe({
      next: (ridesFromBackend) => {
        this.allRides = ridesFromBackend.map(ride => ({
          id: ride.id,
          routeId: ride.routeId,
          date: ride.startTime?.split(',')[0]?.trim() || '-',
          startTime: ride.startTime?.split(',')[1]?.trim() || '-',
          endTime: ride.estimatedEndTime ? ride.estimatedEndTime.split(',')[1]?.trim() : '-',
          from: ride.startLocation || '-',
          to: ride.endLocation || '-',
          price: ride.price ? `${ride.price.toFixed(0)}` : '-',
          status: ride.status === 'FINISHED' ? 'Completed' : (ride.status === 'CANCELED' ? 'Canceled' : ride.status),
          duration: ride.duration && ride.duration > 0 ? `${Math.round(ride.duration)} min` : '-',
          distance: ride.distance && ride.distance > 0 ? `${ride.distance.toFixed(1)} km` : '-',
          driver: {
            name: ride.driverFirstName && ride.driverLastName
              ? `${ride.driverFirstName} ${ride.driverLastName}`
              : ride.driverEmail || '-',
            phone: ride.driverPhoneNumber || '-'
          },
          vehicle: { model: '-', plate: '-' },
          isFavorite: false
        }));
        
        this.checkFavorites();
        
        this.rides = [...this.allRides];
        this.selectedRide = this.rides.length > 0 ? this.rides[0] : null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[UserRideHistory] Error fetching user ride history:', err);
      }
    });
  }

  checkFavorites() {
    this.allRides.forEach(ride => {
      if (ride.routeId) {
        this.routeService.isFavorite(ride.routeId).subscribe({
          next: (isFav) => {
            ride.isFavorite = isFav;
            this.cdr.detectChanges();
          },
          error: (err) => console.error('Error checking favorite:', err)
        });
      }
    });
  }

  toggleFavorite(ride: Ride, event: Event) {
    event.stopPropagation();
    
    if (!ride.routeId) {
      alert('Cannot add this ride to favorites (no route ID)');
      return;
    }

    if (ride.isFavorite) {
      // Ukloni iz omiljenih
      this.routeService.removeFromFavorites(ride.routeId).subscribe({
        next: () => {
          ride.isFavorite = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error removing from favorites:', err);
          alert('❌ Failed to remove from favorites');
        }
      });
    } else {
      // Dodaj u omiljene
      this.routeService.addToFavorites(ride.routeId).subscribe({
        next: () => {
          ride.isFavorite = true;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error adding to favorites:', err);
          alert('❌ Failed to add to favorites');
        }
      });
    }
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
    }
  }

  applyFilters() {
    let filtered = [...this.allRides];
    
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
      'Jan': 0, 'Feb': 1, 'Mar': 2, 'Apr': 3, 'May': 4, 'Jun': 5,
      'Jul': 6, 'Aug': 7, 'Sep': 8, 'Oct': 9, 'Nov': 10, 'Dec': 11,
      'January': 0, 'February': 1, 'March': 2, 'April': 3, 'June': 5,
      'July': 6, 'August': 7, 'September': 8, 'October': 9, 'November': 10, 'December': 11
    };
    
    const parts = dateString.split(' ');
    const day = parseInt(parts[0]);
    const month = months[parts[1]];
    const year = parseInt(parts[2]);
    
    return new Date(year, month, day);
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

  selectRide(ride: Ride) {
    this.selectedRide = ride;
  }

  viewFullRideDetails(ride: Ride) {
    this.router.navigate(['/ride-details', ride.id]);
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

  openRateRide(ride: Ride) {
    if (!ride) return;

    const dialogRef = this.dialog.open(RateRideComponent, {
      width: '420px',
      data: ride,
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Rating:', result.rating, result.comment);
      }
    });
  }
}