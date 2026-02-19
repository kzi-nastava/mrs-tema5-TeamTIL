import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { ActiveRideAdminDTO } from '../models/active-ride-dto.model';
import { ActiveRidesService } from '../services/active-rides.service';

@Component({
  selector: 'app-active-rides',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './active-rides.html',
  styleUrls: ['./active-rides.css']
})
export class ActiveRides implements OnInit, OnDestroy {
  rides: ActiveRideAdminDTO[] = [];
  filteredRides: ActiveRideAdminDTO[] = [];
  selectedRide: ActiveRideAdminDTO | null = null;
  searchQuery: string = '';
  autoRefresh: boolean = true;
  isLoading: boolean = false;
  lastUpdated: Date = new Date();

  private refreshInterval: any;
  private readonly REFRESH_MS = 30_000;

  constructor(
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private activeRidesService: ActiveRidesService
  ) {}

  ngOnInit(): void {
    this.fetchActiveRides();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.clearAutoRefresh();
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken ? this.authService.getToken() : localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  fetchActiveRides(): void {
    this.isLoading = true;
    this.activeRidesService.getActiveRides().subscribe({
      next: (data) => {
        this.rides = data;
        this.applyFilter();
        this.lastUpdated = new Date();
        this.isLoading = false;

        // Keep selected ride in sync after refresh
        if (this.selectedRide) {
          const updated = this.rides.find(r => r.rideId === this.selectedRide!.rideId);
          this.selectedRide = updated ?? null;
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching active rides:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) {
      this.filteredRides = [...this.rides];
    } else {
      this.filteredRides = this.rides.filter(r =>
        `${r.driverFirstName} ${r.driverLastName}`.toLowerCase().includes(q) ||
        r.driverEmail.toLowerCase().includes(q) ||
        r.vehicleModel.toLowerCase().includes(q) ||
        r.licensePlate.toLowerCase().includes(q)
      );
    }
  }

  onSearchChange(): void {
    this.applyFilter();
    this.cdr.detectChanges();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applyFilter();
  }

  selectRide(ride: ActiveRideAdminDTO): void {
    this.selectedRide = this.selectedRide?.rideId === ride.rideId ? null : ride;
    this.cdr.detectChanges();
  }

  deselectRide(): void {
    this.selectedRide = null;
    this.cdr.detectChanges();
  }

  openFullDetails(): void {
    if (!this.selectedRide) return;
    this.router.navigate(['/track-ride', this.selectedRide.rideId]);
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
    if (this.autoRefresh) {
      this.startAutoRefresh();
    } else {
      this.clearAutoRefresh();
    }
  }

  private startAutoRefresh(): void {
    this.clearAutoRefresh();
    this.refreshInterval = setInterval(() => {
      if (this.autoRefresh) this.fetchActiveRides();
    }, this.REFRESH_MS);
  }

  private clearAutoRefresh(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
      this.refreshInterval = null;
    }
  }

  getStatusLabel(status: string): string {
    return status === 'IN_PROGRESS' ? 'ongoing' : 'upcoming'; // TODO ne valja
  }

  getAvatarSrc(pic: string | null): string | null {
    if (!pic) return null;
    return pic.startsWith('http') ? pic : `data:image/png;base64,${pic}`;
  }

  formatRating(rating: number): string {
    return rating?.toFixed(1) ?? 'N/A';
  }
}