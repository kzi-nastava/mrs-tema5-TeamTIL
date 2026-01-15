import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './driver-profile.html',
  styleUrls: ['./driver-profile.css']
})
export class DriverProfileComponent {
  isActive: boolean = true;
  activeTab: string = 'profile'; 
  isEditMode: boolean = false;

  driver = {
    name: 'Petar',
    surname: 'Petrovic',
    email: 'petarpetrovic@gmail.com',
    address: 'Bulevar despota Stefana 7a, 21000 Novi Sad',
    phone: '+381 64 123 123'
  };

  toggleStatus() {
    this.isActive = !this.isActive;
  }

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
    // ako se promeni tab, izadji iz edit moda
    this.isEditMode = false;
  }

  // Edit
  toggleEdit() {
    this.isEditMode = true;
  }

  saveChanges() {
    this.isEditMode = false;
    console.log('Sacuvani podaci:', this.driver);
  }
}