import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  forgotForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotForm.invalid) return;
    console.log('Reset password for:', this.forgotForm.value.email);
    this.router.navigate(['/reset-password']);
  }

  onCancel() {
    window.history.back();
  }
}
