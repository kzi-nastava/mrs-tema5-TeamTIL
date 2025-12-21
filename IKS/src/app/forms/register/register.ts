import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule, MatInputModule, MatFormFieldModule, MatButtonModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm: FormGroup;
  selectedPhoto: File | null = null;
  photoPreview: string | null = null;

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      surname: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      address: ['', Validators.required],
      repeatPassword: ['', Validators.required],
      phoneNumber: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const repeatPassword = form.get('repeatPassword')?.value;
    return password === repeatPassword ? null : { passwordMismatch: true };
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    if (this.registerForm.invalid) return;
    if (this.registerForm.value.password !== this.registerForm.value.repeatPassword) {
      alert('Passwords do not match');
      return;
    }
    console.log(this.registerForm.value);
    console.log('Selected photo:', this.selectedPhoto);
  }

  onPhotoSelect(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedPhoto = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.photoPreview = e.target?.result as string;
        console.log('Photo preview set:', this.photoPreview);
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }
  }

  onRemovePhoto() {
    this.selectedPhoto = null;
    this.photoPreview = null;
  }
}
