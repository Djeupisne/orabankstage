# Résumé des évolutions - Gestion des congés et améliorations

## Nouvelles fonctionnalités implémentées

### 1. Gestion des congés des participants

#### Backend
- **Nouvelle entité `Conge`** : Modélise les congés des employés avec gestion des demi-journées
  - Dates de début et fin
  - Indicateurs de demi-journée (début/fin)
  - Type de congé (congés payés, maladie, sans solde, etc.)
  
- **Repository `CongeRepository`** : Méthodes de recherche des congés par période et par employé

- **Algorithme de planification mis à jour** :
  - Vérification automatique des congés lors de l'affectation
  - Les employés en congé sont automatiquement exclus de la planification
  - Support des demi-journées de congé

#### Script SQL
- Exemples de congés pré-configurés dans `init.sql` :
  - Congé annuel pour Kofi AMENYONOR (15-19 juin 2026)
  - Congé pour Folly GANOU (22-26 juin 2026)
  - Demi-journée pour Ama TCHASSAN (8 juin 2026 matin)

### 2. Réaffectation automatique en cas d'absence exceptionnelle

#### Nouvelle méthode `reassignDueToAbsence()`
- Détecte les absences exceptionnelles (congés non planifiés initialement)
- Trouve automatiquement l'employé programmé le jour ouvrable suivant
- Réaffecte le jour libéré à cet employé
- Respecte les règles métier (TFJ Lundi-Vendredi, Permanence Samedi)

**Exemple de fonctionnement** :
```
Si Jean est programmé le Mardi 10/06 mais déclare un congé maladie :
- Le système cherche qui est programmé le Mercredi 11/06 (ex: Marie)
- Marie est réaffectée au Mardi 10/06
- Son affectation du Mercredi reste maintenue (ou recalculée selon besoin)
```

### 3. Gestion hiérarchique améliorée - Managers en dernier recours

#### Séparation Managers / Non-Managers
- Les employés sont classés selon leur niveau hiérarchique
- Deux catégories : Managers et Non-Managers (Collaborateurs, Cadres non-managers)

#### Règle d'affectation prioritaire
1. **Priorité aux Non-Managers** : Tous les jours de la semaine
2. **Managers en secours** : Uniquement si aucun Non-Manager n'est disponible
   - Log message informatif lorsqu'un manager est affecté
   - Permet de préserver la disponibilité des managers pour leurs responsabilités

**Implémentation** :
```java
// D'abord tentative avec les non-managers
Employee selected = selectEmployeeFromGroups(nonManagerEmployeesByRole, ...);

if (selected == null) {
    // Fallback sur les managers
    log.info("Aucun non-manager disponible, tentative avec les managers");
    selected = selectEmployeeFromGroups(managerEmployeesByRole, ...);
}
```

## Modifications techniques

### Fichiers créés
1. `backend/src/main/java/com/orabank/tfj/model/Conge.java` - Entité JPA
2. `backend/src/main/java/com/orabank/tfj/repository/CongeRepository.java` - Repository

### Fichiers modifiés
1. `backend/src/main/java/com/orabank/tfj/service/PlanningAlgorithmService.java`
   - Ajout de la dépendance `CongeRepository`
   - Refonte des méthodes `assignTFJ()` et `assignPermanence()`
   - Nouvelle méthode `selectEmployeeFromGroups()`
   - Nouvelle méthode `isEmployeeOnLeave()`
   - Nouvelle méthode `reassignDueToAbsence()`
   - Gestion séparée managers/non-managers

2. `backend/src/main/java/com/orabank/tfj/repository/ScheduleRepository.java`
   - Ajout de la méthode `findByDate()`

3. `backend/src/main/resources/db/init.sql`
   - Ajout d'exemples de congés

## Utilisation

### Via l'interface utilisateur
1. Sélectionnez une période (date de début et date de fin)
2. Cliquez sur "Générer un planning"
3. Le système :
   - Exclut automatiquement les employés en congé
   - Priorise les non-managers
   - Applique la rotation anti-chronologique
   - Respecte les règles de non-successivité

### En cas d'absence exceptionnelle
Lorsqu'un employé déjà programmé déclare un congé inattendu :
```java
// Appel API ou service
Schedule nouveauPlanning = planningAlgorithmService.reassignDueToAbsence(
    scheduleOriginal, 
    dateDebut, 
    dateFin
);
```

## Tests recommandés

1. **Test des congés** :
   - Générer un planning sur la période du 15-19 juin 2026
   - Vérifier que Kofi AMENYONOR n'apparaît pas
   - Idem pour Folly GANOU (22-26 juin)

2. **Test de la priorité Non-Manager** :
   - Créer un scénario où tous les non-managers d'un rôle sont en congé
   - Vérifier qu'un manager est affecté (avec log approprié)

3. **Test de réaffectation** :
   - Générer un planning complet
   - Ajouter un congé inopiné pour un employé programmé
   - Appeler `reassignDueToAbsence()`
   - Vérifier que le collègue du jour suivant est réaffecté

## Limitations connues et améliorations futures

1. **Réaffectation** : Actuellement manuelle via appel de service
   - → Pourrait être automatisée avec un trigger ou job planifié
   
2. **Demi-journées** : Gestion simplifiée (absent/present)
   - → Pourrait gérer spécifiquement matin vs après-midi
   
3. **Notifications** : Aucune notification envoyée lors des réaffectations
   - → Intégrer un service d'emailing

4. **Interface de gestion des congés** : À développer dans le frontend
   - Formulaire de saisie des congés
   - Calendrier visuel des congés par équipe
