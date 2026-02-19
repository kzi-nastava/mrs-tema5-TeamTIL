import { Injectable } from '@angular/core';
import { ReplaySubject, Subject, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface PanicAlert {
  panicId: string;
  rideId: string;
  reportedBy: 'DRIVER' | 'REGISTERED_USER';
  location: string;
  locationAddress?: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  vehicleName?: string;
  vehicleLicensePlate?: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private socket: WebSocket | null = null;
  rideFinished$ = new ReplaySubject<void>(1);

  // Observable na koji se komponente pretplaćuju
  notification$ = new Subject<any>();
  panicAlert$ = new Subject<PanicAlert>();
  
  // Track vehicles in panic state (by license plate)
  vehiclesInPanic$ = new BehaviorSubject<Set<string>>(new Set());
  
  private panicVehicles = new Set<string>();

  connect(email: string) {
    // Use environment wsUrl with email parameter
    const wsUrl = `${environment.wsUrl}?email=${email}`;
    this.socket = new WebSocket(wsUrl);
    
    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      // Hendluj panic alerte posebno
      if (data.type === 'PANIC_ALERT') {
        this.handlePanicAlert(data);
      } else {
        this.notification$.next(data);
      }
    };

    this.socket.onerror = (error) => console.error('WS error:', error);
    this.socket.onclose = () => console.log('WS closed');
  }

  private handlePanicAlert(alertData: any) {
    console.log('[NotificationService] Raw panic alert data from WebSocket:', alertData);
    
    const panicAlert: PanicAlert = {
      panicId: alertData.panicId,
      rideId: alertData.rideId,
      reportedBy: alertData.reportedBy,
      location: alertData.location,
      locationAddress: alertData.locationAddress || alertData.location || 'Unknown Location',
      latitude: alertData.latitude,
      longitude: alertData.longitude,
      timestamp: alertData.timestamp || new Date().toLocaleTimeString(),
      vehicleName: alertData.vehicleName,
      vehicleLicensePlate: alertData.vehicleLicensePlate
    };

    console.log('[NotificationService] Parsed panic alert:', panicAlert);
    console.log('[NotificationService] Vehicle license plate:', panicAlert.vehicleLicensePlate);

    // Add vehicle to panic list
    if (panicAlert.vehicleLicensePlate) {
      this.panicVehicles.add(panicAlert.vehicleLicensePlate);
      this.vehiclesInPanic$.next(new Set(this.panicVehicles));
      console.log('[NotificationService] ✅ Added vehicle to panic set:', panicAlert.vehicleLicensePlate);
    } else {
      console.warn('[NotificationService] ⚠️ No vehicle license plate in panic alert! Cannot mark vehicle red.');
    }

    // Reprodukuj zvuk
    this.playAlertSound();

    // Prikaži browser notifikaciju ako je dozvola data
    this.showBrowserNotification(panicAlert);

    // Emituj event kroz observable
    this.panicAlert$.next(panicAlert);
  }

  private playAlertSound() {
    try {
      const audio = new Audio('data:audio/wav;base64,UklGRiYAAABXQVZFZm10IBAAAAABAAEAQB8AAAB9AAACABAAZGF0YQIAAAAAAA==');
      audio.play().catch(err => console.log('Audio play failed:', err));
    } catch (error) {
      console.log('Could not play alert sound');
    }
  }

  private showBrowserNotification(alert: PanicAlert) {
    if (!('Notification' in window)) {
      return;
    }

    if (Notification.permission === 'granted') {
      new Notification('🚨 PANIC ALERT!', {
        body: `${alert.reportedBy === 'DRIVER' ? 'Driver' : 'User'} reported panic at ${alert.location}`,
        icon: '/alert-icon.png',
        tag: `panic-${alert.panicId}`,
        requireInteraction: true
      });
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission();
    }
  }

  constructor(private authService: AuthService) {}

  async markPanicAsHandled(panicId: string): Promise<void> {
    try {
      const token = this.authService.getToken();
      const apiUrl = environment.apiUrl;
      
      // Convert panicId to number
      const numericId = parseInt(panicId, 10);
      if (isNaN(numericId)) {
        throw new Error(`Invalid panicId: ${panicId}`);
      }
      
      console.log('[NotificationService] Marking panic as handled with ID:', numericId, 'Token exists:', !!token);
      
      const response = await fetch(`${apiUrl}/panic/${numericId}/handle`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('[NotificationService] Error response:', response.status, errorText);
        throw new Error(`Failed to mark panic as handled: ${response.status} - ${errorText}`);
      }
      
      console.log('[NotificationService] ✅ Panic marked as handled:', numericId);
    } catch (error) {
      console.error('[NotificationService] Error marking panic as handled:', error);
      throw error;
    }
  }

  removePanicVehicle(licensePlate: string): void {
    if (licensePlate) {
      this.panicVehicles.delete(licensePlate);
      this.vehiclesInPanic$.next(new Set(this.panicVehicles));
      console.log('[NotificationService] Removed vehicle from panic:', licensePlate);
    }
  }

  addPanicVehicle(licensePlate: string): void {
    if (licensePlate) {
      this.panicVehicles.add(licensePlate);
      this.vehiclesInPanic$.next(new Set(this.panicVehicles));
      console.log('[NotificationService] Added vehicle to panic:', licensePlate);
      console.log('[NotificationService] Current panic vehicles:', Array.from(this.panicVehicles));
    }
  }

  disconnect() {
    this.socket?.close();
  }
}