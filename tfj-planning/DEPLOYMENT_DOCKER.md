# Déploiement de l'application TFJ Planning

## Prérequis

- Docker et Docker Compose installés
- Au moins 2GB de RAM disponible
- Ports 5432, 8080 et 4200 disponibles

## Démarrage rapide

### 1. Lancer tous les services

```bash
docker-compose up -d --build
```

Cette commande va :
- Construire l'image du backend Spring Boot
- Construire l'image du frontend Angular
- Démarrer la base de données PostgreSQL
- Initialiser les données de test

### 2. Vérifier l'état des services

```bash
docker-compose ps
```

### 3. Accéder à l'application

- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html

### 4. Arrêter les services

```bash
docker-compose down
```

Pour supprimer également les volumes (base de données) :

```bash
docker-compose down -v
```

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Backend   │────▶│  PostgreSQL │
│   Angular   │     │ Spring Boot │     │   Database  │
│   Port 4200 │     │   Port 8080 │     │   Port 5432 │
└─────────────┘     └─────────────┘     └─────────────┘
```

## Variables d'environnement

### Backend

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DATABASE_URL` | URL de connexion à la base de données | `jdbc:postgresql://postgres:5432/tfj_planning` |
| `DATABASE_USERNAME` | Utilisateur de la base de données | `postgres` |
| `DATABASE_PASSWORD` | Mot de passe de la base de données | `postgres` |
| `JWT_SECRET` | Clé secrète pour JWT | `your-secret-key-change-in-production` |
| `SPRING_PROFILES_ACTIVE` | Profil Spring | `dev` |

### Frontend

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `API_URL` | URL de l'API backend | `http://backend:8080` |

## Données d'initialisation

Le script `init.sql` crée automatiquement :

- **3 Services** : Applications, Infrastructure, Exploitation
- **6 Rôles** : Administrateur réseau, Développeur, Administrateur système, DBA, Chef de projet, Technicien support
- **3 Niveaux hiérarchiques** : Cadre, Manager, Collaborateur
- **9 Employés** répartis dans les différents services
- **12 Jours fériés** pour l'année 2026
- **2 Demi-journées fériées**

## Logs

### Voir les logs de tous les services

```bash
docker-compose logs -f
```

### Voir les logs d'un service spécifique

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

## Résolution de problèmes

### Le backend ne démarre pas

Vérifiez que PostgreSQL est bien démarré :

```bash
docker-compose logs postgres
```

### Erreur de connexion à la base de données

Assurez-vous que le healthcheck PostgreSQL est passé :

```bash
docker-compose ps
```

### Le frontend n'affiche pas les données

1. Vérifiez que le backend est accessible : http://localhost:8080/swagger-ui.html
2. Consultez les logs du frontend : `docker-compose logs frontend`
3. Vérifiez la console du navigateur (F12)

## Développement local

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
ng serve
```

## Production

Pour un déploiement en production :

1. Modifiez le mot de passe PostgreSQL dans `docker-compose.yml`
2. Changez la clé JWT dans `docker-compose.yml`
3. Configurez un reverse proxy (Nginx, Traefik)
4. Activez HTTPS
5. Sauvegardez régulièrement les volumes PostgreSQL

## Maintenance

### Sauvegarde de la base de données

```bash
docker-compose exec postgres pg_dump -U postgres tfj_planning > backup.sql
```

### Restauration

```bash
cat backup.sql | docker-compose exec -T postgres psql -U postgres tfj_planning
```

### Mise à jour

```bash
docker-compose pull
docker-compose up -d --build
```
