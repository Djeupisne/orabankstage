# Déploiement sur Render - Guide Complet

Ce projet est configuré pour être déployé sur Render avec :
- **Backend** : Spring Boot (Java 17)
- **Frontend** : Angular 17 (Site statique)
- **Base de données** : PostgreSQL

## 📁 Structure du projet

```
tfj-planning/
├── backend/          # Application Spring Boot
├── frontend/         # Application Angular
└── render.yaml       # Configuration de déploiement Render
```

## 🚀 Déploiement Automatique

### Étape 1 : Pousser le code sur Git

Assurez-vous que votre code est sur une branche `main` dans un repository Git (GitHub, GitLab, ou Bitbucket).

```bash
git add .
git commit -m "Configuration pour Render"
git push origin main
```

### Étape 2 : Créer un Blueprint sur Render

1. Connectez-vous à [Render](https://render.com)
2. Cliquez sur **"New +"** → **"Blueprint"**
3. Connectez votre repository Git
4. Sélectionnez le fichier `render.yaml` à la racine du projet
5. Cliquez sur **"Apply"**

Render va automatiquement créer :
- ✅ Un service Web Java pour le backend
- ✅ Un site statique pour le frontend Angular
- ✅ Une base de données PostgreSQL
- ✅ Toutes les variables d'environnement nécessaires

## 🔧 Configuration Détaillée

### Backend Spring Boot

**Service Web Java**
- **Root Directory** : `backend`
- **Build Command** : `mvn clean package -DskipTests`
- **Start Command** : `java -jar target/tfj-planning-1.0.0.jar`
- **Port** : 8080
- **Health Check** : `/actuator/health`

**Variables d'environnement automatiques** :
- `JAVA_VERSION` : 17
- `SPRING_PROFILES_ACTIVE` : prod
- `PORT` : 8080
- `DATABASE_URL` : Fourni par Render PostgreSQL
- `DATABASE_USERNAME` : Fourni par Render PostgreSQL
- `DATABASE_PASSWORD` : Fourni par Render PostgreSQL
- `JWT_SECRET` : Généré automatiquement
- `CORS_ALLOWED_ORIGINS` : URL du frontend (automatique)

### Frontend Angular

**Site Statique**
- **Root Directory** : `frontend`
- **Node Version** : 18
- **Build Command** : `npm install && ng build --configuration production`
- **Publish Directory** : `dist/tfj-planning-frontend`

**Variables d'environnement** :
- `NODE_VERSION` : 18
- `API_BASE_URL` : URL du backend (automatique depuis le service web)

### Base de données PostgreSQL

- **Nom** : tfj-planning-db
- **Database** : tfj_planning
- **User** : tfj_user
- **Plan** : Starter (gratuit - 1GB)
- **Region** : Frankfurt (pour être proche du backend)

## 🌐 URLs après déploiement

Après déploiement réussi, vous aurez :

1. **Frontend** : `https://tfj-planning-frontend.onrender.com`
2. **Backend API** : `https://tfj-planning-backend.onrender.com`
3. **Swagger UI** : `https://tfj-planning-backend.onrender.com/swagger-ui.html`
4. **PostgreSQL** : Connection string fournie dans le dashboard Render

## 🔍 Vérification du déploiement

### Backend

1. Accédez à l'URL du backend
2. Testez l'endpoint : `GET /api/planning/generate?startDate=2024-01-01&endDate=2024-01-31`
3. Vérifiez Swagger : `/swagger-ui.html`

### Frontend

1. Accédez à l'URL du frontend
2. L'application devrait charger et afficher l'interface
3. Testez la génération d'un planning

## ⚙️ Configuration Manuelle (Alternative)

Si vous préférez configurer manuellement :

### 1. Créer la base de données PostgreSQL

- Dashboard Render → New → PostgreSQL
- Nom : `tfj-planning-db`
- Database : `tfj_planning`
- User : `tfj_user`

### 2. Créer le service Backend

- Dashboard Render → New → Web Service
- Connecter le repository Git
- Root Directory : `backend`
- Environment : `Java`
- Build Command : `mvn clean package -DskipTests`
- Start Command : `java -jar target/tfj-planning-1.0.0.jar`

**Variables d'environnement à ajouter** :
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<copier depuis PostgreSQL>
DATABASE_USERNAME=tfj_user
DATABASE_PASSWORD=<mot de passe PostgreSQL>
JWT_SECRET=<générer une clé de 32+ caractères>
CORS_ALLOWED_ORIGINS=https://votre-frontend.onrender.com
```

### 3. Créer le service Frontend

- Dashboard Render → New → Static Site
- Connecter le repository Git
- Root Directory : `frontend`
- Build Command : `npm install && ng build --configuration production`
- Publish Directory : `dist/tfj-planning-frontend`

**Variable d'environnement à ajouter** :
```
API_BASE_URL=https://votre-backend.onrender.com
```

## 🔧 Personnalisation

### Changer la région

Modifiez `region: frankfurt` dans `render.yaml` par :
- `oregon` (US Ouest)
- `ohio` (US Est)
- `frankfurt` (Europe)
- `singapore` (Asie)

### Changer le plan

Pour plus de ressources, changez `plan: starter` par :
- `standard` (plus de RAM/CPU)
- `pro` (production critique)

## 🐛 Résolution de problèmes

### Le frontend ne se connecte pas au backend

1. Vérifiez que `API_BASE_URL` est correct dans le service frontend
2. Vérifiez les logs du frontend sur Render
3. Assurez-vous que le CORS est bien configuré côté backend

### Erreur de connexion à la base de données

1. Vérifiez que `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` sont corrects
2. Redémarrez le service backend
3. Vérifiez les logs du backend

### Build Angular échoue

1. Vérifiez la version de Node (doit être 18+)
2. Exécutez localement : `cd frontend && npm install && ng build --configuration production`
3. Vérifiez les logs de build sur Render

## 📝 Notes importantes

- **Premier démarrage** : Le premier déploiement peut prendre 5-10 minutes
- **Cold starts** : Avec le plan gratuit, le service peut mettre ~30s à démarrer après inactivité
- **Logs** : Consultez les logs dans le dashboard Render pour déboguer
- **Migrations DB** : Hibernate configure automatiquement le schéma (`ddl-auto: update`)

## 🎯 Prochaines étapes

1. Peupler la base de données avec les employés, rôles et services
2. Configurer l'authentification JWT
3. Ajouter des tests automatisés
4. Configurer un domaine personnalisé (optionnel)

---

**Support** : Pour toute question, consultez la [documentation Render](https://render.com/docs)
