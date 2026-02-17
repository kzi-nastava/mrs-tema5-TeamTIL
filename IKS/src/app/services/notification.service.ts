import { Injectable } from '@angular/core';
import { ReplaySubject, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private socket: WebSocket | null = null;
  rideFinished$ = new ReplaySubject<void>(1);

  // Observable na koji se komponente pretplaćuju
  notification$ = new Subject<any>();

  connect(email: string) {
    this.socket = new WebSocket(`ws://localhost:8080/ws/notifications?email=${email}`);
    
    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      this.notification$.next(data);
    };

    this.socket.onerror = (error) => console.error('WS error:', error);
    this.socket.onclose = () => console.log('WS closed');
  }

  disconnect() {
    this.socket?.close();
  }
}