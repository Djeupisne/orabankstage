# TFJ Planning - Application de Planification des Travaux de Fin de Journée

Application complète pour la planification automatique des TFJ (Travaux de Fin de Journée) et des permanences à la DSI d'Orabank Togo.

## 📋 Fonctionnalités

### Règles métier implémentées :
1. **Non-successivité** : Les membres d'un même groupe ne peuvent pas se suivre
2. **Rotation anti-chronologique** : Si un membre est affecté un jour J cette semaine, la semaine suivante il prend le jour J-1
3. **Membres solos** : Les membres seuls dans leur groupe sont programmés uniquement les vendredis (TFJ) ou samedis (permanence)

### Gestion :
- Jours fériés (complets et demi-journées)
- Services DSI (Application, Infrastructure, Exploitation)
- Rôles (Administrateur réseau, Développeur, DBA, etc.)
- Niveaux hiérarchiques (Cadres, Managers)

## 🏗️ Architecture

### Backend
- **Framework** : Spring Boot 3.2
- **Langage** : Java 17
- **Base de données** : PostgreSQL / Oracle
- **Build** : Maven
- **Documentation API** : Swagger/OpenAPI

### Frontend
- **Framework** : Angular 17
- **Langage** : TypeScript
- **Style** : CSS moderne avec design responsive

## 🚀 Déploiement sur Render

### Option 1 : Déploiement automatique avec render.yaml

```bash
# Le fichier render.yaml configure automatiquement :
# - Un service web Java pour le backend
# - Une base de données PostgreSQL
# - Les variables d'environnement nécessaires
```

1. Connectez-vous à [Render](https://render.com)
2. Cliquez sur "New +" → "Blueprint"
3. Connectez votre repository Git
4. Sélectionnez le fichier `render.yaml`
5. Déployez !

### Option 2 : Déploiement manuel

#### Backend Spring Boot

1. **Créer un service Web sur Render** :
   - Type : Java
   - Root Directory : `backend`
   - Build Command : `mvn clean package -DskipTests`
   - Start Command : `java -jar target/tfj-planning-1.0.0.jar`

2. **Créer une base de données PostgreSQL** :
   - Nom : `tfj-planning-db`
   - Plan : Starter (gratuit)

3. **Variables d'environnement** :
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=<fourni par Render>
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=<votre mot de passe>
   JWT_SECRET=<générer une clé secrète 32+ caractères>
   ```

#### Frontend Angular

1. **Construire l'application** :
   ```bash
   cd frontend
   npm install
   ng build --configuration production
   ```

2. **Déployer comme site statique** :
   - Type : Static Site
   - Root Directory : `frontend`
   - Build Command : `npm install && ng build --configuration production`
   - Publish Directory : `dist/tfj-planning-frontend`

3. **Configurer le proxy API** :
   - Ajouter un fichier `ng serve --proxy-config proxy.conf.json` pour le développement
   - Pour la production, configurer les redirections d'URL vers le backend

## 📖 Utilisation de l'API

### Générer un planning

```http
GET /api/planning/generate?startDate=2024-01-01&endDate=2024-01-31
```

**Réponse** :
```json
[
  {
    "id": 1,
    "employeeId": 5,
    "employeeFullName": "Jean Dupont",
    "employeeEmail": "jean.dupont@orabank.tg",
    "roleName": "Développeur",
    "serviceName": "Application",
    "date": "2024-01-08",
    "dayOfWeek": "MONDAY",
    "type": "TFJ",
    "notes": null,
    "isConfirmed": false,
    "isSoloInGroup": false
  }
]
```

## 🗄️ Modèle de données

### Tables principales :
- **employees** : Personnel de la DSI
- **roles** : Rôles/fonctions (Développeur, DBA, etc.)
- **services** : Services (Application, Infrastructure, Exploitation)
- **hierarchical_levels** : Niveaux hiérarchiques
- **schedules** : Planning des affectations
- **non_working_days** : Jours fériés et demi-journées

## 🔧 Développement local

### Backend

```bash
cd backend

# Lancer avec PostgreSQL local
docker run --name tfj-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=tfj_planning -p 5432:5432 -d postgres

# Démarrer l'application
mvn spring-boot:run
```

L'API sera disponible sur : http://localhost:8080
Swagger UI : http://localhost:8080/swagger-ui.html

### Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Lancer en mode développement
ng serve
```

L'application sera disponible sur : http://localhost:4200

## 📝 Scripts SQL d'initialisation

Des données de démonstration peuvent être insérées via le fichier `data.sql` (à créer dans `src/main/resources`).

## 🔐 Sécurité

- CORS configuré pour accepter les requêtes du frontend
- JWT prêt à être implémenté pour l'authentification
- Validation des entrées utilisateur

## 📊 Tableau de bord

L'interface permet de :
- Sélectionner une période (date de début/fin)
- Générer automatiquement le planning selon les règles métier
- Visualiser les affectations dans un tableau clair
- Identifier les membres solos (programmés vendredi/samedi uniquement)
- Distinguer TFJ (Lundi-Vendredi) et Permanences (Samedi)

## 🤝 Support

Pour toute question ou problème, contactez la DSI d'Orabank Togo.

---

**© 2024 Orabank Togo - Tous droits réservés**
