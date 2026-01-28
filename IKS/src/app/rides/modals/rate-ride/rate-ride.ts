import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

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
    private dialogRef: MatDialogRef<RateRideComponent>,
    @Inject(MAT_DIALOG_DATA) public ride: any,
    private http: HttpClient
  ) {}

  setDriverRating(value: number) {
    this.driverRating = value;
  }

  setVehicleRating(value: number) {
    this.vehicleRating = value;
  }

  submit() {
    if (this.driverRating === 0 || this.vehicleRating === 0) {
      alert('Please rate both driver and vehicle before submitting!');
      return;
    }

    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    const body = {
      driverRating: this.driverRating,
      vehicleRating: this.vehicleRating,
      comment: this.comment
    };

     this.http.post(
      `http://localhost:8080/api/rides/${this.ride.id}/rate`,
      body,
      { headers }
    ).pipe(
      catchError(err => {
        alert('Failed to submit rating: ' + err.message);
        return throwError(err);
      })
    ).subscribe((res: any) => {
      alert(res.message || 'Rating submitted successfully!');
      this.dialogRef.close(res);
    });

    this.dialogRef.close({
      driverRating: this.driverRating,
      vehicleRating: this.vehicleRating,
      comment: this.comment
    });
  }

  close() {
    this.dialogRef.close();
  }
}
