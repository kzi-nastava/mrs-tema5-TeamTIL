import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackbar = inject(MatSnackBar); 

  const userType = authService.getUserType();
  const isLoggedIn = authService.isLoggedIn();
  const token = authService.getToken();
  const email = authService.getEmail();
  
  console.log('[AdminGuard] Full check:');
  console.log('  - UserType:', userType);
  console.log('  - IsLoggedIn:', isLoggedIn);
  console.log('  - Token exists:', !!token);
  console.log('  - Email:', email);
  console.log('  - localStorage currentUser:', localStorage.getItem('currentUser'));

  if (userType === 'ADMINISTRATOR') {
    console.log('[AdminGuard] ✅ Access GRANTED');
    return true;
  }

  if (!isLoggedIn) {
    console.log('[AdminGuard] ❌ Not logged in, redirecting to /login');
    snackbar.open('You must be logged in to access this page.', 'Close', { duration: 3000 });
    router.navigate(['/login']);
  } else {
    console.log('[AdminGuard] ❌ No admin permission (userType:', userType, '), redirecting to /');
    snackbar.open('You do not have permission to access this page.', 'Close', { duration: 3000 });
    router.navigate(['/']);
  }

  return false;
};