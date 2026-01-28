import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapView } from '../map/map';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { HttpClientModule } from '@angular/common/http';
import { RideService } from '../services/ride.service';
import { RideRequestDTO } from '../models/ride-dto.model';

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
  
  // lista mejlova za putnike
  passengers: string[] = [];
  newPassengerEmail: string = '';

  // tip vozila i dodaci
  selectedVehicleType: string = 'STANDARD';
  babyFriendly: boolean = false;
  petFriendly: boolean = false;

  // kalendar podaci
  viewDate: Date = new Date(); 
  selectedDate: Date = new Date(); 
  daysInMonth: (number | null)[] = [];
  monthNames = ["January", "February", "March", "April", "May", "June", 
                "July", "August", "September", "October", "November", "December"];

  // vreme podaci
  hourValue: number = 12;
  minuteValue: number = 0;

  // Injektujemo servis
  constructor(private rideService: RideService) {}

  ngOnInit() {
    this.generateCalendar();
  }

  // --- GLAVNA FUNKCIJA ZA SLANJE ---
  requestRide() {
    if (!this.startLocation || !this.endLocation) {
      alert("Please enter start and end locations!");
      return;
    }

    // Spajamo lokacije: [Start, ...Stanice, Kraj]
    // Filter izbacuje prazne stringove ako korisnik nije popunio medjustanicu
    const allLocations = [
      this.startLocation,
      ...this.intermediateStops.filter(s => s && s.trim() !== ''),
      this.endLocation
    ];

    // Priprema datuma
    const scheduledDateTime = new Date(this.selectedDate);
    scheduledDateTime.setHours(this.hourValue);
    scheduledDateTime.setMinutes(this.minuteValue);

    // Konverzija u ISO string za backend (pazimo na vremensku zonu)
    // Ovo salje lokalno vreme kao string
    const offset = scheduledDateTime.getTimezoneOffset();
    const localDate = new Date(scheduledDateTime.getTime() - (offset * 60 * 1000));
    const isoString = localDate.toISOString().split('.')[0]; 

    const request: RideRequestDTO = {
      locations: allLocations,
      passengerEmails: this.passengers,
      vehicleType: this.selectedVehicleType,
      babyFriendly: this.babyFriendly,
      petFriendly: this.petFriendly,
      scheduledTime: isoString 
    };

    console.log('Sending request:', request);

    this.rideService.createRide(request).subscribe({
      next: (response) => {
        console.log('Ride created:', response);
        alert('Ride ordered successfully! Price: ' + response.price);
      },
      error: (err) => {
        console.error('Error:', err);
        alert('Failed to order ride.');
      }
    });
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