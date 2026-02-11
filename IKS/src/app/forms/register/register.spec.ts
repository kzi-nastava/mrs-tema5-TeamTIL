import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Register } from './register';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { vi } from 'vitest';

/**
 * UNIT TESTS FOR FUNCTIONALITY 2.2.2: USER REGISTRATION
 * 
 * This test suite covers:
 * 1. Form validation (required fields, email format, password length)
 * 2. Password matching
 * 3. Sending data to backend (AuthService.register call)
 * 4. Error handling (displaying messages when backend returns error)
 * 5. Navigation after successful/unsuccessful registration
 */
describe('Register - Functionality 2.2.2: User Registration', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authService: AuthService;
  let router: Router;
  let snackBar: MatSnackBar;

  beforeEach(async () => {
    const mockSnackBar = {
      open: vi.fn().mockReturnValue({} as any)
    };

    await TestBed.configureTestingModule({
      imports: [Register, BrowserAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    snackBar = TestBed.inject(MatSnackBar);
    
    await fixture.whenStable();
    fixture.detectChanges();
  });

  // ==================== GROUP 1: BASIC TESTING ====================
  
  it('TEST 1: Should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('TEST 2: Should initialize the form with all fields', () => {
    expect(component.registerForm).toBeDefined();
    expect(component.registerForm.get('name')).toBeDefined();
    expect(component.registerForm.get('surname')).toBeDefined();
    expect(component.registerForm.get('email')).toBeDefined();
    expect(component.registerForm.get('password')).toBeDefined();
    expect(component.registerForm.get('repeatPassword')).toBeDefined();
    expect(component.registerForm.get('address')).toBeDefined();
    expect(component.registerForm.get('phoneNumber')).toBeDefined();
  });

  // ==================== GROUP 2: REQUIRED FIELDS VALIDATION ====================
  
  describe('Required fields validation', () => {
    it('TEST 3: Form should be invalid when empty', () => {
      expect(component.registerForm.valid).toBeFalsy();
    });

    it('TEST 4: "name" field is required', () => {
      const nameControl = component.registerForm.get('name');
      
      // Empty value should trigger required error
      nameControl?.setValue('');
      expect(nameControl?.hasError('required')).toBeTruthy();
      expect(nameControl?.valid).toBeFalsy();

      // Valid value should remove required error
      nameControl?.setValue('Marko');
      expect(nameControl?.hasError('required')).toBeFalsy();
      expect(nameControl?.valid).toBeTruthy();
    });

    it('TEST 5: "surname" field is required', () => {
      const surnameControl = component.registerForm.get('surname');
      
      surnameControl?.setValue('');
      expect(surnameControl?.hasError('required')).toBeTruthy();

      surnameControl?.setValue('Marković');
      expect(surnameControl?.valid).toBeTruthy();
    });

    it('TEST 6: "address" field is required', () => {
      const addressControl = component.registerForm.get('address');
      
      addressControl?.setValue('');
      expect(addressControl?.hasError('required')).toBeTruthy();

      addressControl?.setValue('Bulevar Oslobođenja 15, Novi Sad');
      expect(addressControl?.valid).toBeTruthy();
    });

    it('TEST 7: "phoneNumber" field is required', () => {
      const phoneControl = component.registerForm.get('phoneNumber');
      
      phoneControl?.setValue('');
      expect(phoneControl?.hasError('required')).toBeTruthy();

      phoneControl?.setValue('+381641234567');
      expect(phoneControl?.valid).toBeTruthy();
    });
  });

  // ==================== GROUP 3: EMAIL VALIDATION ====================
  
  describe('Email validation', () => {
    it('TEST 8: Email field is required', () => {
      const emailControl = component.registerForm.get('email');
      emailControl?.setValue('');
      expect(emailControl?.hasError('required')).toBeTruthy();
    });

    it('TEST 9: Should reject invalid email address', () => {
      const emailControl = component.registerForm.get('email');
      
      // Invalid email formats
      emailControl?.setValue('nevazeca-email');
      expect(emailControl?.hasError('email')).toBeTruthy();

      emailControl?.setValue('test@');
      expect(emailControl?.hasError('email')).toBeTruthy();

      emailControl?.setValue('@example.com');
      expect(emailControl?.hasError('email')).toBeTruthy();
    });

    it('TEST 10: Should accept valid email address', () => {
      const emailControl = component.registerForm.get('email');
      
      emailControl?.setValue('marko.markovic@example.com');
      expect(emailControl?.hasError('email')).toBeFalsy();
      expect(emailControl?.valid).toBeTruthy();
    });
  });

  // ==================== GROUP 4: PASSWORD VALIDATION ====================
  
  describe('Password validation', () => {
    it('TEST 11: "password" field is required', () => {
      const passwordControl = component.registerForm.get('password');
      passwordControl?.setValue('');
      expect(passwordControl?.hasError('required')).toBeTruthy();
    });

    it('TEST 12: Password must have at least 6 characters', () => {
      const passwordControl = component.registerForm.get('password');
      
      // Password with fewer than 6 characters
      passwordControl?.setValue('12345');
      expect(passwordControl?.hasError('minlength')).toBeTruthy();
      expect(passwordControl?.valid).toBeFalsy();

      // Password with exactly 6 characters
      passwordControl?.setValue('123456');
      expect(passwordControl?.hasError('minlength')).toBeFalsy();
    });

    it('TEST 13: Password with 6+ characters is valid', () => {
      const passwordControl = component.registerForm.get('password');
      passwordControl?.setValue('sigurnaLozinka123');
      expect(passwordControl?.valid).toBeTruthy();
    });
  });

  // ==================== GROUP 5: PASSWORD MATCHING VALIDATION ====================
  
  describe('Password matching validation', () => {
    it('TEST 14: Form is valid when passwords match', () => {
      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'test@example.com',
        password: 'lozinka123',
        repeatPassword: 'lozinka123',
        address: 'Adresa 1',
        phoneNumber: '+381641234567'
      });

      expect(component.registerForm.hasError('passwordMismatch')).toBeFalsy();
      expect(component.registerForm.valid).toBeTruthy();
    });

    it('TEST 15: Form is invalid when passwords do NOT match', () => {
      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'test@example.com',
        password: 'lozinka123',
        repeatPassword: 'drugaLozinka',
        address: 'Adresa 1',
        phoneNumber: '+381641234567'
      });

      expect(component.registerForm.hasError('passwordMismatch')).toBeTruthy();
      expect(component.registerForm.valid).toBeFalsy();
    });
  });

  // ==================== GROUP 6: FORM SUBMISSION (MOST IMPORTANT!) ====================
  
  describe('Sending data to backend (onSubmit method)', () => {
    
    it('TEST 16: Should not submit form if invalid', () => {
      const registerSpy = vi.spyOn(authService, 'register');
      
      // Form with invalid email
      component.registerForm.patchValue({
        name: 'Marko',
        email: 'nevalidan-email' // Invalid email format
      });
      
      fixture.detectChanges();

      component.onSubmit();

      // Verifies that register was NOT called because form is invalid
      expect(registerSpy).not.toHaveBeenCalled();
      // Verifies that isLoading remains false
      expect(component.isLoading).toBe(false);
    });

    it('TEST 17: Should not submit form if passwords do not match', () => {
      const registerSpy = vi.spyOn(authService, 'register');
      
      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'marko@example.com',
        password: 'lozinka123',
        repeatPassword: 'drugaLozinka', // Different password
        address: 'Bulevar 15',
        phoneNumber: '+381641234567'
      });
      
      fixture.detectChanges();

      component.onSubmit();

      // Verifies that register was NOT called because passwords do not match
      expect(registerSpy).not.toHaveBeenCalled();
      // Verifies that isLoading remains false
      expect(component.isLoading).toBe(false);
    });

    it('TEST 18: Sends data to backend when form is valid', () => {
      // Mock service call
      vi.spyOn(authService, 'register').mockReturnValue(
        of({ 
          email: 'marko@example.com',
          userType: 'REGISTERED_USER',
          message: 'Registration successful' 
        })
      );
      vi.spyOn(authService, 'login').mockReturnValue(
        of({ 
          token: 'mock-token',
          userType: 'REGISTERED_USER',
          email: 'marko@example.com',
          name: 'Marko',
          message: 'Login successful'
        })
      );
      
      // Fill form with valid data
      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'marko@example.com',
        password: 'sigurnaLozinka123',
        repeatPassword: 'sigurnaLozinka123',
        address: 'Bulevar Oslobođenja 15',
        phoneNumber: '+381641234567'
      });

      component.onSubmit();

      // Verify that register was called
      expect(authService.register).toHaveBeenCalled();
      
      // Verify the data that was sent
      const callArgs = (authService.register as any).mock.calls[0][0];
      expect(callArgs.name).toBe('Marko');
      expect(callArgs.surname).toBe('Marković');
      expect(callArgs.email).toBe('marko@example.com');
      expect(callArgs.password).toBe('sigurnaLozinka123');
      expect(callArgs.userType).toBe('REGISTERED_USER');
    });

    it('TEST 19: Calls AuthService.register with correct data', () => {
      const registerSpy = vi.spyOn(authService, 'register').mockReturnValue(
        of({ 
          email: 'test@example.com',
          userType: 'REGISTERED_USER',
          message: 'Success' 
        })
      );
      vi.spyOn(authService, 'login').mockReturnValue(
        of({ 
          token: 'token',
          userType: 'REGISTERED_USER',
          email: 'test@example.com',
          name: 'Test',
          message: 'Login successful'
        })
      );

      component.registerForm.patchValue({
        name: 'Test',
        surname: 'User',
        email: 'test@example.com',
        password: 'password123',
        repeatPassword: 'password123',
        address: 'Address 1',
        phoneNumber: '+381123456'
      });
      
      component.onSubmit();
      
      // Verify that register was called with correct data
      expect(registerSpy).toHaveBeenCalledWith({
        name: 'Test',
        surname: 'User',
        email: 'test@example.com',
        password: 'password123',
        phoneNumber: '+381123456',
        city: 'Address 1',
        userType: 'REGISTERED_USER',
        profilePictureUrl: undefined
      });
    });

    it('TEST 20: Navigates to /user-profile after successful registration and login', () => {
      vi.spyOn(authService, 'register').mockReturnValue(
        of({ 
          email: 'marko@example.com',
          userType: 'REGISTERED_USER',
          message: 'Registration successful' 
        })
      );
      vi.spyOn(authService, 'login').mockReturnValue(
        of({ 
          token: 'test-token',
          userType: 'REGISTERED_USER',
          email: 'marko@example.com',
          name: 'Marko',
          message: 'Login successful'
        })
      );
      vi.spyOn(router, 'navigate');

      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'marko@example.com',
        password: 'lozinka123',
        repeatPassword: 'lozinka123',
        address: 'Adresa',
        phoneNumber: '+381123456'
      });

      component.onSubmit();

      expect(router.navigate).toHaveBeenCalledWith(['/user-profile']);
    });

    it('TEST 21: Navigates to /login if auto-login fails', () => {
      vi.spyOn(authService, 'register').mockReturnValue(
        of({ 
          email: 'marko@example.com',
          userType: 'REGISTERED_USER',
          message: 'Registration successful' 
        })
      );
      vi.spyOn(authService, 'login').mockReturnValue(
        throwError(() => ({ status: 401 }))
      );
      vi.spyOn(router, 'navigate');

      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'marko@example.com',
        password: 'lozinka123',
        repeatPassword: 'lozinka123',
        address: 'Adresa',
        phoneNumber: '+381123456'
      });

      component.onSubmit();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('TEST 22: Sets isLoading to false after registration error', async () => {
      vi.spyOn(authService, 'register').mockReturnValue(
        throwError(() => ({ 
          error: { message: 'Email već postoji u sistemu' } 
        }))
      );
      vi.spyOn(router, 'navigate');

      component.registerForm.patchValue({
        name: 'Marko',
        surname: 'Marković',
        email: 'postojeci@example.com',
        password: 'lozinka123',
        repeatPassword: 'lozinka123',
        address: 'Adresa',
        phoneNumber: '+381123456'
      });
      
      fixture.detectChanges();

      expect(component.isLoading).toBe(false);
      component.onSubmit();
      
      await new Promise(resolve => setTimeout(resolve, 100)); // Wait for async operations

      // Verify that isLoading is set back to false after error
      expect(component.isLoading).toBe(false);
      // Should not navigate  
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  // ==================== GROUP 7: PHOTO HANDLING ====================
  
  describe('Profile photo handling', () => {
    it('TEST 23: Initially no selected photo', () => {
      expect(component.selectedPhoto).toBeNull();
      expect(component.photoPreview).toBeNull();
    });

    it('TEST 24: Removes photo when onRemovePhoto is called', () => {
      component.selectedPhoto = new File(['test'], 'test.jpg');
      component.photoPreview = 'data:image/jpeg;base64,mockdata';

      component.onRemovePhoto();

      expect(component.selectedPhoto).toBeNull();
      expect(component.photoPreview).toBeNull();
    });
  });

  // ==================== GROUP 8: GETTER FUNCTION ====================
  
  it('TEST 25: Getter "f" provides access to form controls', () => {
    expect(component.f['name']).toBeDefined();
    expect(component.f['email']).toBeDefined();
    expect(component.f['password']).toBeDefined();
    expect(component.f['surname']).toBeDefined();
    expect(component.f['address']).toBeDefined();
    expect(component.f['phoneNumber']).toBeDefined();
    expect(component.f['repeatPassword']).toBeDefined();
  });
});
