import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-new-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatInputModule, MatFormFieldModule, MatButtonModule],
  templateUrl: './new-password.html',
  styleUrl: './new-password.css'
})
export class NewPasswordComponent {
  resetForm: FormGroup;

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private authService: AuthService
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
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

    const oldPassword = this.authService.getOldPassword();
    const newPassword = this.resetForm.value.newPassword;

    if (!oldPassword) {
      alert("Session expired, please start over.");
      this.router.navigate(['/change-password']);
      return;
    }

    this.authService.changePassword(oldPassword, newPassword).subscribe({
      next: (response: any) => {
        alert("Success: " + response);
        this.router.navigate(['/my-profile']);
      },
      error: (err: any) => {
        // err.error sadrzi poruku sa backenda "Old password is incorrect"
        alert("Error: " + (err.error || "Something went wrong")); 
        this.router.navigate(['/change-password']);
      }
    });
  }
}
