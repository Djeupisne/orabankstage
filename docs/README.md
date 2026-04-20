# Documentation Technique - ORABANK TOGO

Ce répertoire contient la documentation technique pour les applications de la DSI d'ORABANK TOGO.

## Documents Disponibles

### 1. Déploiement et Paramétrage - Application Crédit en Ligne

**Fichier :** `DEPLOIEMENT_PARAMETRAGE.md`

Documentation complète pour le déploiement et le paramétrage de l'application **Crédit en Ligne**, un système de workflow documentaire pour la digitalisation des demandes de crédit.

**Stack Technique :**
- Backend : PHP 8.4+ / Symfony 8.0 / MySQL / Oracle
- Frontend : Angular / TypeScript / Spring Boot / Java 17 / PostgreSQL / Oracle

**Contenu :**
- Prérequis techniques
- Installation de l'environnement
- Configuration de la base de données
- Paramétrage des permissions, profils et utilisateurs
- Configuration des workflows
- Intégration avec le système bancaire core
- Procédures de déploiement en production
- Maintenance et surveillance

**Public cible :** Administrateurs système, DBA, chefs de projet, équipes d'exploitation

---

### 2. Automatisation de la Planification des TFJ et Permanences

**Fichier :** `PLANIFICATION_TFJ.md`

Spécifications techniques et guide d'implémentation pour le système automatisé de planification des Travaux de Fin de Journée (TFJ) et des permanences du samedi pour la DSI.

**Stack Technique :**
- Backend : Java 17 / Spring Boot / PostgreSQL / Oracle
- Frontend : Angular / TypeScript

**Règles de Gestion Implémentées :**
1. **Non-successivité intra-groupe** : Les membres d'un même groupe ne peuvent pas se suivre sur des jours consécutifs
2. **Rotation anti-chronologique** : Un membre affecté un jour donné prendra le jour antérieur la semaine suivante (ex: Jeudi → Mercredi)
3. **Exception membres isolés** : Les membres seuls dans leur groupe ne sont programmés que les Vendredis ou Samedis

**Fonctionnalités :**
- Gestion des jours fériés
- Gestion des demi-journées
- Algorithme de rotation équitable
- Export Excel/PDF
- Interface de validation manuelle

**Public cible :** Managers DSI, administrateurs, équipes d'exploitation

---

## Structure des Répertoires

```
docs/
├── README.md                      # Ce fichier
├── DEPLOIEMENT_PARAMETRAGE.md     # Guide de déploiement Crédit en Ligne
└── PLANIFICATION_TFJ.md           # Spécifications planification TFJ
```

---

## Utilisation

### Consultation

Les documents sont au format Markdown et peuvent être consultés :
- Directement dans un éditeur de texte
- Via un visualiseur Markdown (VS Code, GitHub, GitLab, etc.)
- Convertis en PDF pour impression

```bash
# Conversion en PDF (nécessite pandoc)
pandoc DEPLOIEMENT_PARAMETRAGE.md -o DEPLOIEMENT_PARAMETRAGE.pdf
pandoc PLANIFICATION_TFJ.md -o PLANIFICATION_TFJ.pdf
```

### Mise à Jour

Pour mettre à jour la documentation :
1. Modifier le fichier Markdown correspondant
2. Mettre à jour la date et la version en fin de document
3. Commiter avec un message explicite

---

## Contacts

Pour toute question concernant cette documentation :

| Service | Email |
|---------|-------|
| DSI Développement | dev@orabank.tg |
| DSI Exploitation | exploitation@orabank.tg |
| Support Technique | support.tech@orabank.tg |

---

*Document propriété d'ORABANK TOGO - Usage interne uniquement*
*Dernière mise à jour : 2024-01-20*
