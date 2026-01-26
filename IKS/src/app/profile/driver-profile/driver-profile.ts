import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { DriverService } from '../../services/driver.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './driver-profile.html',
  styleUrls: ['./driver-profile.css']
})
export class DriverProfileComponent implements OnInit {
  isActive: boolean = true;
  activeTab: string = 'profile'; 
  isEditMode: boolean = false;
  driver: any = {};

  constructor(private driverService: DriverService, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { 
    this.loadData(); 
  }

  loadData() {
    this.driverService.getDriverProfile().subscribe(data => {
      this.driver = data;
      this.cdr.detectChanges();
      this.isActive = data.isActive;
    });
  }

  onPhotoSelect(event: any) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const base64Content = e.target.result.replace(/^data:image\/[a-z]+;base64,/, '');
      
      this.driver.profilePictureUrl = base64Content;
      
      this.cdr.detectChanges(); 
    };
    reader.readAsDataURL(file);
  }
}

saveChanges() {
  this.driverService.updateDriverProfile(this.driver).subscribe({
    next: (response) => {
      this.driver = response;
      this.isEditMode = false;

      this.authService.updateUser(response); 

      this.cdr.detectChanges();
      alert('Changes saved successfully!');
    },
    error: (err) => {
      alert(`Failed to save: ${err.status} ${err.statusText}`);
    }
  });
}
  
  setActiveTab(tab: string) { this.activeTab = tab; this.isEditMode = false; }
  toggleEdit() { this.isEditMode = true; }
  toggleStatus() {

  this.isActive = !this.isActive;
  this.driver.isActive = this.isActive;

  this.driverService.updateDriverProfile(this.driver).subscribe({
    next: (res) => {
      console.log('Status updated in database:', res.isActive);
    },
    error: (err) => {
      this.isActive = !this.isActive;
      this.driver.isActive = this.isActive;
      alert('Could not update status!');
    }
  });
}
}