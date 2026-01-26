import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule, MatInputModule, MatFormFieldModule, MatButtonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePasswordComponent {
  changeForm: FormGroup;

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private authService: AuthService 
  ) {
    this.changeForm = this.fb.group({
      oldPassword: ['', [Validators.required]]
    });
  }

  get f() {
    return this.changeForm.controls;
  }

  onNext() {
    if (this.changeForm.invalid) return;
    
    this.authService.setOldPassword(this.changeForm.value.oldPassword);
    this.router.navigate(['/new-password']);
  }

  onCancel() {
    window.history.back();
  }
}