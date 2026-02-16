import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { DriverService } from '../../services/driver.service';
import { AuthService } from '../../services/auth.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './driver-profile.html',
  styleUrls: ['./driver-profile.css']
})
export class DriverProfileComponent implements OnInit, OnDestroy {
  isActive: boolean = true;
  activeTab: string = 'profile'; 
  isEditMode: boolean = false;
  driver: any = {};
  activeHours: string = '';
  private activeHoursSubscription: Subscription | null = null;

  constructor(private driverService: DriverService, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { 
    this.loadData();
    this.startPollingActiveHours();
  }

  ngOnDestroy() {
    if (this.activeHoursSubscription) {
      this.activeHoursSubscription.unsubscribe();
    }
  }

  loadData() {
    this.driverService.getDriverProfile().subscribe(data => {
      this.driver = data;
      this.cdr.detectChanges();
      this.isActive = data.isActive;
    });

    this.driverService.getActiveHours().subscribe(data => {
      console.log('Active hours API response:', data);
      this.activeHours = this.formatActiveHours(data);
      this.cdr.detectChanges();
    });
  }

  startPollingActiveHours() {
    this.activeHoursSubscription = interval(60000).subscribe(() => {
      this.driverService.getActiveHours().subscribe(data => {
        console.log('Active hours API response (polling):', data);
        this.activeHours = this.formatActiveHours(data);
        this.cdr.detectChanges();
      });
    });
  }

  private formatActiveHours(data: any): string {
    // Ako je objekat sa activeHoursLast24h svojstvom (novi format)
    if (data && data.activeHoursLast24h !== undefined) {
      const totalMinutes = Math.round(data.activeHoursLast24h * 60);
      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;
      return `${hours}h ${minutes}min`;
    }

    // Ako je objekat sa activeHours svojstvom (stari format)
    if (data && data.activeHours !== undefined) {
      if (typeof data.activeHours === 'number') {
        const totalMinutes = Math.round(data.activeHours * 60);
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;
        return `${hours}h ${minutes}min`;
      } else if (typeof data.activeHours === 'string') {
        return data.activeHours;
      }
    }

    // Ako je direktno broj (decimalni sati)
    if (typeof data === 'number') {
      const totalMinutes = Math.round(data * 60);
      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;
      return `${hours}h ${minutes}min`;
    }

    // Fallback
    return '0h 0min';
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