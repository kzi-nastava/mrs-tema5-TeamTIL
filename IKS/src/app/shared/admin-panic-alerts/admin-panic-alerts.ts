import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PanicService } from '../../services/panic.service';
import { NotificationService } from '../../services/notification.service';
import { PanicAlertResponse } from '../../models/panic.model';

@Component({
  selector: 'app-admin-panic-alerts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-panic-alerts.html',
  styleUrl: './admin-panic-alerts.css'
})
export class AdminPanicAlertsComponent implements OnInit {
  panicAlerts: PanicAlertResponse[] = [];
  unhandledAlerts: PanicAlertResponse[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private panicService: PanicService, 
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadPanicAlerts();
  }

  loadPanicAlerts() {
    this.isLoading = true;
    console.log('[AdminPanicAlerts] Loading panic alerts...');
    
    this.panicService.getPanicAlerts().subscribe({
      next: (alerts) => {
        console.log('[AdminPanicAlerts] Raw alerts from API:', alerts);
        console.log('[AdminPanicAlerts] Loaded alerts count:', alerts.length);
        
        // Map alerts to ensure panicId field is set
        this.panicAlerts = alerts.map(alert => {
          const mapped: PanicAlertResponse = {
            ...alert as any,
            panicId: (alert.panicId ?? alert.id ?? 0) as number | string,
            locationAddress: alert.locationAddress || alert.location || 'Unknown Location',
            vehicleName: alert.vehicleName || 'Unknown Vehicle',
            vehicleLicensePlate: alert.vehicleLicensePlate || 'N/A',
            rideId: alert.rideId || 0,
            reportedBy: alert.reportedBy || 'DRIVER',
            latitude: alert.latitude || 45.2671,
            longitude: alert.longitude || 19.8335,
            timestamp: alert.timestamp || new Date().toISOString(),
            handled: alert.handled || false
          };
          return mapped;
        }).sort((a, b) => 
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        );
        
        this.unhandledAlerts = this.panicAlerts.filter(a => !a.handled);
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('[AdminPanicAlerts] ✅ Alerts loaded successfully');
      },
      error: (err) => {
        console.error('[AdminPanicAlerts] Error loading panic alerts:', err);
        console.error('[AdminPanicAlerts] Error status:', err.status);
        console.error('[AdminPanicAlerts] Error message:', err.message);
        console.error('[AdminPanicAlerts] Error response:', err.error);
        
        this.errorMessage = `Failed to load panic alerts (Status: ${err.status})`;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  markAsHandled(panicId: string | number, licensePlate?: string) {
    console.log('[AdminPanicAlerts] Marking panic as handled:', panicId);
    
    // Convert to string if needed
    const panicIdStr = String(panicId);
    
    this.panicService.markPanicAsHandled(panicIdStr).subscribe({
      next: () => {
        console.log('[AdminPanicAlerts] ✅ Marked panic as handled:', panicId);
        // Remove vehicle from panic list
        if (licensePlate) {
          this.notificationService.removePanicVehicle(licensePlate);
        }
        this.loadPanicAlerts();
      },
      error: (err) => {
        console.error('[AdminPanicAlerts] ❌ Error marking panic as handled:', err);
        console.error('[AdminPanicAlerts] Error status:', err.status);
        console.error('[AdminPanicAlerts] Error message:', err.message);
        this.cdr.detectChanges();
      }
    });
  }

  getReporterIcon(reportedBy: string): string {
    return reportedBy === 'DRIVER' ? '🚗' : '👤';
  }

  getStatusColor(handled: boolean): string {
    return handled ? 'status-handled' : 'status-unhandled';
  }
}
