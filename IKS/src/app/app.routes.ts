import { Routes } from '@angular/router';
import { UserProfile } from './profile/user-profile/user-profile';
import { DriverProfileComponent } from './profile/driver-profile/driver-profile';
import { AdminProfileComponent } from './profile/admin-profile/admin-profile';
import { DriverHistory } from './ride-history/driver-history/driver-history';
import { UserRideHistory } from './ride-history/user-ride-history/user-ride-history';
import { AdminRideHistory } from './ride-history/admin-ride-history/admin-ride-history';
import { MainLayoutComponent } from './layout/main-layout/main-layout';

export const routes: Routes = [
  // Routes with layout (navbar + footer)
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'driver-history', component: DriverHistory },
      { path: 'user-ride-history', component: UserRideHistory },
      { path: 'admin-ride-history', component: AdminRideHistory },
      { path: 'user-profile', component: UserProfile },
      { path: 'driver-profile', component: DriverProfileComponent },
      { path: 'admin-profile', component: AdminProfileComponent },
    ]
  },
  // Routes without layout (forms only)
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./forms/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./forms/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];