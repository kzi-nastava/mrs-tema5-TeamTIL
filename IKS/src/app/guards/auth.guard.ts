import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, switchMap } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('[AuthGuard] Checking authentication for route:', state.url);

  // First check if user is logged in locally
  if (!authService.isLoggedIn()) {
    console.log('[AuthGuard] User not logged in locally, redirecting to login');
    router.navigate(['/login']);
    return false;
  }

  // Then validate token with backend
  return authService.validateToken().pipe(
    map(response => {
      if (response.valid) {
        console.log('[AuthGuard] Token is valid, allowing access');
        return true;
      } else {
        console.log('[AuthGuard] Token is invalid, redirecting to login');
        router.navigate(['/login']);
        return false;
      }
    })
  );
};
