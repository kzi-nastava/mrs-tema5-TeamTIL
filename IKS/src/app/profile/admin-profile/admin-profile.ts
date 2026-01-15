import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-profile.html',
  styleUrls: ['./admin-profile.css']
})
export class AdminProfileComponent {
  activeTab: string = 'profile';
  isEditMode: boolean = false; 

  admin = {
    name: 'Petar',
    surname: 'Petrovic',
    email: 'petarpetrovic@gmail.com',
    address: 'Bulevar despota Stefana 7a, 21000 Novi Sad',
    phone: '+381 64 123 123'
  };

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
    this.isEditMode = false; 
  }

  toggleEdit() {
    this.isEditMode = true; 
  }

  saveChanges() {
    this.isEditMode = false; 
    console.log('Novi podaci:', this.admin);
  }
}