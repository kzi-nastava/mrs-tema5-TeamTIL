import { Routes } from '@angular/router';
import { UserProfile } from './profile/user-profile/user-profile';
import { DriverProfileComponent } from './profile/driver-profile/driver-profile';
import { AdminProfileComponent } from './profile/admin-profile/admin-profile';
import { DriverHistory } from './rides/ride-history/driver-history/driver-history';
import { UserRideHistory } from './rides/ride-history/user-ride-history/user-ride-history';
import { AdminRideHistory } from './rides/ride-history/admin-ride-history/admin-ride-history';
import { AssignedRides } from './rides/assigned-rides/assigned-rides';
import { MainLayoutComponent } from './layout/main-layout/main-layout';
import { authGuard } from './guards/auth.guard';
import { ChangePasswordComponent } from './change-password/change-password';
import { NewPasswordComponent } from './new-password/new-password';

export const routes: Routes = [
  // Routes with layout (navbar + footer)
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      // Protected routes
      { path: 'driver-history', component: DriverHistory, canActivate: [authGuard] },
      { path: 'assigned-rides', component: AssignedRides, canActivate: [authGuard] },
      { path: 'user-ride-history', component: UserRideHistory, canActivate: [authGuard] },
      { path: 'admin-ride-history', component: AdminRideHistory, canActivate: [authGuard] },
      { path: 'user-profile', component: UserProfile, canActivate: [authGuard] },
      { path: 'driver-profile', component: DriverProfileComponent, canActivate: [authGuard] },
      { path: 'admin-profile', component: AdminProfileComponent, canActivate: [authGuard] },
      { path: 'change-password', component: ChangePasswordComponent },
      { path: 'new-password', component: NewPasswordComponent },
      // Public routes can be added here (book, favorites, support, etc.)
    ]
  },
  // Routes without layout (forms only)
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./forms/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./forms/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: '**', redirectTo: '' }
];