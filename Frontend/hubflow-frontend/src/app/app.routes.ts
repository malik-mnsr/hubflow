import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { ScanComponent } from './components/scan/scan';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'scan', component: ScanComponent },
  { path: '**', redirectTo: 'dashboard' }
];