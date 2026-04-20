# Backend Java/Spring Boot

## Configuration pour Render

### Variables d'environnement requises sur Render :
- `DATABASE_URL`: jdbc:postgresql://... (fourni automatiquement par Render PostgreSQL)
- `DATABASE_USERNAME`: postgres
- `DATABASE_PASSWORD`: votre_mot_de_passe
- `SPRING_PROFILES_ACTIVE`: prod
- `JWT_SECRET`: votre_clé_secrète_32_caractères_minimum

### Build Command :
```bash
cd backend && mvn clean package -DskipTests
```

### Start Command :
```bash
java -jar backend/target/tfj-planning-1.0.0.jar
```

# Frontend Angular

## Build pour la production :
```bash
cd frontend
npm install
ng build --configuration production
```

## Déploiement statique sur Render :
- Publier le dossier `frontend/dist/tfj-planning-frontend` comme site statique
- Configurer le proxy pour rediriger `/api` vers l'URL du backend
