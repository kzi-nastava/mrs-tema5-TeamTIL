import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterModule,
    CommonModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm: FormGroup;
  selectedPhoto: File | null = null;
  photoPreview: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
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
    if (this.registerForm.invalid) {
      this.snackBar.open('Please fill all required fields correctly.', 'Close', { duration: 3000 });
      return;
    }

    if (this.registerForm.value.password !== this.registerForm.value.repeatPassword) {
      this.snackBar.open('Passwords do not match!', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;

    // Map form data to backend DTO
    const registerData = {
      name: this.registerForm.value.name,
      surname: this.registerForm.value.surname,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      phoneNumber: this.registerForm.value.phoneNumber,
      city: this.registerForm.value.address,
      userType: 'REGISTERED_USER',
      profilePictureUrl: this.photoPreview ? this.photoPreview.replace(/^data:image\/[a-z]+;base64,/, '') : undefined
    };

    console.log('Sending registration data:', {
      ...registerData,
      profilePictureUrl: registerData.profilePictureUrl ? `Base64 string (${registerData.profilePictureUrl.length} chars)` : 'none'
    });

    if (registerData.profilePictureUrl) {
      console.log('First 100 chars of profilePictureUrl (without prefix):', registerData.profilePictureUrl.substring(0, 100));
    }

    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.snackBar.open(response.message + ' Logging you in...', 'Close', { duration: 2000 });
        
        // Automatically log in the user after successful registration
        const loginCredentials = {
          email: this.registerForm.value.email,
          password: this.registerForm.value.password
        };

        this.authService.login(loginCredentials).subscribe({
          next: (loginResponse) => {
            this.isLoading = false;
            this.snackBar.open('Welcome! Registration successful.', 'Close', { duration: 3000 });
            this.router.navigate(['/user-profile']);
          },
          error: (loginError) => {
            this.isLoading = false;
            this.snackBar.open('Registration successful! Please log in.', 'Close', { duration: 3000 });
            this.router.navigate(['/login']);
          }
        });
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Registration failed. Please try again.';
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        console.error('Registration error:', error);
      }
    });
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
