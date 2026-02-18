import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

export const driverGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackbar = inject(MatSnackBar); 

  if (authService.getUserType() === 'DRIVER') {
    return true;
  }

  if (!authService.isLoggedIn()) {
    snackbar.open('You must be logged in to access this page.', 'Close', { duration: 3000 });
    router.navigate(['/login']);
  } else {
    snackbar.open('You do not have permission to access this page.', 'Close', { duration: 3000 });
    router.navigate(['/']);
  }

  router.navigate(['/']);
  return false;
};