import { Component, signal, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { NotificationService, PanicAlert } from './services/notification.service';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { PanicAlertModalComponent } from './shared/panic-alert-modal/panic-alert-modal';
import { RideService } from './rides/services/ride.service';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    CommonModule,
    RouterModule,
    PanicAlertModalComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  protected readonly title = signal('UberProject');
  showNotification = false;
  notificationMessage = '';
  notificationTitle = '';
  notificationType = '';

  // Panic alert properties
  showPanicModal = false;
  currentPanicAlert: PanicAlert | null = null;
  userType: string | null = null;

  constructor(private notificationService: NotificationService, private authService: AuthService, private rideService: RideService, private cdr: ChangeDetectorRef) { }
  
  ngOnInit() {
    // Get current user type
    this.authService.currentUser$.subscribe(user => {
      this.userType = user?.userType || null;
    });

    // Reconect ako je korisnik već ulogovan (page refresh)
    if (this.authService.isLoggedIn()) {
      const email = this.authService.getEmail();
      if (email) this.notificationService.connect(email);
    }

    // Subscribe na obične notifikacije
    this.notificationService.notification$.subscribe(data => {
      const handledTypes = ['RIDE_FINISHED', 'RIDE_ACCEPTED', 'RIDE_REJECTED', 'RIDE_REMINDER', 'NEW_RIDE_ASSIGNED'];

      if (handledTypes.includes(data.type)) {
        this.notificationMessage = data.message;
        this.notificationTitle = this.getNotificationTitle(data.type);
        this.notificationType = data.type;
        this.showNotification = true;

        if (data.type === 'RIDE_FINISHED') {
          this.notificationService.rideFinished$.next();
        }

        this.cdr.detectChanges();

        setTimeout(() => {
          this.showNotification = false;
          this.cdr.detectChanges();
        }, 8000);
      }
    });

    // Subscribe na panic alerts
    this.notificationService.panicAlert$.subscribe((alert: PanicAlert) => {
      console.log('[App] Received panic alert:', alert);
      this.currentPanicAlert = alert;
      this.showPanicModal = true;
      
      // If WebSocket already provided vehicle info, use it immediately
      if (alert.vehicleLicensePlate) {
        console.log('[App] WebSocket provided vehicle info:', alert.vehicleLicensePlate);
        this.notificationService.addPanicVehicle(alert.vehicleLicensePlate);
      }
      
      // Try to load ride details for additional info
      if (alert.rideId) {
        const rideIdNum = typeof alert.rideId === 'string' ? parseInt(alert.rideId, 10) : alert.rideId;
        console.log('[App] Loading ride details for rideId:', rideIdNum);
        
        this.rideService.getRideDetails(rideIdNum)
          .pipe(take(1))
          .subscribe({
            next: (rideDetails) => {
              console.log('[App] Loaded ride details for panic:', rideDetails);
              
              // Add vehicle to panic list if we have license plate
              if (rideDetails?.vehicleLicensePlate && !alert.vehicleLicensePlate) {
                console.log('[App] Adding vehicle from ride details:', rideDetails.vehicleLicensePlate);
                this.notificationService.addPanicVehicle(rideDetails.vehicleLicensePlate);
              }
              
              // Update alert with vehicle info from ride details (preferred)
              this.currentPanicAlert = {
                ...alert,
                timestamp: alert.timestamp || new Date().toLocaleTimeString(),
                vehicleName: rideDetails?.vehicleModel || alert.vehicleName || 'Unknown',
                vehicleLicensePlate: rideDetails?.vehicleLicensePlate || alert.vehicleLicensePlate || 'Unknown'
              };
              
              this.cdr.detectChanges();
            },
            error: (err) => {
              console.warn('[App] ⚠️ Could not load ride details (may not exist), using WebSocket data:', err);
              // Still show the panic alert with WebSocket data
              this.currentPanicAlert = {
                ...alert,
                timestamp: alert.timestamp || new Date().toLocaleTimeString(),
                vehicleName: alert.vehicleName || 'Unknown',
                vehicleLicensePlate: alert.vehicleLicensePlate || 'Unknown'
              };
              this.cdr.detectChanges();
            }
          });
      }
      
      this.cdr.detectChanges();
    });
  }

  onClosePanicModal() {
    this.showPanicModal = false;
    this.currentPanicAlert = null;
    this.cdr.detectChanges();
  }

  onMarkPanicAsHandled(panicId: string) {
    console.log('[App] onMarkPanicAsHandled called with panicId:', panicId);
    
    // Remove vehicle from panic list
    if (this.currentPanicAlert?.vehicleLicensePlate) {
      console.log('[App] Removing vehicle from panic:', this.currentPanicAlert.vehicleLicensePlate);
      this.notificationService.removePanicVehicle(this.currentPanicAlert.vehicleLicensePlate);
    }
    
    this.notificationService.markPanicAsHandled(panicId)
      .then(() => {
        console.log('[App] ✅ Successfully marked panic as handled');
        this.onClosePanicModal();
      })
      .catch(err => {
        console.error('[App] ❌ Error marking panic as handled:', err);
        alert('Failed to mark panic as handled. Please try again.');
      });
  }

getNotificationTitle(type: string): string {
  switch(type) {
    case 'RIDE_ACCEPTED': return '✅ Ride Accepted';
    case 'RIDE_REJECTED': return '❌ No Drivers Available';
    case 'NEW_RIDE_ASSIGNED': return '🚗 New Ride Assigned';
    case 'RIDE_REMINDER': return '⏰ Ride Reminder';
    case 'RIDE_FINISHED': return '🏁 Ride Finished';
    default: return 'Notification';
  }
}
}
