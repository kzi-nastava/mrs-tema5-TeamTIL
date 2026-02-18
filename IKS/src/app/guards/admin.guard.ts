import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take, skip } from 'rxjs/operators';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('[adminGuard] userType:', authService.getUserType());

  if (authService.getUserType() === 'ADMINISTRATOR') {
    return true;
  }

  router.navigate(['/']);
  return false;
};