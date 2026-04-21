import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  errorMessage: string = '';
  loading: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.errorMessage = '';
    this.loading = true;

    if (!this.username || !this.password) {
      this.errorMessage = 'Veuillez saisir votre identifiant et mot de passe';
      this.loading = false;
      return;
    }

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.role === 'ADMIN') {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (err) => {
        this.loading = false;
        // Messages d'erreur plus précis selon le code HTTP
        if (err.message.includes('401')) {
          this.errorMessage = 'Identifiants incorrects. Veuillez vérifier votre identifiant et mot de passe.';
        } else if (err.message.includes('403')) {
          this.errorMessage = 'Accès refusé. Votre compte n\'a pas les permissions nécessaires pour accéder à cette interface.';
        } else if (err.message.includes('404')) {
          this.errorMessage = 'Le service d\'authentification est indisponible. Veuillez réessayer ultérieurement.';
        } else if (err.message.includes('0')) {
          this.errorMessage = 'Impossible de se connecter au serveur. Vérifiez votre connexion internet.';
        } else {
          this.errorMessage = err.message || 'Échec de la connexion. Veuillez vérifier vos identifiants.';
        }
        console.error('Erreur de connexion:', err);
      }
    });
  }
}
