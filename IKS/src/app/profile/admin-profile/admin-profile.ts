import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-profile.html',
  styleUrls: ['./admin-profile.css']
})
export class AdminProfileComponent implements OnInit {
  activeTab: string = 'profile';
  isEditMode: boolean = false; 

  admin: any = {
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    profilePictureUrl: ''
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAdminData();
  }

  loadAdminData(): void {
    this.userService.getMyProfile().subscribe({
      next: (data) => {
        this.admin = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Greška pri učitavanju admina:', err)
    });
  }

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
    this.isEditMode = false; 
  }

  toggleEdit() {
    this.isEditMode = true; 
  }

  saveChanges() {
  
    this.userService.updateMyProfile(this.admin).subscribe({
      next: (response) => {
        this.admin = response;
        this.isEditMode = false;
        
        this.authService.updateUser(response);
        
        this.cdr.detectChanges();
        alert('Admin profile updated successfully!');
      },
      error: (err) => alert('Failed to update admin profile')
    });
  }

  onPhotoSelect(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        
        const base64Content = e.target.result.replace(/^data:image\/[a-z]+;base64,/, '');
        this.admin.profilePictureUrl = base64Content;
        this.cdr.detectChanges(); 
      };
      reader.readAsDataURL(file);
    }
  }
}