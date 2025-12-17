import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./forms/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./forms/register/register').then(m => m.Register) },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
