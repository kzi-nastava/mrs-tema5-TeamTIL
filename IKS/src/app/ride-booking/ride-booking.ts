import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapView } from '../map/map';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { HttpClientModule } from '@angular/common/http';
import { RideService } from '../rides/services/ride.service';
import { RouteService } from '../services/route.service';
import { RideCreatedResponseDTO, RideRequestDTO, FavoriteRouteDTO } from '../models/ride-dto.model';
import { GeocodingService } from '../services/geocoding.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-ride-booking',
  standalone: true,
  imports: [CommonModule, MapView, FormsModule, ReactiveFormsModule, MatCheckboxModule, HttpClientModule],
  templateUrl: './ride-booking.html',
  styleUrls: ['./ride-booking.css']
})
export class RideBooking implements OnInit {
  
  startLocation: string = '';
  endLocation: string = '';
  intermediateStops: string[] = ['']; 
  
  showFavorites: boolean = false;
  
  passengers: string[] = [];
  newPassengerEmail: string = '';

  selectedVehicleType: string = 'STANDARD';
  babyFriendly: boolean = false;
  petFriendly: boolean = false;

  viewDate: Date = new Date(); 
  selectedDate: Date = new Date(); 
  daysInMonth: (number | null)[] = [];
  monthNames = ["January", "February", "March", "April", "May", "June", 
                "July", "August", "September", "October", "November", "December"];

  hourValue: number = 12;
  minuteValue: number = 0;

  favoriteRoutes: FavoriteRouteDTO[] = [];

  constructor(
    private rideService: RideService, 
    private routeService: RouteService,
    private geocodingService: GeocodingService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.generateCalendar();
    this.loadFavoriteRoutes(); 
  }

  loadFavoriteRoutes() {
    this.routeService.getFavoriteRoutes().subscribe({
      next: (routes) => {
        this.favoriteRoutes = routes;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading favorite routes:', err);
        this.favoriteRoutes = [];
      }
    });
  }

  selectFavoriteRoute(route: FavoriteRouteDTO) {
  // Popuni start location
  this.startLocation = route.startLocation;
  
  // Popuni end location
  this.endLocation = route.endLocation;
  
  // Popuni stops
  if (route.intermediateStops && route.intermediateStops.length > 0) {
    this.intermediateStops = [...route.intermediateStops];
  } else {
    this.intermediateStops = [''];
  }
  
  this.showFavorites = false;
  this.cdr.detectChanges();
}

