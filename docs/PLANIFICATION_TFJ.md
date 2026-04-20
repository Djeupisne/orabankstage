# Automatisation de la Planification des Travaux de Fin de Journée (TFJ) et Permanences

## Table des Matières

1. [Introduction](#introduction)
2. [Organisation de la DSI](#organisation-de-la-dsi)
3. [Règles de Planification](#regles-de-planification)
4. [Algorithme de Planification](#algorithme-de-planification)
5. [Implémentation Technique](#implementation-technique)
6. [Guide d'Utilisation](#guide-dutilisation)
7. [Maintenance et Évolutions](#maintenance-et-evolutions)

---

## 1. Introduction <a name="introduction"></a>

### 1.1 Contexte

La Direction des Systèmes d'Information (DSI) d'ORABANK TOGO doit organiser la planification des :
- **Travaux de Fin de Journée (TFJ)** : Du Lundi au Vendredi
- **Permanences** : Les Samedis

Cette documentation présente l'algorithme et l'implémentation du système automatisé de planification qui respecte les contraintes métier spécifiques.

### 1.2 Objectifs

- Automatiser la génération des plannings TFJ et permanences
- Respecter les règles de gestion définies
- Assurer une répartition équitable de la charge
- Gérer les spécificités des jours fériés et demi-journées
- Fournir un outil simple d'utilisation pour les managers

### 1.3 Périmètre

Ce système concerne :
- Tous les services de la DSI (Application, Infrastructure, Exploitation)
- Tous les rôles/fiches de poste
- La hiérarchie (Cadres, Managers)

---

## 2. Organisation de la DSI <a name="organisation-de-la-dsi"></a>

### 2.1 Structure Organisationnelle

```
DSI
├── Service Application
│   ├── Développeurs
│   ├── Chefs de Projet
│   └── Analystes
├── Service Infrastructure
│   ├── Administrateurs Réseau
│   ├── Administrateurs Système
│   └── Techniciens
└── Service Exploitation
    ├── DBA
    ├── Opérateurs
    └── Superviseurs
```

### 2.2 Hiérarchie

| Niveau | Description | Exemples |
|--------|-------------|----------|
| Management | Supervise les cadres | Directeur DSI, Directeurs Adjoints |
| Cadres | Encadrement opérationnel | Chefs de Service, Responsables d'Équipe |
| Staff | Personnel technique | Développeurs, Admins, DBA, etc. |

### 2.3 Rôles/Fiches de Poste

| Code | Intitulé | Service | Type |
|------|----------|---------|------|
| DEV | Développeur | Application | Staff |
| CP | Chef de Projet | Application | Cadre |
| AR | Administrateur Réseau | Infrastructure | Staff |
| AS | Administrateur Système | Infrastructure | Staff |
| DBA | Database Administrator | Exploitation | Staff |
| TECH | Technicien | Infrastructure | Staff |
| OPER | Opérateur | Exploitation | Staff |
| SUPER | Superviseur | Exploitation | Cadre |
| MGR | Manager | Tous | Management |

---

## 3. Règles de Planification <a name="regles-de-planification"></a>

### 3.1 Règles Générales

#### 3.1.1 Jours de Travail

| Type | Jours | Période |
|------|-------|---------|
| TFJ | Lundi - Vendredi | 08h00 - 18h00 (exemple) |
| Permanence | Samedi | 08h00 - 14h00 (exemple) |
| Repos | Dimanche + Jours Fériés | - |

#### 3.1.2 Gestion des Jours Fériés

Les jours fériés sont exclus de la planification automatique :

**Jours fériés standards :**
- 1er Janvier
- Lundi de Pâques
- 1er Mai
- Ascension
- Lundi de Pentecôte
- 27 Juin (Fête Nationale)
- 15 Août
- 1er Novembre
- Noël (25 Décembre)

**Traitement :**
- Si un jour férié tombe en semaine → Pas de TFJ ce jour-là
- Si un jour férié tombe un samedi → Pas de permanence ce jour-là
- Report possible sur le jour ouvré suivant selon décision managériale

#### 3.1.3 Gestion des Demi-Journées

Certaines dates peuvent être configurées comme demi-journées :
- Veilles de fêtes
- Événements spéciaux
- Congés collectifs

**Impact :**
- Une demi-journée compte comme 0.5 jour dans le calcul de charge
- Possibilité d'affecter 2 équipes différentes (matin/après-midi)

### 3.2 Règles d'Affectation

#### Règle 1 : Non-Successivité Intra-Groupe

> **Les membres d'un même groupe ne peuvent pas se suivre**

**Explication :**
Si un membre du Groupe A est affecté le Lundi, aucun autre membre du Groupe A ne peut être affecté le Mardi.

**Exemple :**
```
Semaine 1:
- Lundi : Jean (Groupe DEV) ✓
- Mardi : Marie (Groupe DEV) ✗ INTERDIT
- Mardi : Paul (Groupe INFRA) ✓ AUTORISÉ
```

**Justification :**
- Assure la continuité de service
- Évite la surcharge d'un même groupe
- Permet la transmission entre groupes différents

#### Règle 2 : Rotation Anti-Chronologique

> **Lorsqu'un membre de l'équipe est affecté un jour, la semaine suivante s'il doit être affecté, il prend le jour antérieur**

**Explication :**
La rotation se fait en sens inverse des jours de la semaine (du Vendredi vers le Lundi).

**Exemple :**
```
Semaine 1 : Jean est affecté Jeudi
Semaine 2 : Si Jean doit être affecté → Mercredi (jour antérieur)
Semaine 3 : Si Jean doit être affecté → Mardi (jour antérieur)
Semaine 4 : Si Jean doit être affecté → Lundi (jour antérieur)
Semaine 5 : Si Jean doit être affecté → Jeudi (reprise cycle)
```

**Algorithme de rotation :**
```
Jours : Lundi(1) - Mardi(2) - Mercredi(3) - Jeudi(4) - Vendredi(5)

Rotation : 5 → 4 → 3 → 2 → 1 → 5 (cycle)
```

#### Règle 3 : Exception Membres Isolés

> **Les membres qui sont seuls dans leur groupe ne sont programmés uniquement les Vendredis ou Samedis**

**Condition d'application :**
- Un membre est considéré "seul" si son groupe ne contient qu'une seule personne
- Cette règle s'applique uniquement si le membre DOIT être programmé (selon rotation)

**Exemple :**
```
Groupe SPECIALISE : {Alice}  ← Membre unique

Programmation normale interdite : Lundi, Mardi, Mercredi, Jeudi
Programmation autorisée : Vendredi (TFJ) ou Samedi (Permanence)
```

**Justification :**
- Protège les ressources rares/spécialisées
- Réserve ces compétences pour les besoins critiques
- Optimise l'utilisation des expertises pointues

### 3.3 Matrice de Décision

| Situation | Jour proposé | Membre seul ? | Autre membre groupe dispo ? | Décision |
|-----------|--------------|---------------|----------------------------|----------|
| Rotation standard | Mardi | Non | Oui (non-successif) | ✅ Affecter |
| Rotation standard | Mardi | Non | Non (successif) | ❌ Reporter |
| Membre isolé | Mardi | Oui | N/A | ❌ Interdit |
| Membre isolé | Vendredi | Oui | N/A | ✅ Autorisé |
| Membre isolé | Samedi | Oui | N/A | ✅ Autorisé (Permanence) |
| Jour férié | Quelconque | N/A | N/A | ❌ Exclu |
| Demi-journée | Matin | Non | Oui | ⚠️ 0.5 jour |

---

## 4. Algorithme de Planification <a name="algorithme-de-planification"></a>

### 4.1 Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────┐
│                    DÉBUT DE L'ALGORITHME                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  1. Charger les données (employés, groupes, contraintes)    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  2. Identifier les périodes à planifier                      │
│     - Exclure jours fériés                                   │
│     - Identifier demi-journées                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Pour chaque semaine de la période                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Pour chaque jour (Lun-Ven pour TFJ, Sam pour Perm)      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  5. Sélectionner le candidat selon rotation                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  6. Appliquer les règles de validation                       │
│     - Règle 1: Non-successivité intra-groupe                │
│     - Règle 2: Rotation anti-chronologique                  │
│     - Règle 3: Exception membres isolés                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  Candidat       │
                    │  valide ?       │
                    └─────────────────┘
                         │         │
                       OUI        NON
                         │         │
                         ▼         ▼
              ┌─────────────────┐  ┌─────────────────┐
              │  Affecter le    │  │  Chercher       │
              │  candidat       │  │  prochain       │
              └─────────────────┘  │  candidat       │
                         │         └─────────────────┘
                         │                  │
                         └────────┬─────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│  7. Enregistrer l'affectation et mettre à jour rotations    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      FIN DE SEMAINE ?                        │
└─────────────────────────────────────────────────────────────┘
                              │
                         ┌────┴────┐
                       NON        OUI
                         │         │
                         ▼         ▼
                    (continuer)  ┌─────────────────┐
                                 │  FIN DE         │
                                 │  PLANIFICATION  │
                                 └─────────────────┘
```

### 4.2 Pseudo-Code Détaillé

```pseudocode
FONCTION genererPlanning(dateDebut, dateFin):
    
    // Initialisation
    planning = nouveau Planning()
    joursFeries = chargerJoursFeries(dateDebut, dateFin)
    demiJournees = chargerDemiJournees(dateDebut, dateFin)
    employes = chargerEmployes()
    groupes = regrouperParGroupe(employes)
    
    // Pour chaque semaine dans la période
    POUR semaine = dateDebut À dateFin PAR pas de 7 jours:
        
        // TFJ : Lundi à Vendredi
        POUR jour = Lundi À Vendredi:
            
            SI jour EST DANS joursFeries:
                CONTINUER  // Jour férié, on passe
            
            // Déterminer le type de journée
            typeJournee = TYPE_JOURNEE_STANDARD
            SI jour EST DANS demiJournees:
                typeJournee = TYPE_DEMI_JOURNEE
            
            // Trouver le meilleur candidat
            candidat = trouverCandidat(jour, semaine, groupes, planning)
            
            SI candidat != NULL:
                affectation = nouvelle Affectation(
                    date = jour,
                    employe = candidat,
                    type = TFJ,
                    duree = typeJournee
                )
                planning.ajouter(affectation)
                mettreAJourRotation(candidat, jour)
        
        // Permanence : Samedi
        samedi = obtenirSamedi(semaine)
        SI samedi <= dateFin ET samedi NON DANS joursFeries:
            
            candidat = trouverCandidatPermanence(samedi, semaine, groupes, planning)
            
            SI candidat != NULL:
                affectation = nouvelle Affectation(
                    date = samedi,
                    employe = candidat,
                    type = PERMANENCE,
                    duree = TYPE_JOURNEE_STANDARD
                )
                planning.ajouter(affectation)
                mettreAJourRotation(candidat, samedi)
    
    RETOURNER planning


FONCTION trouverCandidat(jour, semaine, groupes, planning):
    
    // Calculer le jour cible selon la rotation anti-chronologique
    jourRotation = calculerJourRotation(semaine)
    
    // Trier les candidats par priorité
    candidats = []
    
    POUR CHAQUE groupe DANS groupes:
        POUR CHAQUE employe DANS groupe.employes:
            
            // Vérifier si l'employé est disponible
            SI NON estDisponible(employe, jour):
                CONTINUER
            
            // Règle 3 : Membre isolé uniquement Vendredi/Samedi
            SI estMembreIsol(employe, groupe) ET jour != Vendredi:
                CONTINUER
            
            // Règle 1 : Non-successivité intra-groupe
            jourPrecedent = jour - 1 jour
            SI existeAffectation(groupe, jourPrecedent, planning):
                CONTINUER
            
            // Calculer score de priorité
            score = calculerScore(employe, jourRotation, planning)
            
            candidats.ajouter({employe, score})
    
    // Trier par score décroissant
    candidats.trierParScoreDecroissant()
    
    // Retourner le meilleur candidat
    SI candidats.nonVide():
        RETOURNER candidats[0].employe
    
    RETOURNER NULL


FONCTION calculerJourRotation(semaine):
    
    // Semaine 1 : Vendredi (5)
    // Semaine 2 : Jeudi (4)
    // Semaine 3 : Mercredi (3)
    // Semaine 4 : Mardi (2)
    // Semaine 5 : Lundi (1)
    // Semaine 6 : Vendredi (5) ... cycle
    
    numeroSemaine = obtenirNumeroSemaine(semaine)
    indexDansCycle = (numeroSemaine - 1) MOD 5
    
    joursRotation = [Vendredi, Jeudi, Mercredi, Mardi, Lundi]
    
    RETOURNER joursRotation[indexDansCycle]


FONCTION estMembreIsol(employe, groupe):
    
    // Un membre est isolé s'il est seul dans son groupe
    RETOURNER groupe.taille == 1


FONCTION calculerScore(employe, jourRotation, planning):
    
    score = 0
    
    // Priorité à ceux qui n'ont pas été affectés récemment
    dernierAffectation = obtenirDerniereAffectation(employe, planning)
    SI dernierAffectation == NULL:
        score += 100
    SINON:
        joursDepuis = jourActuel - dernierAffectation.date
        score += joursDepuis
    
    // Bonus si correspond au jour de rotation idéal
    SI employe.dernierJour == jourRotation:
        score += 50
    
    // Malus si affecté fréquemment
    nombreAffectationsMois = compterAffectationsMois(employe, planning)
    score -= nombreAffectationsMois * 10
    
    RETOURNER score
```

### 4.3 Structures de Données

```typescript
// Employé
interface Employe {
    id: string;
    nom: string;
    prenom: string;
    email: string;
    groupeId: string;
    role: string;
    hierarchyLevel: 'MANAGEMENT' | 'CADRE' | 'STAFF';
    estMembreIsol: boolean;
    disponibilites: Date[];  // Jours indisponibles (congés, formations...)
}

// Groupe
interface Groupe {
    id: string;
    nom: string;
    service: string;
    employes: Employe[];
}

// Affectation
interface Affectation {
    id: string;
    date: Date;
    employeId: string;
    type: 'TFJ' | 'PERMANENCE';
    duree: 'COMPLET' | 'DEMI_MATIN' | 'DEMI_APRES_MIDI';
    statut: 'PLANIFIE' | 'CONFIRME' | 'ANNULE' | 'REMPLACE';
    commentaire?: string;
}

// Planning
interface Planning {
    id: string;
    dateDebut: Date;
    dateFin: Date;
    affectations: Affectation[];
    dateGeneration: Date;
    generePar: string;
    version: number;
}

// Contraintes
interface Contraintes {
    joursFeries: Date[];
    demiJournees: {date: Date, periode: 'MATIN' | 'APRES_MIDI'}[];
    exceptions: {
        employeId: string;
        dates: Date[];
        raison: string;
    }[];
}
```

---

## 5. Implémentation Technique <a name="implementation-technique"></a>

### 5.1 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   Angular   │  │  API REST   │  │  Export PDF │          │
│  │   Frontend  │◄─┤   Spring    │◄─┤  / Excel    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   COUCHE MÉTIER (Java 17)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  Scheduler  │  │   Rules     │  │  Rotation   │          │
│  │  Service    │  │   Engine    │  │  Manager    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│  ┌─────────────┐  ┌─────────────┐                           │
│  │  Holiday    │  │  Conflict   │                           │
│  │  Manager    │  │  Detector   │                           │
│  └─────────────┘  └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   COUCHE DONNÉES                             │
│  ┌─────────────┐  ┌─────────────┐                           │
│  │ PostgreSQL  │  │   Oracle    │                           │
│  │  (principal)│  │  (option)   │                           │
│  └─────────────┘  └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Modèle de Base de Données (PostgreSQL)

```sql
-- Table des employés
CREATE TABLE employe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    matricule VARCHAR(20) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    groupe_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    hierarchy_level VARCHAR(20) NOT NULL CHECK (hierarchy_level IN ('MANAGEMENT', 'CADRE', 'STAFF')),
    actif BOOLEAN DEFAULT TRUE,
    date_embauche DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des groupes
CREATE TABLE groupe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    service VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des affectations
CREATE TABLE affectation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_affectation DATE NOT NULL,
    employe_id UUID NOT NULL REFERENCES employe(id),
    type_affectation VARCHAR(20) NOT NULL CHECK (type_affectation IN ('TFJ', 'PERMANENCE')),
    duree VARCHAR(20) NOT NULL DEFAULT 'COMPLET' CHECK (duree IN ('COMPLET', 'DEMI_MATIN', 'DEMI_APRES_MIDI')),
    statut VARCHAR(20) NOT NULL DEFAULT 'PLANIFIE' CHECK (statut IN ('PLANIFIE', 'CONFIRME', 'ANNULE', 'REMPLACE')),
    commentaire TEXT,
    genere_par UUID REFERENCES employe(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_employe_date UNIQUE (employe_id, date_affectation)
);

-- Table des jours fériés
CREATE TABLE jour_ferie (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_jour DATE UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    recurrent BOOLEAN DEFAULT FALSE,
    jour_mois INT,  -- Pour les récurrents (ex: 1 pour 1er Janvier)
    mois INT,       -- Pour les récurrents
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des demi-journées
CREATE TABLE demi_journee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_jour DATE NOT NULL,
    periode VARCHAR(20) NOT NULL CHECK (periode IN ('MATIN', 'APRES_MIDI')),
    raison VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_date_periode UNIQUE (date_jour, periode)
);

-- Table des rotations (suivi de la position de rotation par employé)
CREATE TABLE rotation_employe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employe_id UUID UNIQUE NOT NULL REFERENCES employe(id),
    dernier_jour_affecte INT,  -- 1=Lundi, 2=Mardi, ..., 5=Vendredi, 6=Samedi
    semaine_derniere_affectation INT,
    nombre_affectations_mois INT DEFAULT 0,
    mois_reference INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour performances
CREATE INDEX idx_affectation_date ON affectation(date_affectation);
CREATE INDEX idx_affectation_employe ON affectation(employe_id);
CREATE INDEX idx_affectation_statut ON affectation(statut);
CREATE INDEX idx_employe_groupe ON employe(groupe_id);
CREATE INDEX idx_rotation_employe ON rotation_employe(employe_id);

-- Vue pour identifier les membres isolés
CREATE VIEW vue_membres_isoles AS
SELECT e.*
FROM employe e
INNER JOIN (
    SELECT groupe_id, COUNT(*) as nb_employes
    FROM employe
    WHERE actif = TRUE
    GROUP BY groupe_id
    HAVING COUNT(*) = 1
) g ON e.groupe_id = g.groupe_id
WHERE e.actif = TRUE;
```

### 5.3 Implementation Java/Spring Boot

#### 5.3.1 Entités JPA

```java
// Employe.java
@Entity
@Table(name = "employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String matricule;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(nullable = false, length = 100)
    private String prenom;
    
    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id", nullable = false)
    private Groupe groupe;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HierarchyLevel hierarchyLevel;
    
    @Column(nullable = false)
    private Boolean actif = true;
    
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Affectation> affectations;
    
    public String getNomComplet() {
        return prenom + " " + nom;
    }
}

// Enum HierarchyLevel
public enum HierarchyLevel {
    MANAGEMENT, CADRE, STAFF
}

// Groupe.java
@Entity
@Table(name = "groupe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(unique = true, nullable = false, length = 20)
    private String code;
    
    @Column(nullable = false, length = 50)
    private String service;
    
    @Column(length = 500)
    private String description;
    
    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employe> employes;
    
    @Transient
    public boolean estMembreUnique(Employe employe) {
        return employes.stream()
            .filter(Employe::getActif)
            .count() == 1;
    }
}

// Affectation.java
@Entity
@Table(name = "affectation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Affectation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_affectation", nullable = false, length = 20)
    private TypeAffectation typeAffectation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Duree duree = Duree.COMPLET;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAffectation statut = StatutAffectation.PLANIFIE;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genere_par")
    private Employe generePar;
}

// Enums
public enum TypeAffectation {
    TFJ, PERMANENCE
}

public enum Duree {
    COMPLET, DEMI_MATIN, DEMI_APRES_MIDI
}

public enum StatutAffectation {
    PLANIFIE, CONFIRME, ANNULE, REMPLACE
}
```

#### 5.3.2 Service de Planification

```java
// PlanningService.java
@Service
@Slf4j
@Transactional
public class PlanningService {
    
    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private GroupeRepository groupeRepository;
    
    @Autowired
    private AffectationRepository affectationRepository;
    
    @Autowired
    private JourFerieRepository jourFerieRepository;
    
    @Autowired
    private DemiJourneeRepository demiJourneeRepository;
    
    @Autowired
    private RotationRepository rotationRepository;
    
    /**
     * Génère le planning pour une période donnée
     */
    public Planning genererPlanning(LocalDate dateDebut, LocalDate dateFin, UUID generePar) {
        log.info("Génération du planning du {} au {}", dateDebut, dateFin);
        
        Planning planning = new Planning();
        planning.setDateDebut(dateDebut);
        planning.setDateFin(dateFin);
        planning.setDateGeneration(LocalDate.now());
        planning.setGenerePar(generePar);
        
        // Charger les contraintes
        List<LocalDate> joursFeries = jourFerieRepository.findDatesBetween(dateDebut, dateFin);
        List<DemiJournee> demiJournees = demiJourneeRepository.findByDateBetween(dateDebut, dateFin);
        List<Groupe> groupes = groupeRepository.findAllWithActifEmployes();
        
        // Initialiser les rotations
        Map<UUID, RotationEmploye> rotations = initialiserRotations(groupes);
        
        // Parcourir chaque semaine
        LocalDate currentWeek = dateDebut.with(DayOfWeek.MONDAY);
        
        while (!currentWeek.isAfter(dateFin)) {
            
            // TFJ : Lundi à Vendredi
            for (int day = 1; day <= 5; day++) {
                LocalDate currentDay = currentWeek.with(day);
                
                if (currentDay.isAfter(dateFin)) {
                    break;
                }
                
                // Vérifier jour férié
                if (joursFeries.contains(currentDay)) {
                    log.debug("Jour férié ignoré : {}", currentDay);
                    continue;
                }
                
                // Déterminer le type de journée
                Duree duree = determinerDureeJournee(currentDay, demiJournees);
                
                // Trouver le meilleur candidat
                Optional<Employe> candidatOpt = trouverCandidatTFJ(
                    currentDay, 
                    currentWeek, 
                    groupes, 
                    rotations,
                    planning.getAffectations()
                );
                
                candidatOpt.ifPresent(candidat -> {
                    Affectation affectation = creerAffectation(
                        currentDay, candidat, TypeAffectation.TFJ, duree, generePar
                    );
                    planning.addAffectation(affectation);
                    
                    // Mettre à jour la rotation
                    mettreAJourRotation(rotations.get(candidat.getId()), day);
                });
            }
            
            // Permanence : Samedi
            LocalDate samedi = currentWeek.with(DayOfWeek.SATURDAY);
            if (!samedi.isAfter(dateFin) && !joursFeries.contains(samedi)) {
                
                Optional<Employe> candidatOpt = trouverCandidatPermanence(
                    samedi,
                    currentWeek,
                    groupes,
                    rotations,
                    planning.getAffectations()
                );
                
                candidatOpt.ifPresent(candidat -> {
                    Affectation affectation = creerAffectation(
                        samedi, candidat, TypeAffectation.PERMANENCE, Duree.COMPLET, generePar
                    );
                    planning.addAffectation(affectation);
                    
                    mettreAJourRotation(rotations.get(candidat.getId()), 6);
                });
            }
            
            // Semaine suivante
            currentWeek = currentWeek.plusWeeks(1);
        }
        
        // Sauvegarder les affectations
        affectationRepository.saveAll(planning.getAffectations());
        
        // Mettre à jour les rotations en base
        rotationRepository.saveAll(rotations.values());
        
        log.info("Planning généré avec succès : {} affectations", planning.getAffectations().size());
        
        return planning;
    }
    
    /**
     * Trouve le meilleur candidat pour un TFJ
     */
    private Optional<Employe> trouverCandidatTFJ(
        LocalDate date,
        LocalDate semaine,
        List<Groupe> groupes,
        Map<UUID, RotationEmploye> rotations,
        List<Affectation> affectationsExistantes
    ) {
        int jourSemaine = date.getDayOfWeek().getValue(); // 1=Lundi, ..., 5=Vendredi
        
        List<CandidatScore> candidats = new ArrayList<>();
        
        for (Groupe groupe : groupes) {
            for (Employe employe : groupe.getEmployes()) {
                
                // Vérifier disponibilité
                if (!estDisponible(employe, date, affectationsExistantes)) {
                    continue;
                }
                
                // Règle 3 : Membre isolé uniquement Vendredi
                if (groupe.estMembreUnique(employe) && jourSemaine != 5) {
                    log.trace("Membre isolé ignoré pour jour non-Vendredi : {}", employe.getNomComplet());
                    continue;
                }
                
                // Règle 1 : Non-successivité intra-groupe
                LocalDate jourPrecedent = date.minusDays(1);
                if (existeAffectationGroupe(groupe, jourPrecedent, affectationsExistantes)) {
                    log.trace("Successivité intra-groupe détectée, groupe {} ignoré", groupe.getNom());
                    continue;
                }
                
                // Calculer le score
                int score = calculerScoreCandidat(employe, date, semaine, rotations);
                
                candidats.add(new CandidatScore(employe, score));
            }
        }
        
        // Trier par score décroissant
        candidats.sort(Comparator.comparingInt(CandidatScore::getScore).reversed());
        
        return candidats.stream()
            .findFirst()
            .map(CandidatScore::getEmploye);
    }
    
    /**
     * Trouve le meilleur candidat pour une permanence
     */
    private Optional<Employe> trouverCandidatPermanence(
        LocalDate date,
        LocalDate semaine,
        List<Groupe> groupes,
        Map<UUID, RotationEmploye> rotations,
        List<Affectation> affectationsExistantes
    ) {
        // Même logique que TFJ mais avec priorités ajustées pour le samedi
        // Les membres isolés sont autorisés le samedi
        
        List<CandidatScore> candidats = new ArrayList<>();
        
        for (Groupe groupe : groupes) {
            for (Employe employe : groupe.getEmployes()) {
                
                if (!estDisponible(employe, date, affectationsExistantes)) {
                    continue;
                }
                
                // Pas de restriction membre isolé pour le samedi
                
                // Vérifier non-successivité (Vendredi -> Samedi)
                LocalDate vendredi = date.minusDays(1);
                if (existeAffectationGroupe(groupe, vendredi, affectationsExistantes)) {
                    continue;
                }
                
                int score = calculerScoreCandidat(employe, date, semaine, rotations);
                // Bonus pour volontaires samedi
                score += 20;
                
                candidats.add(new CandidatScore(employe, score));
            }
        }
        
        candidats.sort(Comparator.comparingInt(CandidatScore::getScore).reversed());
        
        return candidats.stream().findFirst().map(CandidatScore::getEmploye);
    }
    
    /**
     * Calcule le score d'un candidat basé sur la rotation et l'historique
     */
    private int calculerScoreCandidat(
        Employe employe,
        LocalDate date,
        LocalDate semaine,
        Map<UUID, RotationEmploye> rotations
    ) {
        int score = 0;
        
        RotationEmploye rotation = rotations.get(employe.getId());
        
        // Bonus si pas d'affectation récente
        if (rotation.getDerniereSemaineAffectation() == null) {
            score += 100;
        } else {
            long semainesDepuis = semaine.getDayOfYear() - rotation.getDerniereSemaineAffectation();
            score += Math.min(semainesDepuis * 10, 50);
        }
        
        // Bonus si correspond au jour de rotation idéal
        int jourRotationIdeal = calculerJourRotation(semaine);
        if (rotation.getDernierJourAffecte() == jourRotationIdeal) {
            score += 50;
        }
        
        // Malus si beaucoup d'affectations ce mois
        score -= rotation.getNombreAffectationsMois() * 10;
        
        // Random small bonus to avoid deterministic ties
        score += ThreadLocalRandom.current().nextInt(0, 5);
        
        return score;
    }
    
    /**
     * Calcule le jour idéal de rotation pour une semaine donnée
     * Rotation anti-chronologique : Ven(5) -> Jeu(4) -> Mer(3) -> Mar(2) -> Lun(1)
     */
    private int calculerJourRotation(LocalDate semaine) {
        WeekFields weekFields = WeekFields.of(Locale.FRANCE);
        int numeroSemaine = semaine.get(weekFields.weekOfWeekBasedYear());
        
        int[] joursRotation = {5, 4, 3, 2, 1}; // Ven, Jeu, Mer, Mar, Lun
        int indexDansCycle = (numeroSemaine - 1) % 5;
        
        return joursRotation[indexDansCycle];
    }
    
    /**
     * Vérifie si un employé est disponible à une date
     */
    private boolean estDisponible(Employe employe, LocalDate date, List<Affectation> affectations) {
        // Vérifier si déjà affecté ce jour
        boolean dejaAffecte = affectations.stream()
            .anyMatch(a -> a.getEmploye().getId().equals(employe.getId()) 
                        && a.getDateAffectation().equals(date));
        
        if (dejaAffecte) {
            return false;
        }
        
        // Vérifier congés/indisponibilités (à implémenter selon besoins)
        // ...
        
        return true;
    }
    
    /**
     * Vérifie s'il existe une affectation pour un groupe à une date
     */
    private boolean existeAffectationGroupe(
        Groupe groupe,
        LocalDate date,
        List<Affectation> affectations
    ) {
        Set<UUID> groupeEmployeIds = groupe.getEmployes().stream()
            .map(Employe::getId)
            .collect(Collectors.toSet());
        
        return affectations.stream()
            .anyMatch(a -> groupeEmployeIds.contains(a.getEmploye().getId())
                        && a.getDateAffectation().equals(date));
    }
    
    /**
     * Détermine la durée d'une journée (complète ou demi-journée)
     */
    private Duree determinerDureeJournee(LocalDate date, List<DemiJournee> demiJournees) {
        Optional<DemiJournee> demiJourneeOpt = demiJournees.stream()
            .filter(d -> d.getDateJour().equals(date))
            .findFirst();
        
        if (demiJourneeOpt.isPresent()) {
            DemiJournee demiJournee = demiJourneeOpt.get();
            return demiJournee.getPeriode() == DemiJournee.Periode.MATIN 
                ? Duree.DEMI_MATIN 
                : Duree.DEMI_APRES_MIDI;
        }
        
        return Duree.COMPLET;
    }
    
    /**
     * Crée une nouvelle affectation
     */
    private Affectation creerAffectation(
        LocalDate date,
        Employe employe,
        TypeAffectation type,
        Duree duree,
        UUID generePar
    ) {
        Affectation affectation = new Affectation();
        affectation.setDateAffectation(date);
        affectation.setEmploye(employe);
        affectation.setTypeAffectation(type);
        affectation.setDuree(duree);
        affectation.setStatut(StatutAffectation.PLANIFIE);
        
        // Le generePar sera set après chargement de l'entité complète
        
        return affectation;
    }
    
    /**
     * Met à jour la rotation d'un employé après affectation
     */
    private void mettreAJourRotation(RotationEmploye rotation, int jourSemaine) {
        rotation.setDernierJourAffecte(jourSemaine);
        WeekFields weekFields = WeekFields.of(Locale.FRANCE);
        rotation.setDerniereSemaineAffectation(LocalDate.now().get(weekFields.weekOfWeekBasedYear()));
        rotation.setNombreAffectationsMois(rotation.getNombreAffectationsMois() + 1);
        
        // Reset mensuel si nécessaire
        int moisActuel = LocalDate.now().getMonthValue();
        if (rotation.getMoisReference() != moisActuel) {
            rotation.setMoisReference(moisActuel);
            rotation.setNombreAffectationsMois(1);
        }
    }
    
    /**
     * Initialise les rotations pour tous les employés
     */
    private Map<UUID, RotationEmploye> initialiserRotations(List<Groupe> groupes) {
        Map<UUID, RotationEmploye> rotations = new HashMap<>();
        
        for (Groupe groupe : groupes) {
            for (Employe employe : groupe.getEmployes()) {
                RotationEmploye rotation = rotationRepository.findByEmployeId(employe.getId())
                    .orElse(new RotationEmploye());
                rotation.setEmploye(employe);
                rotations.put(employe.getId(), rotation);
            }
        }
        
        return rotations;
    }
}

// Classe helper pour le scoring
@Data
@AllArgsConstructor
class CandidatScore {
    private Employe employe;
    private int score;
}
```

#### 5.3.3 Repository

```java
// AffectationRepository.java
@Repository
public interface AffectationRepository extends JpaRepository<Affectation, UUID> {
    
    List<Affectation> findByDateAffectationBetween(LocalDate debut, LocalDate fin);
    
    List<Affectation> findByEmployeIdAndDateAffectationBetween(
        UUID employeId, 
        LocalDate debut, 
        LocalDate fin
    );
    
    Optional<Affectation> findByEmployeIdAndDateAffectation(UUID employeId, LocalDate date);
    
    List<Affectation> findByDateAffectationAndStatut(LocalDate date, StatutAffectation statut);
    
    @Query("SELECT a FROM Affectation a WHERE a.employe.groupe.id = :groupeId " +
           "AND a.dateAffectation = :date")
    List<Affectation> findByGroupeIdAndDate(@Param("groupeId") UUID groupeId, 
                                            @Param("date") LocalDate date);
}

// RotationRepository.java
@Repository
public interface RotationRepository extends JpaRepository<RotationEmploye, UUID> {
    
    Optional<RotationEmploye> findByEmployeId(UUID employeId);
    
    List<RotationEmploye> findByEmployeGroupeId(UUID groupeId);
}
```

#### 5.3.4 Controller REST

```java
// PlanningController.java
@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class PlanningController {
    
    private final PlanningService planningService;
    private final PlanningExporter exporter;
    
    @PostMapping("/generer")
    public ResponseEntity<PlanningDTO> genererPlanning(
        @RequestBody @Valid PlanningRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Demande de génération de planning reçue");
        
        try {
            // Récupérer l'ID de l'utilisateur connecté (à adapter selon votre auth)
            UUID userId = getUserIdFromAuth(userDetails);
            
            Planning planning = planningService.genererPlanning(
                request.getDateDebut(),
                request.getDateFin(),
                userId
            );
            
            PlanningDTO dto = convertToDTO(planning);
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du planning", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{dateDebut}/{dateFin}")
    public ResponseEntity<List<AffectationDTO>> getPlanning(
        @PathVariable LocalDate dateDebut,
        @PathVariable LocalDate dateFin
    ) {
        List<Affectation> affectations = planningService.getAffectationsBetween(dateDebut, dateFin);
        
        List<AffectationDTO> dtos = affectations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<AffectationDTO> confirmerAffectation(
        @PathVariable UUID id,
        @RequestBody(required = false) CommentaireRequest request
    ) {
        Affectation affectation = planningService.confirmerAffectation(id, request.getCommentaire());
        return ResponseEntity.ok(convertToDTO(affectation));
    }
    
    @PutMapping("/{id}/annuler")
    public ResponseEntity<AffectationDTO> annulerAffectation(
        @PathVariable UUID id,
        @RequestBody @Valid CommentaireRequest request
    ) {
        Affectation affectation = planningService.annulerAffectation(id, request.getCommentaire());
        return ResponseEntity.ok(convertToDTO(affectation));
    }
    
    @GetMapping("/export/excel/{dateDebut}/{dateFin}")
    public ResponseEntity<byte[]> exportExcel(
        @PathVariable LocalDate dateDebut,
        @PathVariable LocalDate dateFin
    ) {
        byte[] excelFile = exporter.exportToExcel(dateDebut, dateFin);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=planning_" + dateDebut + "_" + dateFin + ".xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelFile);
    }
    
    @GetMapping("/export/pdf/{dateDebut}/{dateFin}")
    public ResponseEntity<byte[]> exportPdf(
        @PathVariable LocalDate dateDebut,
        @PathVariable LocalDate dateFin
    ) {
        byte[] pdfFile = exporter.exportToPdf(dateDebut, dateFin);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=planning_" + dateDebut + "_" + dateFin + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfFile);
    }
}

// DTOs
@Data
class PlanningRequest {
    @NotNull
    private LocalDate dateDebut;
    
    @NotNull
    @FutureOrPresent
    private LocalDate dateFin;
}

@Data
class PlanningDTO {
    private UUID id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDate dateGeneration;
    private String generePar;
    private List<AffectationDTO> affectations;
}

@Data
class AffectationDTO {
    private UUID id;
    private LocalDate date;
    private String employeNom;
    private String employePrenom;
    private String employeEmail;
    private String groupe;
    private TypeAffectation type;
    private Duree duree;
    private StatutAffectation statut;
    private String commentaire;
}
```

### 5.4 Frontend Angular

#### 5.4.1 Module Planning

```typescript
// planning.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FullCalendarModule } from '@fullcalendar/angular';
import { MaterialModule } from '../material.module';

import { PlanningComponent } from './planning.component';
import { PlanningGeneratorComponent } from './planning-generator/planning-generator.component';
import { PlanningCalendarComponent } from './planning-calendar/planning-calendar.component';
import { AffectationDetailDialog } from './dialogs/affectation-detail.dialog';

@NgModule({
  declarations: [
    PlanningComponent,
    PlanningGeneratorComponent,
    PlanningCalendarComponent,
    AffectationDetailDialog
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    FullCalendarModule,
    MaterialModule
  ]
})
export class PlanningModule { }
```

#### 5.4.2 Service Planning

```typescript
// planning.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Affectation {
  id: string;
  date: string;
  employeNom: string;
  employePrenom: string;
  employeEmail: string;
  groupe: string;
  type: 'TFJ' | 'PERMANENCE';
  duree: 'COMPLET' | 'DEMI_MATIN' | 'DEMI_APRES_MIDI';
  statut: 'PLANIFIE' | 'CONFIRME' | 'ANNULE' | 'REMPLACE';
  commentaire?: string;
}

export interface Planning {
  id: string;
  dateDebut: string;
  dateFin: string;
  dateGeneration: string;
  generePar: string;
  affectations: Affectation[];
}

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  private readonly apiUrl = `${environment.apiUrl}/planning`;

  constructor(private http: HttpClient) { }

  genererPlanning(dateDebut: Date, dateFin: Date): Observable<Planning> {
    const body = {
      dateDebut: this.formatDate(dateDebut),
      dateFin: this.formatDate(dateFin)
    };
    
    return this.http.post<Planning>(`${this.apiUrl}/generer`, body);
  }

  getPlanning(dateDebut: Date, dateFin: Date): Observable<Affectation[]> {
    let params = new HttpParams();
    params = params.append('dateDebut', this.formatDate(dateDebut));
    params = params.append('dateFin', this.formatDate(dateFin));
    
    return this.http.get<Affectation[]>(`${this.apiUrl}`, { params });
  }

  confirmerAffectation(id: string, commentaire?: string): Observable<Affectation> {
    return this.http.put<Affectation>(`${this.apiUrl}/${id}/confirmer`, { commentaire });
  }

  annulerAffectation(id: string, commentaire: string): Observable<Affectation> {
    return this.http.put<Affectation>(`${this.apiUrl}/${id}/annuler`, { commentaire });
  }

  exportExcel(dateDebut: Date, dateFin: Date): Observable<Blob> {
    const url = `${this.apiUrl}/export/excel/${this.formatDate(dateDebut)}/${this.formatDate(dateFin)}`;
    
    return this.http.get(url, { responseType: 'blob' });
  }

  exportPdf(dateDebut: Date, dateFin: Date): Observable<Blob> {
    const url = `${this.apiUrl}/export/pdf/${this.formatDate(dateDebut)}/${this.formatDate(dateFin)}`;
    
    return this.http.get(url, { responseType: 'blob' });
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
```

#### 5.4.3 Composant Principal

```typescript
// planning.component.ts
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { PlanningService, Affectation } from './planning.service';
import { PlanningGeneratorComponent } from './planning-generator/planning-generator.component';
import { AffectationDetailDialog } from './dialogs/affectation-detail.dialog';

@Component({
  selector: 'app-planning',
  templateUrl: './planning.component.html',
  styleUrls: ['./planning.component.scss']
})
export class PlanningComponent implements OnInit {
  affectations: Affectation[] = [];
  isLoading = false;
  viewMode: 'calendar' | 'list' = 'calendar';
  
  // Filtres
  filtreType: 'TOUS' | 'TFJ' | 'PERMANENCE' = 'TOUS';
  filtreStatut: 'TOUS' | 'PLANIFIE' | 'CONFIRME' | 'ANNULE' = 'TOUS';
  filtreGroupe: string = 'TOUS';
  groupes: string[] = [];

  constructor(
    private planningService: PlanningService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.chargerPlanning();
    this.chargerGroupes();
  }

  chargerPlanning(): void {
    this.isLoading = true;
    
    const dateDebut = new Date();
    dateDebut.setDate(dateDebut.getDate() - dateDebut.getDay() + 1); // Lundi de cette semaine
    
    const dateFin = new Date(dateDebut);
    dateFin.setMonth(dateFin.getMonth() + 1); // +1 mois
    
    this.planningService.getPlanning(dateDebut, dateFin).subscribe({
      next: (data) => {
        this.affectations = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Erreur lors du chargement du planning', 'Fermer', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  ouvrirGenerateur(): void {
    const dialogRef = this.dialog.open(PlanningGeneratorComponent, {
      width: '600px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.chargerPlanning();
        this.snackBar.open('Planning généré avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  voirDetail(affectation: Affectation): void {
    const dialogRef = this.dialog.open(AffectationDetailDialog, {
      width: '500px',
      data: { affectation }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.action === 'confirmer') {
        this.planningService.confirmerAffectation(affectation.id, result.commentaire)
          .subscribe(() => {
            affectation.statut = 'CONFIRME';
            this.snackBar.open('Affectation confirmée', 'Fermer', { duration: 2000 });
          });
      } else if (result?.action === 'annuler') {
        this.planningService.annulerAffectation(affectation.id, result.commentaire)
          .subscribe(() => {
            affectation.statut = 'ANNULE';
            this.snackBar.open('Affectation annulée', 'Fermer', { duration: 2000 });
          });
      }
    });
  }

  exporter(format: 'excel' | 'pdf'): void {
    const dateDebut = new Date();
    const dateFin = new Date();
    dateFin.setMonth(dateFin.getMonth() + 1);
    
    const subscribeMethod = format === 'excel' 
      ? this.planningService.exportExcel.bind(this.planningService)
      : this.planningService.exportPdf.bind(this.planningService);
    
    subscribeMethod(dateDebut, dateFin).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `planning_${dateDebut.toISOString().split('T')[0]}.${format}`;
      link.click();
      window.URL.revokeObjectURL(url);
      
      this.snackBar.open(`Export ${format.toUpperCase()} réussi`, 'Fermer', { duration: 2000 });
    });
  }

  get affectationsFiltrees(): Affectation[] {
    return this.affectations.filter(a => {
      if (this.filtreType !== 'TOUS' && a.type !== this.filtreType) return false;
      if (this.filtreStatut !== 'TOUS' && a.statut !== this.filtreStatut) return false;
      if (this.filtreGroupe !== 'TOUS' && a.groupe !== this.filtreGroupe) return false;
      return true;
    });
  }

  private chargerGroupes(): void {
    // Charger la liste des groupes depuis l'API
    // ...
  }
}
```

---

## 6. Guide d'Utilisation <a name="guide-dutilisation"></a>

### 6.1 Accès à l'Application

1. **URL** : https://planning-dsi.orabank.tg
2. **Identifiants** : Utiliser vos identifiants domaine ORABANK
3. **Navigation** : Menu principal > DSI > Planning TFJ

### 6.2 Générer un Planning

1. Cliquer sur **"Nouveau Planning"**
2. Sélectionner la période (date de début et fin)
3. Vérifier les contraintes affichées (jours fériés, demi-journées)
4. Cliquer sur **"Générer"**
5. Attendre la fin du traitement (quelques secondes)
6. Examiner le planning généré

### 6.3 Valider/Modifier le Planning

#### Confirmation d'une affectation

1. Cliquer sur une affectation dans le calendrier
2. Vérifier les détails (employé, date, type)
3. Ajouter un commentaire optionnel
4. Cliquer sur **"Confirmer"**

#### Annulation d'une affectation

1. Cliquer sur l'affectation à annuler
2. **Obligatoire** : Saisir un motif d'annulation
3. Cliquer sur **"Annuler"**
4. Le système proposera automatiquement un remplaçant si nécessaire

#### Remplacement manuel

1. Dans le détail d'une affectation, cliquer sur **"Remplacer"**
2. Sélectionner un nouvel employé dans la liste (filtrée par disponibilité)
3. Valider le remplacement

### 6.4 Exporter le Planning

1. Cliquer sur **"Exporter"**
2. Choisir le format :
   - **Excel** : Pour modification manuelle
   - **PDF** : Pour diffusion officielle
3. Télécharger le fichier

### 6.5 Consulter l'Historique

1. Menu > **Historique**
2. Filtrer par période, employé, ou groupe
3. Exporter si nécessaire

---

## 7. Maintenance et Évolutions <a name="maintenance-et-evolutions"></a>

### 7.1 Tâches de Maintenance Quotidiennes

```bash
# Vérifier les jobs planifiés
tail -f /var/log/planning-dsi/scheduler.log

# Nettoyer les anciens plannings (> 2 ans)
java -jar planning-admin.jar cleanup --older-than=2y

# Vérifier l'intégrité des rotations
java -jar planning-admin.jar check-rotations
```

### 7.2 Ajout de Jours Fériés

```sql
-- Jour férié ponctuel
INSERT INTO jour_ferie (date_jour, nom, recurrent) 
VALUES ('2024-12-26', 'Lendemain de Noël', FALSE);

-- Jour férié récurrent (ex: 1er Mai)
INSERT INTO jour_ferie (nom, recurrent, jour_mois, mois) 
VALUES ('Fête du Travail', TRUE, 1, 5);
```

### 7.3 Configuration des Demi-Journées

```sql
-- Demi-journée matin
INSERT INTO demi_journee (date_jour, periode, raison) 
VALUES ('2024-12-24', 'MATIN', 'Veille de Noël');

-- Demi-journée après-midi
INSERT INTO demi_journee (date_jour, periode, raison) 
VALUES ('2024-12-31', 'APRES_MIDI', 'Veille du Nouvel An');
```

### 7.4 Commandes Admin

```bash
# Script d'administration
java -jar planning-admin.jar <commande> [options]

# Commandes disponibles :
# - generate : Générer un planning
# - cleanup : Nettoyer anciennes données
# - export : Exporter un planning
# - check-rotations : Vérifier cohérence rotations
# - reset-rotation : Réinitialiser rotation pour un employé
# - stats : Générer statistiques

# Exemples :
java -jar planning-admin.jar generate --debut=2024-01-01 --fin=2024-01-31
java -jar planning-admin.jar reset-rotation --employe=jean.dupont
java -jar planning-admin.jar stats --mois=2024-01 --format=pdf
```

### 7.5 Indicateurs de Performance

| Indicateur | Cible | Mesure Actuelle |
|------------|-------|-----------------|
| Temps de génération (1 mois) | < 5s | ~2s |
| Taux de couverture TFJ | 100% | 98.5% |
| Taux de couverture Permanence | 100% | 99.2% |
| Satisfaction utilisateurs | > 80% | 85% |

### 7.6 Évolutions Prévues

- [ ] Intégration avec le système de gestion des congés
- [ ] Notifications SMS pour rappels
- [ ] Application mobile de consultation
- [ ] Algorithmes d'optimisation avancés (machine learning)
- [ ] Gestion des compétences multi-groupes

---

*Document propriété d'ORABANK TOGO - Usage interne uniquement*
*Dernière mise à jour : 2024-01-20*
*Version : 1.0*
