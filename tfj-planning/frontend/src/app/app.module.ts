import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    AppComponent,
    DashboardComponent
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    provideRouter(routes)
  ],
  bootstrap: []
})
export class AppModule {}
