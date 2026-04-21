import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { Routes } from '@angular/router';

import { AppComponent } from './app/app.component';
import { LoginComponent } from './app/components/login/login.component';
import { HomeComponent } from './app/components/home/home.component';
import { DashboardComponent } from './app/components/dashboard/dashboard.component';
import { AuthGuard, AdminGuard } from './app/guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/home' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    provideRouter(routes),
    AuthGuard,
    AdminGuard
  ]
}).catch(err => console.error(err));