  requestRide() {
    if (!this.startLocation || !this.startLocation.trim()) {
      alert("Please enter start location!");
      return;
    }
    
    if (!this.endLocation || !this.endLocation.trim()) {
      alert("Please enter end location!");
      return;
    }

    const allAddresses = [
      this.startLocation.trim(),
      ...this.intermediateStops
        .filter(s => s && s.trim() !== '')
        .map(s => s.trim()),
      this.endLocation.trim()
    ];

    const validEmails = this.passengers.filter(email => 
      email && email.includes('@')
    );

    const scheduledDateTime = new Date(this.selectedDate);
    scheduledDateTime.setHours(this.hourValue);
    scheduledDateTime.setMinutes(this.minuteValue);

    const now = new Date();
    if (scheduledDateTime <= now) {
      alert("Scheduled time must be in the future!");
      return;
    }

    const fiveHoursFromNow = new Date(now.getTime() + 5 * 60 * 60 * 1000);
    if (scheduledDateTime > fiveHoursFromNow) {
      alert("You can only schedule rides up to 5 hours in advance!");
      return;
    }

    const offset = scheduledDateTime.getTimezoneOffset();
    const localDate = new Date(scheduledDateTime.getTime() - (offset * 60 * 1000));
    const isoString = localDate.toISOString().split('.')[0];

    const geocodeObservables = allAddresses.map(address => 
      this.geocodingService.geocode(address)
    );

    forkJoin(geocodeObservables).subscribe({
      next: (results) => {
        for (let i = 0; i < results.length; i++) {
          if (!results[i] || results[i].length === 0) {
            alert(`❌ Could not find location: ${allAddresses[i]}`);
            return;
          }
        }

        const locations = results.map((result, index) => ({
          address: allAddresses[index],
          latitude: parseFloat(result[0].lat), 
          longitude: parseFloat(result[0].lon)
        }));

        const request: RideRequestDTO = {
          locations: locations,
          passengerEmails: validEmails,
          vehicleType: this.selectedVehicleType,
          babyFriendly: this.babyFriendly,
          petFriendly: this.petFriendly,
          scheduledTime: isoString
        };

        this.rideService.createRide(request).subscribe({
          next: (response: RideCreatedResponseDTO) => {
            const message = `
✅ Ride successfully ordered!

📍 Route: ${allAddresses[0]} → ${allAddresses[allAddresses.length - 1]}
${allAddresses.length > 2 ? '   Stops: ' + allAddresses.slice(1, -1).join(', ') : ''}

👤 Driver: ${response.driverName}
🚗 Vehicle: ${response.vehicleInfo}
💰 Price: ${response.estimatedPrice.toFixed(2)} RSD
📏 Distance: ${response.distanceKm.toFixed(1)} km
⏱️ Duration: ${response.durationMin.toFixed(0)} min

🕐 Start: ${response.startTime}
🏁 Estimated arrival: ${response.estimatedEndTime}

${validEmails.length > 0 ? '👥 Passengers: ' + validEmails.join(', ') : ''}
            `.trim();
            
            alert(message);
            this.resetForm();
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('Error creating ride:', err);
            
            let errorMessage = 'Failed to order ride.';
            if (err.error && err.error.error) {
              errorMessage = err.error.error;
            } else if (err.error && typeof err.error === 'string') {
              errorMessage = err.error;
            } else if (err.message) {
              errorMessage = err.message;
            }
            
            alert('❌ ' + errorMessage);
          }
        });
      },
      error: (err) => {
        console.error('Geocoding error:', err);
        alert('❌ Could not find one or more locations. Please check the addresses.');
      }
    });
  }

  resetForm() {
    this.startLocation = '';
    this.endLocation = '';
    this.intermediateStops = [''];
    this.passengers = [];
    this.newPassengerEmail = '';
    this.selectedVehicleType = 'STANDARD';
    this.babyFriendly = false;
    this.petFriendly = false;
    this.selectedDate = new Date();
    this.hourValue = 12;
    this.minuteValue = 0;
  }

  addPassenger() {
    const email = this.newPassengerEmail.trim();
    if (email && email.includes('@') && !this.passengers.includes(email)) {
      this.passengers.push(email);
      this.newPassengerEmail = ''; 
    }
  }

  removePassenger(index: number) {
    this.passengers.splice(index, 1);
  }

  incrementHour() { this.hourValue = (this.hourValue + 1) % 24; }
  incrementMinute() { this.minuteValue = (this.minuteValue + 1) % 60; }

  decrementHour() { this.hourValue = this.hourValue === 0 ? 23 : this.hourValue - 1; }
  decrementMinute() { this.minuteValue = this.minuteValue === 0 ? 59 : this.minuteValue - 1; }

  trackByIndex(index: number): number {
    return index;
  }
  
  formatTime(val: number): string { return val.toString().padStart(2, '0'); }

  generateCalendar() {
    const year = this.viewDate.getFullYear();
    const month = this.viewDate.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const numDays = new Date(year, month + 1, 0).getDate();
    const padding = firstDay === 0 ? 6 : firstDay - 1;
    this.daysInMonth = [...Array(padding).fill(null), ...Array.from({ length: numDays }, (_, i) => i + 1)];
  }

  prevMonth() { this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() - 1, 1); this.generateCalendar(); }
  nextMonth() { this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1, 1); this.generateCalendar(); }
  selectDay(day: number | null) { if (day) this.selectedDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth(), day); }
  
  isToday(day: number | null): boolean {
    if (!day) return false;
    const today = new Date();
    return today.getDate() === day && today.getMonth() === this.viewDate.getMonth() && today.getFullYear() === this.viewDate.getFullYear();
  }

  addStop() { if (this.intermediateStops.length < 5) this.intermediateStops.push(''); }
  removeStop(index: number) { this.intermediateStops.splice(index, 1); }
  toggleFavorites() { this.showFavorites = !this.showFavorites; }
}