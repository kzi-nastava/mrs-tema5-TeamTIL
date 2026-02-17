import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RideService } from '../../rides/services/ride.service';
import { NotificationService } from '../../services/notification.service';

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
  hasActiveRide = false;
  activeRideId: number | null = null;

  constructor(private authService: AuthService, private rideService: RideService, private router: Router, private notificationService: NotificationService, private cdr: ChangeDetectorRef) {}
  // Handler for all navbar link clicks when not logged in
  onNavLinkClick(event: Event) {
    if (!this.isLoggedIn) {
      event.preventDefault();
      this.router.navigate(['/login']);
    }
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = user !== null;
      this.userType = user?.userType || null;
      this.userName = user?.name || user?.email?.split('@')[0] || 'Username';
      this.profilePhoto = user?.profilePictureUrl || null;
      console.log('Navbar - User profile photo URL:', this.profilePhoto);
      this.updateNavigation();

      // Proveri aktivnu vožnju za REGISTERED_USER
      if (user?.userType === 'REGISTERED_USER' && user.email) {
        this.rideService.getActiveUserRides(user.email).subscribe({
          next: (rides) => {
            const activeRide = rides.find((r: any) => r.status?.toUpperCase() === 'IN_PROGRESS');
            this.activeRideId = activeRide?.id || null;
            this.hasActiveRide = !!this.activeRideId;
            this.updateNavigation();
            this.cdr.detectChanges();
          },
          error: () => {
            this.hasActiveRide = false;
            this.cdr.detectChanges();
          }
        });
      } else {
        this.hasActiveRide = false;
      }
    });

    this.notificationService.rideFinished$.subscribe(() => {
      this.hasActiveRide = false;
      this.activeRideId = null;
      this.updateNavigation();
      this.cdr.detectChanges();
    });
  }

  private updateNavigation() {
    if (!this.isLoggedIn) {
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
            { label: 'Reports', route: '/report' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/admin-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/admin-profile', icon: 'fas fa-user' },
            { label: 'Ride History', route: '/admin-ride-history', icon: 'fas fa-calendar-alt' },
            { label: 'Reports', route: '/report', icon: 'fas fa-chart-line' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Change Password', route: '/change-password', icon: 'fas fa-key' },
            { label: 'Log out', icon: 'fas fa-sign-out-alt', action: () => this.logout() }
          ];
          break;
        case 'DRIVER':
          this.navLinks = [
            { label: 'My Vehicle', route: '/my-vehicle' },
            { label: 'Ride History', route: '/driver-ride-history' },
            { label: 'My rides', route: '/assigned-rides' },
            { label: 'Reports', route: '/report' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/driver-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/driver-profile', icon: 'fas fa-user' },
            { label: 'My Rides', route: '/assigned-rides', icon: 'fas fa-route' },
            { label: 'Ride History', route: '/driver-ride-history', icon: 'fas fa-calendar-alt' },
            { label: 'My Vehicle', route: '/my-vehicle', icon: 'fas fa-car' },
            { label: 'Reports', route: '/report', icon: 'fas fa-chart-line' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Change Password', route: '/change-password', icon: 'fas fa-key' },
            { label: 'Log out', icon: 'fas fa-sign-out-alt', action: () => this.logout() }
          ];
          break;
        default: // REGISTERED_USER
          this.navLinks = [
            ...(this.hasActiveRide 
              ? [{ label: 'Track Ride', route: '/track-ride/' + this.activeRideId }]
              : [{ label: 'Book an Uber', route: '/book' }]),
            { label: 'Ride History', route: '/user-ride-history' },
            { label: 'Favorite rides', route: '/favorites' },
            { label: 'Reports', route: '/report' },
            { label: 'Support', route: '/support' }
          ];
          this.profileRoute = '/user-profile';
          this.menuItems = [
            { label: 'View Profile', route: '/user-profile', icon: 'fas fa-user' },
            ...(this.hasActiveRide
              ? [{ label: 'Track Ride', route: '/track-ride/' + this.activeRideId, icon: 'fas fa-location-arrow' }]
              : [{ label: 'Book an Uber', route: '/book', icon: 'fas fa-taxi' }]),
            { label: 'Ride History', route: '/user-ride-history', icon: 'fas fa-calendar-alt' },
            { label: 'Favorite Rides', route: '/favorites', icon: 'fas fa-star' },
            { label: 'Reports', route: '/report', icon: 'fas fa-chart-line' },
            { label: 'Support', route: '/support', icon: 'fas fa-question-circle' },
            { label: 'Change Password', route: '/change-password', icon: 'fas fa-key' },
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
    this.profilePhoto = null;
  }
}