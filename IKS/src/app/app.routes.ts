import { Routes } from '@angular/router';
import { UserProfile } from './profile/user-profile/user-profile';
import { DriverProfileComponent } from './profile/driver-profile/driver-profile';
import { AdminProfileComponent } from './profile/admin-profile/admin-profile';
import { DriverHistory } from './rides/ride-history/driver-ride-history/driver-ride-history';
import { UserRideHistory } from './rides/ride-history/user-ride-history/user-ride-history';
import { AdminRideHistory } from './rides/ride-history/admin-ride-history/admin-ride-history';
import { AssignedRides } from './rides/assigned-rides/assigned-rides';
import { RideDetailsComponent } from './rides/ride-details/ride-details';
import { MainLayoutComponent } from './layout/main-layout/main-layout';
import { Home } from './layout/home/home';
import { authGuard } from './guards/auth.guard';
import { ChangePasswordComponent } from './change-password/change-password';
import { NewPasswordComponent } from './new-password/new-password';
import { DriverRegistrationComponent } from './driver-registration/driver-registration';
import { VehicleRegistrationComponent } from './vehicle-registration/vehicle-registration';
import { RideBooking } from './ride-booking/ride-booking';
import { TrackRide } from './rides/track-ride/track-ride';
import { RideReportComponent } from './rides/ride-report/ride-report.component';
import { PriceConfig } from './price-config/price-config';
import { AdminPanicAlertsComponent } from './shared/admin-panic-alerts/admin-panic-alerts';
import { adminGuard } from './guards/admin.guard';
import { driverGuard } from './guards/driver.guard';
import { userGuard } from './guards/user.guard';
import { Support } from './support/support';
import { ActiveRides } from './active-rides/active-rides'

export const routes: Routes = [
  // Routes with layout (navbar + footer)
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', component: Home },
      // Protected routes
      { path: 'assigned-rides', component: AssignedRides, canActivate: [driverGuard] },
      { path: 'ride-details/:id', component: RideDetailsComponent, canActivate: [authGuard] },
      { path: 'driver-ride-history', component: DriverHistory, canActivate: [driverGuard] },
      { path: 'user-ride-history', component: UserRideHistory, canActivate: [userGuard] },
      { path: 'admin-ride-history', component: AdminRideHistory, canActivate: [adminGuard] },
      { path: 'admin-panic-alerts', component: AdminPanicAlertsComponent, canActivate: [adminGuard] },
      { path: 'user-profile', component: UserProfile, canActivate: [userGuard] },
      { path: 'driver-profile', component: DriverProfileComponent, canActivate: [driverGuard] },
      { path: 'admin-profile', component: AdminProfileComponent, canActivate: [adminGuard] },
      { path: 'price-config', component: PriceConfig, canActivate: [adminGuard] },
      { path: 'book', component: RideBooking, canActivate: [userGuard] },
      { path: 'driver-registration', component: DriverRegistrationComponent, canActivate: [adminGuard] },
      { path: 'vehicle-registration', component: VehicleRegistrationComponent, canActivate: [adminGuard] },
      { path: 'track-ride/:id', component: TrackRide, canActivate: [authGuard] },
      { path: 'report', component: RideReportComponent, canActivate: [authGuard] },
      { path: 'support', component: Support, canActivate: [authGuard] },
      { path: 'active-rides', component: ActiveRides, canActivate: [adminGuard] },
      // Public routes can be added here (book, favorites, support, etc.)
      { path: 'change-password', component: ChangePasswordComponent },
      { path: 'new-password', component: NewPasswordComponent },
    ]
  },
  // Routes without layout (forms only)
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./forms/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./forms/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: 'new-password', component: NewPasswordComponent },
  { path: '**', redirectTo: '' }
];