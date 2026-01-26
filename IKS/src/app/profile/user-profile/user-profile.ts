import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './user-profile.html',
  styleUrls: ['./user-profile.css'],
})
export class UserProfile implements OnInit {
  isEditMode: boolean = false;
  
  user: any = {
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: ''
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

 ngOnInit(): void {
  // provera da li uopste imamo token pre nego sto zovemo server
  if (this.userService.hasToken()) { 
     this.loadUserData();
  }
}

  loadUserData(): void {
    this.userService.getMyProfile().subscribe({
      next: (data) => {
        this.user = data;
        this.cdr.detectChanges(); // osvezavanje prikaza
      },
      error: (err) => console.error('Greška pri ucitavanju:', err)
    });
  }

  toggleEdit(): void {
    this.isEditMode = true;
  }

  saveChanges(): void {
    // provera pre slanja
    this.userService.updateMyProfile(this.user).subscribe({
      next: (response) => {
        this.user = response;
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

  onPhotoSelect(event: any) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const base64Content = e.target.result.replace(/^data:image\/[a-z]+;base64,/, '');
      
      this.user.profilePictureUrl = base64Content;
      
      this.cdr.detectChanges(); 
    };
    reader.readAsDataURL(file);
  }
}
}
  