import { Component, OnInit, AfterViewInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import * as L from 'leaflet';
import { RideService } from '../services/ride.service';
import { RideDetailsResponseDTO, LocationResponseDTO } from '../../models/ride-dto.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './ride-details.html',
  styleUrl: './ride-details.css'
})
export class RideDetailsComponent implements OnInit, AfterViewInit, OnDestroy {
  rideId: number | null = null;
  private map!: L.Map;
  rideDetails: RideDetailsResponseDTO | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';
  private routePolyline: L.Polyline | null = null;
  private mapInitialized: boolean = false;
  private markers: L.Marker[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private rideService: RideService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('[RideDetails] Component initialized');
    this.route.params.subscribe(params => {
      console.log('[RideDetails] Route params:', params);
      const newRideId = +params['id'];
      console.log('[RideDetails] Parsed ride ID:', newRideId);
      
      // Check if this is a different ride
      if (this.rideId && this.rideId !== newRideId) {
        console.log('[RideDetails] Ride ID changed, cleaning up map...');
        this.cleanupMap();
      }
      
      this.rideId = newRideId;
      
      if (this.rideId) {
        this.loadRideDetails(this.rideId);
      } else {
        console.error('[RideDetails] Invalid ride ID from params');
        this.errorMessage = 'Invalid ride ID';
        this.isLoading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    // Map will be initialized after data loads
    console.log('[RideDetails] AfterViewInit called');
  }

  ngOnDestroy(): void {
    console.log('[RideDetails] Component destroyed, cleaning up map');
    if (this.map) {
      this.cleanupMapLayers();
      this.map.remove();
    }
  }

  private loadRideDetails(rideId: number): void {
    console.log('[RideDetails] Starting to load ride details for ID:', rideId);
    this.isLoading = true;
    this.errorMessage = '';
    
    this.rideService.getRideDetails(rideId).subscribe({
      next: (data) => {
        console.log('[RideDetails] Successfully loaded ride details:', data);
        this.rideDetails = data;
        this.isLoading = false;
        this.errorMessage = '';
        console.log('[RideDetails] isLoading set to:', this.isLoading);
        console.log('[RideDetails] rideDetails set to:', this.rideDetails);
        this.cdr.detectChanges();
        
        // Initialize map after data is loaded and view is updated
        setTimeout(() => {
          console.log('[RideDetails] Initializing map after data load...');
          this.initMap();
        }, 250);
      },
      error: (error) => {
        console.error('[RideDetails] Error loading ride details:', error);
        console.error('[RideDetails] Error status:', error.status);
        console.error('[RideDetails] Error message:', error.message);
        console.error('[RideDetails] Full error:', JSON.stringify(error, null, 2));
        
        if (error.status === 401) {
          this.errorMessage = 'Unauthorized. Please log in again.';
        } else if (error.status === 403) {
          this.errorMessage = 'You do not have permission to view this ride.';
        } else if (error.status === 404) {
          this.errorMessage = 'Ride not found.';
        } else {
          this.errorMessage = `Failed to load ride details: ${error.message || 'Unknown error'}`;
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private initMap(): void {
    console.log('[RideDetails] initMap called');
    const mapElement = document.getElementById('rideDetailsMap');
    console.log('[RideDetails] Map element:', mapElement);
    
    if (!mapElement) {
      console.error('[RideDetails] Map element not found! Retrying in 200ms...');
      // Retry after a short delay
      setTimeout(() => this.initMap(), 200);
      return;
    }

    if (this.mapInitialized) {
      console.log('[RideDetails] Map already initialized, just drawing route');
      this.drawRoute();
      return;
    }
    
    const noviSadLat = 45.265;
    const noviSadLng = 19.800;

    console.log('[RideDetails] Creating Leaflet map...');
    try {
      this.map = L.map('rideDetailsMap').setView([noviSadLat, noviSadLng], 13);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      this.mapInitialized = true;
      console.log('[RideDetails] Map initialized successfully');

      // Force map to invalidate size after initialization
      setTimeout(() => {
        if (this.map) {
          this.map.invalidateSize();
          console.log('[RideDetails] Map size invalidated');
        }
      }, 100);

      // Draw route if data is already loaded
      if (this.rideDetails) {
        console.log('[RideDetails] Data available, drawing route...');
        this.drawRoute();
      }
    } catch (error) {
      console.error('[RideDetails] Error creating map:', error);
      this.mapInitialized = false;
    }
  }

  private async drawRoute(): Promise<void> {
    console.log('[RideDetails] drawRoute called');
    console.log('[RideDetails] rideDetails:', this.rideDetails);
    console.log('[RideDetails] map:', this.map);
    
    if (!this.rideDetails || !this.map) {
      console.error('[RideDetails] Cannot draw route - missing data or map');
      return;
    }

    try {
      // Use coordinates directly from backend route
      if (!this.rideDetails.route || this.rideDetails.route.length === 0) {
        console.error('[RideDetails] No route data available from backend');
        return;
      }

      console.log('[RideDetails] Raw route data from backend:', this.rideDetails.route);
      console.log('[RideDetails] Drawing route with', this.rideDetails.route.length, 'locations');

      // Convert backend coordinates to Leaflet LatLng and validate
      const coordinates: L.LatLng[] = [];
      for (const location of this.rideDetails.route) {
        const lat = parseFloat(location.latitude);
        const lng = parseFloat(location.longitude);
        
        // Check if coordinates are valid
        if (isNaN(lat) || isNaN(lng)) {
          console.warn(`[RideDetails] Invalid coordinates for "${location.name}": lat=${location.latitude}, lng=${location.longitude}`);
          continue;
        }
        
        // Check if this is a duplicate coordinate
        const isDuplicate = coordinates.some(coord => 
          Math.abs(coord.lat - lat) < 0.0001 && Math.abs(coord.lng - lng) < 0.0001
        );
        
        if (isDuplicate) {
          console.warn(`[RideDetails] Skipping duplicate coordinate for "${location.name}": ${lat}, ${lng}`);
          continue;
        }
        
        console.log(`[RideDetails] Location "${location.name}": ${lat}, ${lng}`);
        coordinates.push(L.latLng(lat, lng));
      }

      console.log('[RideDetails] Converted coordinates:', coordinates);

      if (coordinates.length === 0) {
        console.error('[RideDetails] No valid coordinates!');
        return;
      }

      if (coordinates.length < 2) {
        console.warn('[RideDetails] Only got 1 coordinate. Showing marker without route.');
        // Just show the single marker we have
        const icon = L.divIcon({
          className: 'custom-marker',
          html: '<div style="background: #4CAF50; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">A</div>',
          iconSize: [30, 30],
          iconAnchor: [15, 15]
        });
        
        const marker = L.marker(coordinates[0], { icon }).addTo(this.map);
        this.markers.push(marker);
        this.map.setView(coordinates[0], 15);
        return;
      }

      // Remove old route and markers if exists
      this.cleanupMapLayers();

      // Try to get route from OSRM, fallback to straight line if it fails
      let routeCoordinates: L.LatLng[] = [];
      
      try {
        // Create route using OSRM routing service
        const waypoints = coordinates.map(c => `${c.lng},${c.lat}`).join(';');
        const routingUrl = `https://router.project-osrm.org/route/v1/driving/${waypoints}?overview=full&geometries=geojson`;

        console.log('[RideDetails] Fetching route from OSRM:', routingUrl);
        const response = await fetch(routingUrl);
        const data = await response.json();
        console.log('[RideDetails] OSRM response:', data);

        if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
          console.log('[RideDetails] Route found from OSRM');
          const route = data.routes[0];
          routeCoordinates = route.geometry.coordinates.map((coord: number[]) => 
            L.latLng(coord[1], coord[0])
          );
        } else {
          console.warn('[RideDetails] OSRM routing failed, using straight line');
          routeCoordinates = coordinates;
        }
      } catch (error) {
        console.error('[RideDetails] OSRM request failed, using straight line:', error);
        routeCoordinates = coordinates;
      }

      // Draw the route in purple
      console.log('[RideDetails] Drawing route with', routeCoordinates.length, 'points');
      this.routePolyline = L.polyline(routeCoordinates, {
        color: '#A855F7',
        weight: 5,
        opacity: 0.8
      }).addTo(this.map);
      console.log('[RideDetails] Route polyline added to map');

      // Add markers for start and end
      const startIcon = L.divIcon({
        className: 'custom-marker',
        html: '<div style="background: #4CAF50; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">A</div>',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
      });

      const endIcon = L.divIcon({
        className: 'custom-marker',
        html: '<div style="background: #F44336; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">B</div>',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
      });

      const startMarker = L.marker(coordinates[0], { icon: startIcon }).addTo(this.map)
        .bindPopup(`<b>Start:</b> ${this.rideDetails.route[0].name}`);
      
      const endMarker = L.marker(coordinates[coordinates.length - 1], { icon: endIcon }).addTo(this.map)
        .bindPopup(`<b>End:</b> ${this.rideDetails.route[this.rideDetails.route.length - 1].name}`);
      
      this.markers.push(startMarker, endMarker);
      console.log('[RideDetails] Markers added to map');

      // Fit map to show entire route
      this.map.fitBounds(this.routePolyline.getBounds(), { padding: [50, 50] });
      console.log('[RideDetails] Map bounds adjusted to show route');
    } catch (error: any) {
      console.error('[RideDetails] Error drawing route:', error);
    }
  }



  changeRating(): void {
    // Logika za promenu ocjene
    console.log('Change rating');
  }

  reportIssue(): void {
    // Logika za prijavljivanje problema
    console.log('Report issue');
  }

  rebookRide(): void {
    // Logika za ponovno rezervisanje vožnje
    console.log('Rebook ride');
  }

  scheduleForLater(): void {
    // Logika za zakazivanje kasnije
    console.log('Schedule for later');
  }

  closeRideInfo(): void {
    // Zatvaranje panela sa informacijama
    console.log('Close ride info');
  }

  zoomIn(): void {
    if (this.map) {
      this.map.zoomIn();
    }
  }

  zoomOut(): void {
    if (this.map) {
      this.map.zoomOut();
    }
  }

  getStatusClass(status: string): string {
    const statusLower = status?.toLowerCase() || '';
    
    if (statusLower === 'completed') {
      return 'status-completed';
    } else if (statusLower === 'canceled' || statusLower === 'cancelled') {
      return 'status-canceled';
    } else if (statusLower === 'pending' || statusLower === 'waiting') {
      return 'status-pending';
    } else if (statusLower === 'active' || statusLower === 'in-progress') {
      return 'status-active';
    }
    
    return 'status-completed'; // default
  }

  private cleanupMapLayers(): void {
    // Remove old route
    if (this.routePolyline) {
      this.map.removeLayer(this.routePolyline);
      this.routePolyline = null;
    }
    
    // Remove old markers
    this.markers.forEach(marker => {
      this.map.removeLayer(marker);
    });
    this.markers = [];
  }

  private cleanupMap(): void {
    console.log('[RideDetails] Cleaning up map for new ride...');
    if (this.map) {
      this.cleanupMapLayers();
    }
  }

  isRegisteredUser(): boolean {
    const userType = this.authService.getUserType();
    return userType === 'REGISTERED_USER';
  }

  formatPhoneNumber(phone: string): string {
    if (!phone || phone === '-') return phone;
    
    const digitsOnly = phone.replace(/\D/g, '');
    const length = digitsOnly.length;
    
    if (length <= 3) return digitsOnly;
    
    const remainder = length % 3;
    
    if (remainder === 1) {
      const groups: string[] = [];
      let i = 0;
      while (i < length - 4) {
        groups.push(digitsOnly.slice(i, i + 3));
        i += 3;
      }
      groups.push(digitsOnly.slice(i));
      return groups.join(' ');
    } else {
      const groups: string[] = [];
      for (let i = 0; i < length; i += 3) {
        groups.push(digitsOnly.slice(i, i + 3));
      }
      return groups.join(' ');
    }
  }
}
