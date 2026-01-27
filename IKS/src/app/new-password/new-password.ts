import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-new-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatInputModule, MatFormFieldModule, MatButtonModule],
  templateUrl: './new-password.html',
  styleUrl: './new-password.css'
})
export class NewPasswordComponent implements OnInit {
  resetForm: FormGroup;
  activationToken: string | null = null;

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.activationToken = params['token'];
      if (this.activationToken) {
        console.log("Activation mode detected. Token:", this.activationToken);
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  get f() {
    return this.resetForm.controls;
  }

  onSubmit() {
    if (this.resetForm.invalid) return;

    const newPassword = this.resetForm.value.newPassword;

    // SCENARIO 1: Aktivacija naloga (imamo token iz URL-a)
    if (this.activationToken) {
      this.authService.activateDriverAccount(this.activationToken, newPassword).subscribe({
        next: (response) => {
          alert("Account activated successfully! Please log in.");
          this.router.navigate(['/login']); // preusmeri na login
        },
        error: (err) => {
          console.error(err);
          alert("Activation failed. Token might be expired.");
        }
      });
    } 
    // SCENARIO 2: Obicna promena sifre
    else {
      const oldPassword = this.authService.getOldPassword();
      
      if (!oldPassword) {
        alert("Session expired or invalid flow.");
        return;
      }

      this.authService.changePassword(oldPassword, newPassword).subscribe({
        next: (response: any) => {
          alert("Password changed successfully!");
          this.router.navigate(['/my-profile']);
        },
        error: (err: any) => {
          alert("Error: " + (err.error || "Something went wrong")); 
        }
      });
    }
  }
}