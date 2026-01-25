import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface NavLink {
  label: string;
  route: string;
}

interface MenuItem {
  label: string;
  route?: string;
  icon: string;
  action?: () => void;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent implements OnInit {
  profileRoute: string = '/user-profile';
  userType: string | null = null;
  isLoggedIn: boolean = false;
  navLinks: NavLink[] = [];
  menuItems: MenuItem[] = [];
  isDropdownOpen: boolean = false;
  userName: string = 'Username';
  profilePhoto: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = user !== null;
      this.userType = user?.userType || null;
      this.userName = user?.name || user?.email?.split('@')[0] || 'Username';
      this.profilePhoto = user?.profilePictureUrl || null;
      console.log('Navbar - User profile photo URL:', this.profilePhoto);
      this.updateNavigation();
    });
  }

  private updateNavigation() {
    if (!this.isLoggedIn) {
      // Neregistrovani korisnik
      this.navLinks = [
        { label: 'Book an Uber', route: '/book' },
        { label: 'Ride History', route: '/user-ride-history' },
        { label: 'Favorite rides', route: '/favorites' },
        { label: 'Support', route: '/support' }
      ];
      this.profileRoute = '/user-profile';
      this.menuItems = [];
    } else {
      switch (this.userType) {
        case 'ADMINISTRATOR':
          this.navLinks = [
            { label: 'Driver registration', route: '/driver-registration' },
            { label: 'Ride History', route: '/admin-ride-history' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/admin-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/admin-profile', icon: 'fas fa-user' },
            { label: 'Ride History', route: '/admin-ride-history', icon: 'fas fa-calendar-alt' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Log out', icon: 'fas fa-sign-out-alt', action: () => this.logout() }
          ];
          break;
        case 'DRIVER':
          this.navLinks = [
            { label: 'My Vehicle', route: '/my-vehicle' },
            { label: 'Ride History', route: '/driver-history' },
            { label: 'My rides', route: '/assigned-rides' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/driver-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/driver-profile', icon: 'fas fa-user' },
            { label: 'My Rides', route: '/assigned-rides', icon: 'fas fa-route' },
            { label: 'Ride History', route: '/driver-history', icon: 'fas fa-calendar-alt' },
            { label: 'My Vehicle', route: '/my-vehicle', icon: 'fas fa-car' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Log out', icon: 'fas fa-sign-out-alt', action: () => this.logout() }
          ];
          break;
        default: // REGISTERED_USER
          this.navLinks = [
            { label: 'Book an Uber', route: '/book' },
            { label: 'Ride History', route: '/user-ride-history' },
            { label: 'Favorite rides', route: '/favorites' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/user-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/user-profile', icon: 'fas fa-user' },
            { label: 'Book an Uber', route: '/book', icon: 'fas fa-taxi' },
            { label: 'Ride History', route: '/user-ride-history', icon: 'fas fa-calendar-alt' },
            { label: 'Favorite Rides', route: '/favorites', icon: 'fas fa-star' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Log out', icon: 'fas fa-sign-out-alt', action: () => this.logout() }
          ];
      }
    }
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown() {
    this.isDropdownOpen = false;
  }

  onMenuItemClick(item: MenuItem) {
    if (item.action) {
      item.action();
    }
    this.closeDropdown();
  }

  logout() {
    this.authService.logout();
    this.closeDropdown();
  }

  onImageError(event: any) {
    console.error('Failed to load profile image:', this.profilePhoto);
    this.profilePhoto = null; // Fallback to default icon
  }
}
