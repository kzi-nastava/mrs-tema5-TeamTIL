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

  constructor(private notificationService: NotificationService, private authService: AuthService, private cdr: ChangeDetectorRef) { }
  
  ngOnInit() {
    // Reconect ako je korisnik već ulogovan (page refresh)
    if (this.authService.isLoggedIn()) {
      const email = this.authService.getEmail();
      if (email) this.notificationService.connect(email);
    }

    this.notificationService.notification$.subscribe(data => {
      if (data.type === 'RIDE_FINISHED') {
        this.notificationMessage = data.message;
        this.showNotification = true;
        this.notificationService.rideFinished$.next(); // trigger za navbar
        this.cdr.detectChanges(); // osveži UI

        // sakrij nakon 8 sekundi
        setTimeout(() => {
          this.showNotification = false;
          this.cdr.detectChanges(); // osveži UI
        }, 8000);
      }
    });
  }
}
