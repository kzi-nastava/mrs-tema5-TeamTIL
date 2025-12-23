import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-profile.html',
  styleUrls: ['./driver-profile.css']
})
export class DriverProfileComponent {
  // pocetno stanje je aktivno
  isActive: boolean = true;

  // funkcija koja menja status na klik
  toggleStatus() {
    this.isActive = !this.isActive;
  }
}
