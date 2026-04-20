# Documentation de Déploiement et de Paramétrage
## Application Crédit en Ligne - ORABANK TOGO

---

## Table des Matières

1. [Introduction](#introduction)
2. [Prérequis Techniques](#prerequis-techniques)
3. [Installation de l'Environnement](#installation-de-lenvironnement)
4. [Configuration de la Base de Données](#configuration-de-la-base-de-donnees)
5. [Paramétrage de l'Application](#parametrage-de-lapplication)
   - 5.1 [Gestion des Permissions](#gestion-des-permissions)
   - 5.2 [Gestion des Profils](#gestion-des-profils)
   - 5.3 [Gestion des Utilisateurs](#gestion-des-utilisateurs)
6. [Configuration des Workflows](#configuration-des-workflows)
7. [Communication avec le Système Bancaire](#communication-avec-le-systeme-bancaire)
8. [Déploiement en Production](#deploiement-en-production)
9. [Maintenance et Surveillance](#maintenance-et-surveillance)
10. [Annexes](#annexes)

---

## 1. Introduction <a name="introduction"></a>

### 1.1 Présentation

**Crédit en Ligne** est une application de workflow documentaire développée avec les technologies suivantes :

**Backend :**
- PHP 8.4+
- Symfony 8.0
- MySQL / Oracle

**Frontend :**
- Angular
- TypeScript
- Spring Boot
- Java 17
- PostgreSQL / Oracle

Cette application permet de digitaliser les demandes de crédit des clients d'ORABANK TOGO. Les clients soumettent leurs demandes via la plateforme, et les gestionnaires les traitent selon des workflows prédéfinis.

### 1.2 Objectif de ce Document

Ce guide a pour objectif de fournir une documentation complète pour :
- Le déploiement de l'application à partir d'un serveur vierge
- Le paramétrage des permissions, profils et utilisateurs
- La configuration des workflows
- L'intégration avec le système bancaire

### 1.3 Public Cible

Ce document s'adresse aux :
- Administrateurs système
- Administrateurs de base de données
- Chefs de projet
- Équipes d'exploitation

---

## 2. Prérequis Techniques <a name="prerequis-techniques"></a>

### 2.1 Serveur Web

#### Configuration Minimale
- **Système d'exploitation** : Linux (Ubuntu 22.04 LTS recommandé) ou Windows Server 2019+
- **Processeur** : 4 cœurs minimum
- **Mémoire RAM** : 8 Go minimum (16 Go recommandé)
- **Espace disque** : 50 Go minimum

#### Logiciels Requis

| Logiciel | Version | Description |
|----------|---------|-------------|
| PHP | 8.4+ | Moteur d'exécution backend |
| Apache/Nginx | Dernière version stable | Serveur web |
| Node.js | 18.x+ | Compilation des assets frontend |
| Composer | 2.6+ | Gestionnaire de dépendances PHP |
| npm/yarn | Dernière version | Gestionnaire de paquets JavaScript |

#### Extensions PHP Requises

```bash
# Extensions obligatoires
ext-ctype
ext-iconv
ext-pdo
ext-pdo_mysql
ext-pdo_oci (pour Oracle)
ext-gd
ext-intl
ext-mbstring
ext-xml
ext-zip
ext-curl
ext-json
ext-tokenizer
ext-fileinfo
ext-session
```

### 2.2 Base de Données

#### Options Supportées

**Option 1 : MySQL/MariaDB**
- MySQL 8.0+ ou MariaDB 10.6+
- Caractères : utf8mb4
- Collation : utf8mb4_unicode_ci

**Option 2 : Oracle**
- Oracle Database 19c+
- Character set : AL32UTF8

**Option 3 : PostgreSQL** (pour certains modules)
- PostgreSQL 14+
- Encodage : UTF8

### 2.3 Environnement Java (pour Spring Boot)

- **JDK** : Java 17 LTS (OpenJDK ou Oracle JDK)
- **Maven** : 3.8+ ou Gradle 7+
- **Spring Boot** : 3.x

---

## 3. Installation de l'Environnement <a name="installation-de-lenvironnement"></a>

### 3.1 Installation de PHP 8.4

#### Sur Ubuntu/Debian

```bash
# Ajouter le repository Ondrej
sudo add-apt-repository ppa:ondrej/php
sudo apt-get update

# Installer PHP 8.4 et les extensions requises
sudo apt-get install -y php8.4 php8.4-cli php8.4-fpm \
    php8.4-mysql php8.4-gd php8.4-intl php8.4-mbstring \
    php8.4-xml php8.4-zip php8.4-curl php8.4-tokenizer \
    php8.4-fileinfo php8.4-bcmath php8.4-soap

# Pour Oracle (nécessite OCI8)
sudo apt-get install -y libaio1 instantclient-basic
# Suivre la documentation Oracle pour PECL oci8
```

#### Vérification

```bash
php -v
php -m | grep -E "(pdo|mysql|oci)"
```

### 3.2 Installation du Serveur Web

#### Apache

```bash
sudo apt-get install -y apache2 libapache2-mod-php8.4

# Activer mod_rewrite
sudo a2enmod rewrite

# Redémarrer Apache
sudo systemctl restart apache2
```

#### Nginx

```bash
sudo apt-get install -y nginx

# Configuration à ajouter dans /etc/nginx/sites-available/crdt_en_ligne
server {
    listen 80;
    server_name votre-domaine.com;
    root /var/www/crdt_en_ligne/public;

    location / {
        try_files $uri /index.php$is_args$args;
    }

    location ~ ^/index\.php(/|$) {
        fastcgi_pass unix:/run/php/php8.4-fpm.sock;
        fastcgi_split_path_info ^(.+\.php)(/.*)$;
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME $realpath_root$fastcgi_script_name;
        fastcgi_param DOCUMENT_ROOT $realpath_root;
        internal;
    }

    location ~ \.php$ {
        return 404;
    }
}
```

### 3.3 Installation de Composer

```bash
cd /tmp
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer
composer --version
```

### 3.4 Installation de Node.js et npm

```bash
# Via NodeSource
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Vérification
node -v
npm -v
```

### 3.5 Clonage du Projet

```bash
# Créer le répertoire d'installation
sudo mkdir -p /var/www/crdt_en_ligne
sudo chown www-data:www-data /var/www/crdt_en_ligne

# Cloner le dépôt (à adapter selon votre SCM)
cd /var/www/crdt_en_ligne
git clone <URL_DU_DEPOT> .

# Ou copier les fichiers sources manuellement
```

### 3.6 Installation des Dépendances

```bash
cd /var/www/crdt_en_ligne

# Installer les dépendances PHP
composer install --no-dev --optimize-autoloader

# Installer les dépendances JavaScript (pour le frontend Angular)
cd frontend  # ou le répertoire contenant le projet Angular
npm install
npm run build --prod
```

---

## 4. Configuration de la Base de Données <a name="configuration-de-la-base-de-donnees"></a>

### 4.1 Création de la Base MySQL

```sql
-- Se connecter à MySQL en tant que root ou administrateur
mysql -u root -p

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS crdt_en_ligne 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Créer l'utilisateur dédié
CREATE USER 'crdt_user'@'localhost' IDENTIFIED BY 'VotreMotDePasseFort!';

-- Accorder les privilèges
GRANT ALL PRIVILEGES ON crdt_en_ligne.* TO 'crdt_user'@'localhost';

-- Appliquer les changements
FLUSH PRIVILEGES;

-- Quitter MySQL
EXIT;
```

### 4.2 Configuration Oracle (Alternative)

```sql
-- Se connecter en tant que SYSDBA
sqlplus / as sysdba

-- Créer le tablespace
CREATE TABLESPACE crdt_tbs 
    DATAFILE 'crdt_tbs.dbf' SIZE 1G 
    AUTOEXTEND ON NEXT 100M MAXSIZE UNLIMITED;

-- Créer l'utilisateur
CREATE USER crdt_user IDENTIFIED BY "VotreMotDePasseFort!"
    DEFAULT TABLESPACE crdt_tbs
    QUOTA UNLIMITED ON crdt_tbs;

-- Accorder les privilèges
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE PROCEDURE TO crdt_user;
GRANT EXECUTE ON DBMS_LOCK TO crdt_user;

-- Quitter
EXIT;
```

### 4.3 Configuration du Fichier .env

```bash
cd /var/www/crdt_en_ligne
cp .env .env.local
```

Éditer `.env.local` :

```ini
###> symfony/framework-bundle ###
APP_ENV=prod
APP_SECRET=votre_clef_secrete_generee_aleatoirement_32_caracteres
###< symfony/framework-bundle ###

###> doctrine/doctrine-bundle ###
# Configuration MySQL
DATABASE_URL="mysql://crdt_user:VotreMotDePasseFort!@127.0.0.1:3306/crdt_en_ligne?serverVersion=8.0&charset=utf8mb4"

# OU Configuration Oracle
# DATABASE_URL="oracle://crdt_user:VotreMotDePasseFort!@localhost:1521/ORCL"
###< doctrine/doctrine-bundle ###

###> Configuration spécifique ORABANK ###
# URL du système bancaire core
BANKING_SYSTEM_URL=https://api-core.orabank.tg
BANKING_SYSTEM_API_KEY=votre_clef_api_bancaire
BANKING_SYSTEM_TIMEOUT=30

# Configuration des emails
MAILER_DSN=smtp://smtp.orabank.tg:587
MAILER_FROM=noreply@orabank.tg

# Configuration des logs
LOG_LEVEL=info
LOG_PATH=/var/log/crdt_en_ligne/
```

### 4.4 Génération de la Clef Secrète

```bash
# Générer une clef secrète aléatoire
php bin/console secrets:generate-keys
# Ou manuellement
openssl rand -hex 16
```

### 4.5 Exécution des Migrations

```bash
cd /var/www/crdt_en_ligne

# Mettre à jour la base de données avec le schéma
php bin/console doctrine:migrations:migrate --no-interaction

# OU si pas de migrations existantes, créer le schéma
php bin/console doctrine:schema:update --force
```

### 4.6 Chargement des Données de Référence

```bash
# Charger les données de référence (permissions, profils, etc.)
php bin/console app:load-reference-data

# Nettoyer le cache
php bin/console cache:clear --env=prod
```

---

## 5. Paramétrage de l'Application <a name="parametrage-de-lapplication"></a>

### 5.1 Gestion des Permissions <a name="gestion-des-permissions"></a>

#### 5.1.1 Structure des Permissions

Les permissions sont organisées par module :

| Module | Code | Description |
|--------|------|-------------|
| Gestion des demandes | `DEMANDE_*` | CRUD sur les demandes de crédit |
| Workflow | `WORKFLOW_*` | Gestion des étapes et transitions |
| Utilisateurs | `USER_*` | Gestion des comptes utilisateurs |
| Profils | `PROFILE_*` | Gestion des rôles et profils |
| Reporting | `REPORT_*` | Accès aux rapports et statistiques |
| Administration | `ADMIN_*` | Paramètres globaux |
| Système Bancaire | `BANK_*` | Intégration bancaire |

#### 5.1.2 Liste Détaillée des Permissions

```yaml
# config/permissions.yaml
permissions:
  DEMANDE:
    - DEMANDE_CREATE: Créer une nouvelle demande
    - DEMANDE_READ: Consulter une demande
    - DEMANDE_UPDATE: Modifier une demande
    - DEMANDE_DELETE: Supprimer une demande
    - DEMANDE_SUBMIT: Soumettre une demande
    - DEMANDE_CANCEL: Annuler une demande
    
  WORKFLOW:
    - WORKFLOW_VIEW: Voir les workflows
    - WORKFLOW_CONFIGURE: Configurer les workflows
    - WORKFLOW_APPROVE: Approuver une étape
    - WORKFLOW_REJECT: Rejeter une étape
    - WORKFLOW_REASSIGN: Réassigner une tâche
    
  USER:
    - USER_CREATE: Créer un utilisateur
    - USER_READ: Consulter un utilisateur
    - USER_UPDATE: Modifier un utilisateur
    - USER_DELETE: Supprimer un utilisateur
    - USER_ACTIVATE: Activer/désactiver un compte
    
  PROFILE:
    - PROFILE_CREATE: Créer un profil
    - PROFILE_READ: Consulter un profil
    - PROFILE_UPDATE: Modifier un profil
    - PROFILE_DELETE: Supprimer un profil
    - PROFILE_ASSIGN: Assigner un profil à un utilisateur
    
  REPORT:
    - REPORT_VIEW: Consulter les rapports
    - REPORT_EXPORT: Exporter les rapports
    - REPORT_CONFIGURE: Configurer les rapports
    
  ADMIN:
    - ADMIN_SETTINGS: Accéder aux paramètres
    - ADMIN_AUDIT: Consulter les logs d'audit
    - ADMIN_BACKUP: Gérer les sauvegardes
    
  BANK:
    - BANK_CONNECT: Se connecter au système bancaire
    - BANK_QUERY: Interroger le système bancaire
    - BANK_SYNC: Synchroniser les données
```

#### 5.1.3 Commandes de Gestion des Permissions

```bash
# Lister toutes les permissions disponibles
php bin/console app:permission:list

# Vérifier les permissions d'un utilisateur
php bin/console app:permission:check --user=username --permission=DEMANDE_CREATE

# Ajouter une permission personnalisée (via commande custom)
php bin/console app:permission:add --code=CUSTOM_PERM --description="Description"
```

### 5.2 Gestion des Profils <a name="gestion-des-profils"></a>

#### 5.2.1 Profils Standards

L'application inclut les profils suivants :

| Profil | Code | Description | Permissions Principales |
|--------|------|-------------|------------------------|
| Client | `ROLE_CLIENT` | Client soumis des demandes | DEMANDE_CREATE, DEMANDE_READ, DEMANDE_SUBMIT |
| Gestionnaire | `ROLE_GESTIONNAIRE` | Traite les demandes | DEMANDE_*, WORKFLOW_APPROVE, WORKFLOW_REJECT |
| Superviseur | `ROLE_SUPERVISEUR` | Valide les décisions | DEMANDE_*, WORKFLOW_*, REPORT_VIEW |
| Administrateur | `ROLE_ADMIN` | Administre l'application | Toutes permissions |
| Auditeur | `ROLE_AUDITEUR` | Consulte les logs | REPORT_VIEW, ADMIN_AUDIT |
| DSI | `ROLE_DSI` | Administration technique | ADMIN_*, BANK_* |

#### 5.2.2 Création d'un Nouveau Profil

**Via l'interface d'administration :**

1. Se connecter avec un compte administrateur
2. Naviguer vers **Administration > Profils**
3. Cliquer sur **Nouveau Profil**
4. Renseigner :
   - Nom du profil
   - Code (unique, préfixé par ROLE_)
   - Description
   - Permissions associées (cocher les cases)
5. Enregistrer

**Via ligne de commande :**

```bash
# Créer un profil (exemple pour un nouveau rôle)
php bin/console app:profile:create \
    --name="Responsable Agence" \
    --code="ROLE_RESPONSABLE_AGENCE" \
    --description="Responsable d'agence bancaire" \
    --permissions="DEMANDE_READ,DEMANDE_UPDATE,WORKFLOW_APPROVE,REPORT_VIEW"
```

#### 5.2.3 Modification d'un Profil

```bash
# Modifier les permissions d'un profil existant
php bin/console app:profile:update \
    --code="ROLE_GESTIONNAIRE" \
    --add-permissions="REPORT_EXPORT" \
    --remove-permissions="DEMANDE_DELETE"
```

#### 5.2.4 Suppression d'un Profil

```bash
# Attention : vérifiez qu'aucun utilisateur n'utilise ce profil
php bin/console app:profile:delete --code="ROLE_OBSOLETE"
```

### 5.3 Gestion des Utilisateurs <a name="gestion-des-utilisateurs"></a>

#### 5.3.1 Création d'un Utilisateur

**Via l'interface :**

1. **Administration > Utilisateurs > Nouvel Utilisateur**
2. Renseigner :
   - Identifiant (login)
   - Nom et prénom
   - Email
   - Mot de passe (temporaire)
   - Profil(s) associé(s)
   - Agence/Rattachement
   - Statut (Actif/Inactif)
3. Enregistrer
4. Communiquer les identifiants à l'utilisateur

**Via ligne de commande :**

```bash
# Créer un utilisateur
php bin/console app:user:create \
    --username=jdupont \
    --email=j.dupont@orabank.tg \
    --plain-password="MotDePasseTemporaire123!" \
    --profile="ROLE_GESTIONNAIRE" \
    --fullname="Jean Dupont" \
    --agency="AGENCE_LOME_CENTRE"
```

#### 5.3.2 Gestion des Mots de Passe

```bash
# Réinitialiser le mot de passe d'un utilisateur
php bin/console app:user:reset-password --username=jdupont

# Forcer le changement de mot de passe à la prochaine connexion
php bin/console app:user:force-password-change --username=jdupont

# Verrouiller un compte
php bin/console app:user:lock --username=jdupont

# Déverrouiller un compte
php bin/console app:user:unlock --username=jdupont
```

#### 5.3.3 Activation/Désactivation

```bash
# Désactiver un utilisateur (départ, congé long)
php bin/console app:user:disable --username=jdupont

# Réactiver un utilisateur
php bin/console app:user:enable --username=jdupont
```

#### 5.3.4 Attribution de Plusieurs Profils

Un utilisateur peut avoir plusieurs profils :

```bash
# Ajouter un profil supplémentaire
php bin/console app:user:add-profile \
    --username=jdupont \
    --profile="ROLE_AUDITEUR"

# Retirer un profil
php bin/console app:user:remove-profile \
    --username=jdupont \
    --profile="ROLE_AUDITEUR"
```

#### 5.3.5 Liste des Utilisateurs

```bash
# Lister tous les utilisateurs
php bin/console app:user:list

# Filtrer par profil
php bin-console app:user:list --profile=ROLE_GESTIONNAIRE

# Filtrer par agence
php bin-console app:user:list --agency=AGENCE_LOME_CENTRE

# Exporter la liste en CSV
php bin/console app:user:export --format=csv --output=/tmp/users.csv
```

---

## 6. Configuration des Workflows <a name="configuration-des-workflows"></a>

### 6.1 Concepts des Workflows

Un workflow définit le parcours d'une demande de crédit à travers différentes étapes :

- **Étapes (Steps)** : États par lesquels passe la demande
- **Transitions** : Passages d'une étape à une autre
- **Conditions** : Règles métier pour valider une transition
- **Acteurs** : Rôles responsables de chaque étape

### 6.2 Workflow Standard de Demande de Crédit

```
[SOUMISE] → [EN_ANALYSE] → [EN_VALIDATION] → [APPROUVEE] → [DEBOURSEE]
                ↓              ↓                 ↓
           [INCOMPLETE]   [REJETEE]         [ANNULEE]
```

### 6.3 Configuration d'un Workflow

#### 6.3.1 Définition des Étapes

```yaml
# config/workflow/credit_request.yaml
workflows:
  credit_request:
    type: state_machine
    marking_store:
      type: method
      property: status
      
    initial_marking: SOUMISE
    
    places:
      - SOUMISE
      - EN_ANALYSE
      - INCOMPLETE
      - EN_VALIDATION
      - APPROUVEE
      - REJETEE
      - ANNULEE
      - DEBOURSEE
      
    transitions:
      submit:
        from: [SOUMISE]
        to: EN_ANALYSE
        
      request_complement:
        from: [EN_ANALYSE]
        to: INCOMPLETE
        
      complete:
        from: [INCOMPLETE]
        to: EN_ANALYSE
        
      validate_analysis:
        from: [EN_ANALYSE]
        to: EN_VALIDATION
        
      reject:
        from: [EN_ANALYSE, EN_VALIDATION]
        to: REJETEE
        
      approve:
        from: [EN_VALIDATION]
        to: APPROUVEE
        
      cancel:
        from: [SOUMISE, EN_ANALYSE, EN_VALIDATION]
        to: ANNULEE
        
      disburse:
        from: [APPROUVEE]
        to: DEBOURSEE
```

#### 6.3.2 Configuration via l'Interface

1. **Administration > Workflows**
2. Sélectionner le workflow à configurer
3. **Éditer le Workflow**
4. Onglet **Étapes** :
   - Ajouter/Modifier/Supprimer des étapes
   - Définir l'étape initiale
   - Configurer les couleurs et labels
5. Onglet **Transitions** :
   - Définir les transitions entre étapes
   - Associer des conditions
   - Définir les acteurs autorisés
6. Onglet **Conditions** :
   - Ajouter des règles métier
   - Ex: Montant > 10M nécessite validation DG
7. **Publier** le workflow

#### 6.3.3 Commandes Workflow

```bash
# Lister les workflows configurés
php bin/console app:workflow:list

# Afficher le diagramme d'un workflow
php bin/console app:workflow:diagram --name=credit_request

# Tester une transition
php bin-console app:workflow:can-transition \
    --entity-type=CreditRequest \
    --entity-id=123 \
    --transition=approve

# Forcer une transition (admin only)
php bin-console app:workflow:apply \
    --entity-type=CreditRequest \
    --entity-id=123 \
    --transition=approve
```

### 6.4 Configuration des Notifications

```yaml
# config/notifications.yaml
notifications:
  workflow:
    on_transition:
      submit:
        recipients: [ROLE_GESTIONNAIRE]
        template: email_demande_soumise.html.twig
        channels: [email, in_app]
        
      approve:
        recipients: [CLIENT, ROLE_GESTIONNAIRE]
        template: email_demande_approuvee.html.twig
        channels: [email, sms, in_app]
        
      reject:
        recipients: [CLIENT, ROLE_GESTIONNAIRE]
        template: email_demande_rejetee.html.twig
        channels: [email, in_app]
        require_comment: true
```

### 6.5 Escalade et Délais

```yaml
# config/escalation.yaml
escalation:
  credit_request:
    EN_ANALYSE:
      deadline_hours: 48
      warning_hours: 24
      actions:
        - type: notify
          recipient: ROLE_SUPERVISEUR
        - type: reassign
          after_hours: 72
          
    EN_VALIDATION:
      deadline_hours: 24
      warning_hours: 12
      actions:
        - type: notify
          recipient: ROLE_DIRECTEUR_AGENCE
```

---

## 7. Communication avec le Système Bancaire <a name="communication-avec-le-systeme-bancaire"></a>

### 7.1 Architecture d'Intégration

```
┌─────────────────┐     HTTPS/REST     ┌──────────────────────┐
│  Crédit en      │ ◄────────────────► │  Core Banking System │
│  Ligne          │                    │  (Oracle T24/autre)  │
└─────────────────┘                    └──────────────────────┘
        │
        │ Async Queue
        ▼
┌─────────────────┐
│  File d'attente │
│  (RabbitMQ/     │
│   Redis)        │
└─────────────────┘
```

### 7.2 Configuration de la Connexion

```yaml
# config/services/banking_integration.yaml
parameters:
  banking.system.url: '%env(BANKING_SYSTEM_URL)%'
  banking.system.api_key: '%env(BANKING_SYSTEM_API_KEY)%'
  banking.system.timeout: '%env(int:BANKING_SYSTEM_TIMEOUT)%'
  banking.system.retry_count: 3
  banking.system.retry_delay: 5000  # ms

services:
  App\Service\BankingClient:
    arguments:
      $baseUrl: '%banking.system.url%'
      $apiKey: '%banking.system.api_key%'
      $timeout: '%banking.system.timeout%'
      $retryCount: '%banking.system.retry_count%'
```

### 7.3 Configuration .env

```ini
# Système bancaire core
BANKING_SYSTEM_URL=https://api-core.orabank.tg/v1
BANKING_SYSTEM_API_KEY=your_secure_api_key_here
BANKING_SYSTEM_TIMEOUT=30
BANKING_SYSTEM_RETRY_COUNT=3
BANKING_SYSTEM_RETRY_DELAY=5000

# Certificats SSL (si nécessaire)
BANKING_SYSTEM_SSL_CERT=/etc/ssl/certs/orabank_core.crt
BANKING_SYSTEM_SSL_KEY=/etc/ssl/private/orabank_core.key

# File d'attente pour communications async
QUEUE_DSN=redis://localhost:6379
# OU
# QUEUE_DSN=messaging://rabbitmq://guest:guest@localhost:5672/%2f/messages
```

### 7.4 Endpoints Bancaires Configurés

| Endpoint | Méthode | Description | Permission Requise |
|----------|---------|-------------|-------------------|
| `/customers/{id}` | GET | Récupérer infos client | BANK_QUERY |
| `/accounts/{id}` | GET | Récupérer infos compte | BANK_QUERY |
| `/credits/simulate` | POST | Simulation crédit | BANK_QUERY |
| `/credits/create` | POST | Créer crédit approuvé | BANK_CONNECT |
| `/transactions` | POST | Enregistrer transaction | BANK_CONNECT |
| `/sync/customers` | POST | Synchroniser clients | BANK_SYNC |

### 7.5 Commandes de Synchronisation

```bash
# Synchroniser les clients depuis le core banking
php bin/console app:banking:sync-customers --batch-size=100

# Synchroniser les comptes
php bin-console app:banking:sync-accounts --customer-id=12345

# Vérifier la connectivité
php bin-console app:banking:health-check

# Tester un endpoint spécifique
php bin-console app:banking:test-endpoint --endpoint=/customers/12345

# Purger le cache des données bancaires
php bin-console app:banking:cache-clear
```

### 7.6 Gestion des Erreurs

```yaml
# config/error_handling.yaml
error_handling:
  banking:
    retry_on:
      - connection_timeout
      - service_unavailable
      - rate_limit_exceeded
      
    do_not_retry:
      - authentication_failed
      - invalid_request
      - resource_not_found
      
    alerts:
      consecutive_failures: 5
      alert_recipients:
        - dsitech@orabank.tg
        - exploitation@orabank.tg
```

### 7.7 Logs et Audit

```bash
# Consulter les logs d'intégration
tail -f /var/log/crdt_en_ligne/banking_integration.log

# Exporter les logs d'audit
php bin-console app:audit:export \
    --start-date="2024-01-01" \
    --end-date="2024-01-31" \
    --module=BANKING \
    --format=csv
```

---

## 8. Déploiement en Production <a name="deploiement-en-production"></a>

### 8.1 Checklist Pré-Déploiement

- [ ] Tests unitaires passés avec succès
- [ ] Tests d'intégration validés
- [ ] Validation de sécurité effectuée
- [ ] Sauvegarde de la base de données
- [ ] Plan de rollback préparé
- [ ] Communication aux utilisateurs prévue

### 8.2 Procédure de Déploiement

```bash
#!/bin/bash
# deploy.sh

set -e

DEPLOY_DIR="/var/www/crdt_en_ligne"
BACKUP_DIR="/backups/crdt_en_ligne"
DATE=$(date +%Y%m%d_%H%M%S)

echo "=== Début du déploiement ==="

# 1. Maintenance mode
echo "Activation du mode maintenance..."
php $DEPLOY_DIR/bin/console app:maintenance:enable

# 2. Sauvegarde
echo "Sauvegarde de l'application..."
mkdir -p $BACKUP_DIR
tar -czf $BACKUP_DIR/app_$DATE.tar.gz -C $DEPLOY_DIR .
mysqldump -u crdt_user -p crdt_en_ligne > $BACKUP_DIR/db_$DATE.sql

# 3. Récupération du code
echo "Récupération du nouveau code..."
cd $DEPLOY_DIR
git fetch origin
git checkout main
git pull origin main

# 4. Installation des dépendances
echo "Installation des dépendances..."
composer install --no-dev --optimize-autoloader --no-interaction

# 5. Build frontend
echo "Compilation du frontend..."
cd $DEPLOY_DIR/frontend
npm install --production
npm run build --prod

# 6. Migrations
echo "Exécution des migrations..."
cd $DEPLOY_DIR
php bin/console doctrine:migrations:migrate --no-interaction

# 7. Cache
echo "Nettoyage du cache..."
php bin/console cache:clear --env=prod --no-debug
php bin/console cache:warmup --env=prod

# 8. Assets
echo "Installation des assets..."
php bin/console assets:install public --symlink --relative

# 9. Permissions
echo "Configuration des permissions..."
chown -R www-data:www-data $DEPLOY_DIR
chmod -R 755 $DEPLOY_DIR/var
chmod -R 755 $DEPLOY_DIR/public

# 10. Mode maintenance OFF
echo "Désactivation du mode maintenance..."
php bin/console app:maintenance:disable

# 11. Health check
echo "Vérification de la santé de l'application..."
curl -f http://localhost/health || exit 1

echo "=== Déploiement terminé avec succès ==="
```

### 8.3 Configuration Apache

```apache
<VirtualHost *:80>
    ServerName credit.orabank.tg
    DocumentRoot /var/www/crdt_en_ligne/public
    
    <Directory /var/www/crdt_en_ligne/public>
        AllowOverride All
        Require all granted
        
        # Sécurité
        Header always set X-Content-Type-Options nosniff
        Header always set X-Frame-Options DENY
        Header always set X-XSS-Protection "1; mode=block"
        Header always set Referrer-Policy strict-origin-when-cross-origin
    </Directory>
    
    # Logs
    ErrorLog ${APACHE_LOG_DIR}/crdt_error.log
    CustomLog ${APACHE_LOG_DIR}/crdt_access.log combined
    
    # Compression
    <IfModule mod_deflate.c>
        AddOutputFilterByType DEFLATE text/html text/plain text/xml text/css text/javascript application/javascript application/json
    </IfModule>
    
    # Cache statique
    <IfModule mod_expires.c>
        ExpiresActive On
        ExpiresByType image/jpg "access plus 1 year"
        ExpiresByType image/jpeg "access plus 1 year"
        ExpiresByType image/gif "access plus 1 year"
        ExpiresByType image/png "access plus 1 year"
        ExpiresByType text/css "access plus 1 month"
        ExpiresByType application/javascript "access plus 1 month"
    </IfModule>
</VirtualHost>

# Redirection HTTPS (recommandé en production)
<VirtualHost *:80>
    ServerName credit.orabank.tg
    Redirect permanent / https://credit.orabank.tg/
</VirtualHost>
```

### 8.4 Configuration HTTPS

```bash
# Installation de Let's Encrypt
sudo apt-get install -y certbot python3-certbot-apache

# Obtention du certificat
sudo certbot --apache -d credit.orabank.tg

# Renouvellement automatique (déjà configuré par certbot)
# Vérifier le cron : /etc/cron.d/certbot
```

### 8.5 Surveillance

```yaml
# config/monitoring.yaml
monitoring:
  health_check:
    enabled: true
    interval: 60  # secondes
    endpoints:
      - /health
      - /health/database
      - /health/banking-system
      
  metrics:
    enabled: true
    path: /metrics
    
  alerts:
    - metric: response_time
      threshold: 2000  # ms
      action: alert
      
    - metric: error_rate
      threshold: 5  # %
      action: alert
      
    - metric: queue_size
      threshold: 1000
      action: alert
```

---

## 9. Maintenance et Surveillance <a name="maintenance-et-surveillance"></a>

### 9.1 Tâches Quotidiennes

```bash
# Nettoyage des sessions expirées
php bin/console app:session:cleanup

# Nettoyage des logs anciens (> 30 jours)
find /var/log/crdt_en_ligne -name "*.log" -mtime +30 -delete

# Vérification de l'espace disque
df -h /var/www/crdt_en_ligne

# Vérification des files d'attente
php bin/console app:queue:status
```

### 9.2 Tâches Hebdomadaires

```bash
# Sauvegarde complète
mysqldump -u crdt_user -p crdt_en_ligne | gzip > /backups/db_weekly_$(date +%Y%m%d).sql.gz

# Analyse des performances
php bin/console app:performance:report --output=/tmp/perf_report.pdf

# Rotation des logs
logrotate /etc/logrotate.d/crdt_en_ligne
```

### 9.3 Tâches Mensuelles

```bash
# Audit de sécurité
php bin/console app:security:audit

# Nettoyage de la base de données
php bin/console app:database:cleanup --dry-run  # D'abord en dry-run
php bin-console app:database:cleanup  # Puis exécution réelle

# Mise à jour des dépendances (test d'abord en staging)
composer outdated
npm outdated
```

### 9.4 Commandes Utiles

```bash
# État de l'application
php bin/console app:status

# Liste des tâches en attente
php bin/console app:tasks:list --status=pending

# Relancer les tâches échouées
php bin/console app:tasks:retry --failed

# Purger le cache
php bin/console cache:clear

# Optimiser l'autoloader
composer dump-autoload --optimize

# Vérifier la configuration
php bin/console debug:config

# Liste des routes
php bin/console debug:router
```

### 9.5 Logs et Debugging

```bash
# Emplacement des logs
/var/log/crdt_en_ligne/
├── prod.log          # Logs de production
├── dev.log           # Logs de développement
├── banking.log       # Logs intégration bancaire
├── workflow.log      # Logs des workflows
├── audit.log         # Logs d'audit
└── error.log         # Erreurs critiques

# Niveau de log
# Dans .env.local : MONOLOG_LEVEL=info (debug, info, notice, warning, error, critical, alert, emergency)

# Console en temps réel
tail -f /var/log/crdt_en_ligne/prod.log

# Recherche dans les logs
grep "ERROR" /var/log/crdt_en_ligne/prod.log | tail -50
```

---

## 10. Annexes <a name="annexes"></a>

### 10.1 Glossaire

| Terme | Définition |
|-------|------------|
| Workflow | Parcours structuré d'une demande à travers différentes étapes |
| Transition | Passage d'une étape à une autre dans un workflow |
| Profil | Ensemble de permissions attribuées à un type d'utilisateur |
| Permission | Droit d'accès à une fonctionnalité spécifique |
| Core Banking | Système central de gestion bancaire |
| TFJ | Travaux de Fin de Journée |

### 10.2 Codes d'Erreur Courants

| Code | Signification | Action |
|------|---------------|--------|
| ERR_AUTH_001 | Authentification échouée | Vérifier credentials |
| ERR_BANK_002 | Connection système bancaire | Vérifier réseau/API |
| ERR_WF_003 | Transition non autorisée | Vérifier permissions |
| ERR_DB_004 | Erreur base de données | Consulter logs DB |
| ERR_VAL_005 | Validation métier échouée | Vérifier données |

### 10.3 Contacts Support

| Service | Email | Téléphone |
|---------|-------|-----------|
| Support Technique | support.tech@orabank.tg | +228 XX XX XX XX |
| DSI Exploitation | exploitation@orabank.tg | +228 XX XX XX XX |
| DSI Développement | dev@orabank.tg | +228 XX XX XX XX |
| Urgences | urgence.dsi@orabank.tg | +228 XX XX XX XX |

### 10.4 Historique des Versions

| Version | Date | Modifications |
|---------|------|---------------|
| 1.0.0 | 2024-01-15 | Version initiale |
| 1.1.0 | 2024-03-01 | Ajout module reporting |
| 1.2.0 | 2024-06-01 | Intégration Oracle |

---

*Document propriété d'ORABANK TOGO - Usage interne uniquement*
*Dernière mise à jour : $(date +%Y-%m-%d)*
