import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-profile.html',
  styleUrls: ['./user-profile.css'],
})
export class UserProfile {
  isEditMode: boolean = false;

  user = {
    name: 'Petar',
    surname: 'Petrovic',
    email: 'petarpetrovic@gmail.com',
    address: 'Bulevar despota Stefana 7a, 21000 Novi Sad',
    phone: '+381 64 123 123'
  };

  toggleEdit() {
    this.isEditMode = true;
  }

  saveChanges() {
    this.isEditMode = false;
    // Podaci su već ažurirani u 'this.user' objektu zahvaljujući [(ngModel)]
    console.log('Novi podaci:', this.user);
  }
}