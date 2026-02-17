import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { RideService } from '../../services/ride.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-rate-ride',
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
  ],
  templateUrl: './rate-ride.html',
  styleUrls: ['./rate-ride.css'],
})
export class RateRideComponent {
  driverRating = 0;
  vehicleRating = 0;
  comment = '';

  constructor(
    private authService : AuthService,
    private rideService: RideService,
    private dialogRef: MatDialogRef<RateRideComponent>,
    @Inject(MAT_DIALOG_DATA) public ride: any,
    private snackBar: MatSnackBar
  ) {}

  setDriverRating(value: number) {
    if (value < 1 || value > 5) return;
    this.driverRating = value;
  }

  setVehicleRating(value: number) {
    if (value < 1 || value > 5) return;
    this.vehicleRating = value;
  }

  submit() {
    if (this.driverRating === 0 || this.vehicleRating === 0) {
      this.snackBar.open('Please rate both driver and vehicle before submitting!', 'Close', { duration: 3000 });
      return;
    }

    if (!this.isWithinDeadline()) {
      this.snackBar.open('Rating deadline has expired!', 'Close', { duration: 3000 });
      return;
    }

    this.authService.currentUser$.subscribe(user => {
      if (!user || !user.email) return;

      const body = {
        userEmail: user.email,
        driverRating: this.driverRating,
        vehicleRating: this.vehicleRating,
        comment: this.comment
      };

      this.rideService.rateRide(this.ride.id, body).subscribe((res: any) => {
        this.dialogRef.close(res);
      }, (error: any) => {
        this.snackBar.open('Failed to submit rating: ' + error.message, 'Close', { duration: 3000 });
      });
    });
  }

  close() {
    this.dialogRef.close();
  }

  isWithinDeadline(): boolean {
    const { date, endTime } = this.ride;

    if (!date || !endTime) {
      console.warn('Missing date or endTime', this.ride);
      return false;
    }

    const combined = `${date} ${endTime}`; // npr. "15 Feb 2026 15:31"
    const rideEndDate = this.parseCustomDate(combined);

    if (!rideEndDate || isNaN(rideEndDate.getTime())) {
      console.warn('Invalid combined date:', combined);
      return false;
    }

    const now = Date.now();
    const threeDaysInMs = 3 * 24 * 60 * 60 * 1000;

    return now - rideEndDate.getTime() <= threeDaysInMs;
  }

  private parseCustomDate(dateString: string): Date {
    const parts = dateString.split(' ');
    if (parts.length !== 4) {
      console.warn('Unexpected date format:', dateString);
      return new Date(dateString);
    }

    const [day, monthStr, year, timePart] = parts;
    const [hours, minutes] = timePart.split(':');

    const months: Record<string, number> = {
      Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
      Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11
    };

    return new Date(
      Number(year),
      months[monthStr],
      Number(day),
      Number(hours),
      Number(minutes)
    );
  }
}
