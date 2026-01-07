import { Routes } from '@angular/router';
import { UserProfile } from './user-profile/user-profile';
import { DriverProfileComponent } from './driver-profile/driver-profile';
import { AdminProfileComponent } from './admin-profile/admin-profile';
import { DriverHistory } from './pages/driver-history/driver-history';
import { UserRideHistory } from './pages/user-ride-history/user-ride-history';

export const routes: Routes = [
  { path: 'driver-history', component: DriverHistory },
  { path: 'user-ride-history', component: UserRideHistory },
  { path: 'user-profile', component: UserProfile },
  { path: 'driver-profile', component: DriverProfileComponent },
  { path: 'admin-profile', component: AdminProfileComponent },
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./forms/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./forms/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];