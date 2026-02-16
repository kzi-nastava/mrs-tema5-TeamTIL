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

 // ZA BLOCKING TAB
  blockingView: string = 'drivers'; 
  drivers: any[] = [];
  users: any[] = [];
  filteredDrivers: any[] = []; 
  filteredUsers: any[] = []; 
  searchQuery: string = ''; 
  showBlockModal: boolean = false;
  selectedUser: any = null;
  blockReason: string = '';

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
    
    if (tabName === 'blocking') {
      this.searchQuery = ''; 
      if (this.blockingView === 'drivers') {
        this.loadDrivers();
      } else {
        this.loadUsers();
      }
    }
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

  // ZA BLOCKING TAB

  loadDrivers(): void {
    this.userService.getAllDrivers().subscribe({
      next: (data) => {
        this.drivers = data;
        this.filteredDrivers = data; // inicijalno prikazi sve
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Greška pri učitavanju vozača:', err)
    });
  }

  loadUsers(): void {
    this.userService.getAllRegisteredUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = data; // inicijalno prikazi sve
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Greška pri učitavanju korisnika:', err)
    });
  }

  openBlockModal(user: any): void {
    this.selectedUser = user;
    this.blockReason = user.blockReason || '';
    this.showBlockModal = true;
  }

  closeBlockModal(): void {
    this.showBlockModal = false;
    this.selectedUser = null;
    this.blockReason = '';
  }

  confirmBlockAction(): void {
    if (!this.selectedUser) return;

    const isBlocking = !this.selectedUser.isBlocked;

    // validacija - mora uneti razlog ako blokira
    if (isBlocking && !this.blockReason.trim()) {
      alert('Please provide a reason for blocking!');
      return;
    }

    // sacuvaj info pre zatvaranja modala
    const userType = this.selectedUser.userType === 'DRIVER' ? 'Driver' : 'User';
    const action = isBlocking ? 'blocked' : 'unblocked';

    this.userService.blockUser(
      this.selectedUser.id,
      isBlocking,
      this.blockReason
    ).subscribe({
      next: () => {
        // prvo zatvori modal
        this.closeBlockModal();
        
        // zatim prikazi alert
        alert(`${userType} ${action} successfully!`);
        
        // onda refresh liste
        if (this.blockingView === 'drivers') {
          this.loadDrivers();
        } else {
          this.loadUsers();
        }
      },
      error: (err) => {
        console.error('Greška pri blokiranju:', err);
        // zatvori modal i kod greske
        this.closeBlockModal();
        alert('Failed to block/unblock user!');
      }
    });
  }

  // SEARCH METODE

  onSearchChange(): void {
    const query = this.searchQuery.toLowerCase().trim();
    
    if (this.blockingView === 'drivers') {
      this.filterDrivers(query);
    } else {
      this.filterUsers(query);
    }
  }

  filterDrivers(query: string): void {
    if (!query) {
      this.filteredDrivers = this.drivers;
    } else {
      this.filteredDrivers = this.drivers.filter(driver => 
        driver.firstName.toLowerCase().includes(query) ||
        driver.lastName.toLowerCase().includes(query) ||
        driver.email.toLowerCase().includes(query) ||
        (driver.phoneNumber && driver.phoneNumber.includes(query))
      );
    }
    this.cdr.detectChanges();
  }

  filterUsers(query: string): void {
    if (!query) {
      this.filteredUsers = this.users;
    } else {
      this.filteredUsers = this.users.filter(user => 
        user.firstName.toLowerCase().includes(query) ||
        user.lastName.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        (user.phoneNumber && user.phoneNumber.includes(query))
      );
    }
    this.cdr.detectChanges();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.onSearchChange();
  }
}