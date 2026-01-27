import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapView } from '../map/map';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'app-ride-booking',
  standalone: true,
  imports: [CommonModule, MapView, FormsModule, ReactiveFormsModule, MatCheckboxModule],
  templateUrl: './ride-booking.html',
  styleUrls: ['./ride-booking.css']
})
export class RideBooking implements OnInit {
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

  ngOnInit() {
    this.generateCalendar();
  }

  // funkcija za dodavanje putnika (slicno kao stanice)
  addPassenger() {
    const email = this.newPassengerEmail.trim();
    if (email && email.includes('@') && !this.passengers.includes(email)) {
      this.passengers.push(email);
      this.newPassengerEmail = ''; // resetuj polje nakon unosa
    }
  }

  removePassenger(index: number) {
    this.passengers.splice(index, 1);
  }

  // time stepper logika
  incrementHour() { this.hourValue = (this.hourValue + 1) % 24; }
  incrementMinute() { this.minuteValue = (this.minuteValue + 1) % 60; }
  formatTime(val: number): string { return val.toString().padStart(2, '0'); }

  // kalendar logika
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

  // stopovi logika
  addStop() { if (this.intermediateStops.length < 5) this.intermediateStops.push(''); }
  removeStop(index: number) { this.intermediateStops.splice(index, 1); }
  toggleFavorites() { this.showFavorites = !this.showFavorites; }
}