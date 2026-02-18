import { Component, signal, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { NotificationService } from './services/notification.service';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    CommonModule,
    RouterModule
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

  constructor(private notificationService: NotificationService, private authService: AuthService, private cdr: ChangeDetectorRef) { }
  
  ngOnInit() {
    // Reconect ako je korisnik već ulogovan (page refresh)
    if (this.authService.isLoggedIn()) {
      const email = this.authService.getEmail();
      if (email) this.notificationService.connect(email);
    }

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
