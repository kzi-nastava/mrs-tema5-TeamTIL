import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { DriverDataService } from '../services/driver-data';

@Component({
  selector: 'app-driver-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule, CommonModule, MatInputModule, 
    MatFormFieldModule, MatButtonModule
  ],
  templateUrl: './driver-registration.html',
  styleUrl: './driver-registration.css'
})
export class DriverRegistrationComponent implements OnInit {
  registerForm: FormGroup;
  selectedPhoto: File | null = null;
  photoPreview: string | null = null;

  constructor(
    private fb: FormBuilder, 
    private cdr: ChangeDetectorRef, 
    private router: Router,
    private driverDataService: DriverDataService
  ) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      address: ['', Validators.required],
      phoneNumber: ['', Validators.required]
    });
  }

  ngOnInit() {
    // ako smo se vratili sa back, popunjavamo formu starim podacima
    const savedData = this.driverDataService.getDriverData();
    const savedPhoto = this.driverDataService.getPhotoPreview();

    if (savedData) {
      this.registerForm.patchValue(savedData);
    }
    if (savedPhoto) {
      this.photoPreview = savedPhoto;
    }
  }

  get f() { return this.registerForm.controls; }

  onPhotoSelect(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedPhoto = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.photoPreview = e.target?.result as string;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }
  }

  onRemovePhoto() {
    this.selectedPhoto = null;
    this.photoPreview = null;
  }

  onNext() {
    if (this.registerForm.valid) {
      this.driverDataService.setDriverData(this.registerForm.value, this.photoPreview);
      this.router.navigate(['/vehicle-registration']);
    }
  }
}