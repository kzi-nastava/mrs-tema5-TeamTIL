import { Routes } from '@angular/router';
import { UserProfile } from './user-profile/user-profile';
import { DriverHistory } from './pages/driver-history/driver-history';

export const routes: Routes = [
  { path: 'driver-history', component: DriverHistory },
  { path: 'profile', component: UserProfile },
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./forms/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./forms/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
