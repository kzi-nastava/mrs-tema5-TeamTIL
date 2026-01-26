import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DriverDataService } from '../services/driver-data';

@Component({
  selector: 'app-vehicle-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule, 
    CommonModule, 
    RouterModule,
    MatInputModule, 
    MatFormFieldModule, 
    MatButtonModule, 
    MatSelectModule, 
    MatCheckboxModule
  ],
  templateUrl: './vehicle-registration.html',
  styleUrl: './vehicle-registration.css'
})
export class VehicleRegistrationComponent implements OnInit {
  vehicleForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private driverDataService: DriverDataService,
    private router: Router
  ) {
    this.vehicleForm = this.fb.group({
      model: ['', Validators.required],
      plate: ['', Validators.required],
      type: ['STANDARD', Validators.required],
      seats: [1, [Validators.required, Validators.min(1)]],
      babyFriendly: [false],
      petFriendly: [false]
    });
  }

  ngOnInit() {
    // ako se vraca sa prve forme ponovo na ovu, vrati unete podatke o vozilu
    const savedVehicle = this.driverDataService.getVehicleData();
    if (savedVehicle) {
      this.vehicleForm.patchValue(savedVehicle);
    }
  }

  get v() { return this.vehicleForm.controls; }

  onBack() {
    this.driverDataService.setVehicleData(this.vehicleForm.value);
    this.router.navigate(['/driver-registration']);
  }

  onSubmit() {
    console.log("KLIKNUT FINISH!");
  if (this.vehicleForm.valid) {
    this.driverDataService.setVehicleData(this.vehicleForm.value);
    
    this.driverDataService.sendRegistration().subscribe({
      next: (response) => {
        alert("Driver and Vehicle successfully registered!");
        this.driverDataService.clearData();
        this.router.navigate(['/driver-registration']);
      },
      error: (err) => {
        console.error("Registration error:", err);
        alert("Failed to register driver. Check console for details.");
      }
    });
  }
  else{
    console.log("FORMA NIJE VALIDNA!");
  }
}
}