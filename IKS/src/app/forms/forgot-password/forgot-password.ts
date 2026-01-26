import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule, MatInputModule, MatFormFieldModule, MatButtonModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  forgotForm: FormGroup;
  emailSent: boolean = false;

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get f() {
    return this.forgotForm.controls;
  }

  onSubmit() {
    if (this.forgotForm.invalid) return;
    const email = this.forgotForm.value.email;
    this.authService.sendPasswordResetEmail(email).subscribe({
      next: () => {
        this.emailSent = true;
      },
      error: (err) => {
        // Prikazati poruku o grešci po potrebi
        alert('Došlo je do greške pri slanju emaila. Pokušajte ponovo.');
      }
    });
  }

  onCancel() {
    window.history.back();
  }
}
