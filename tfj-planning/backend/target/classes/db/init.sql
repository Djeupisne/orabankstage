-- Script d'initialisation des données pour l'application TFJ Planning
-- Orabank Togo - DSI

-- 1. Insertion des services DSI
INSERT INTO service (name, description) VALUES 
    ('Applications', 'Service des applications métier'),
    ('Infrastructure', 'Service infrastructure réseau et système'),
    ('Exploitation', 'Service exploitation et maintenance');

-- 2. Insertion des rôles / fiches de poste
INSERT INTO role (name, description) VALUES 
    ('Administrateur réseau', 'Gestion et administration du réseau'),
    ('Développeur', 'Développement des applications métier'),
    ('Administrateur système', 'Administration des systèmes et serveurs'),
    ('DBA', 'Administration des bases de données'),
    ('Chef de projet', 'Gestion de projets informatiques'),
    ('Technicien support', 'Support utilisateur et maintenance');

-- 3. Insertion des niveaux hiérarchiques
INSERT INTO hierarchical_level (name, code, priority) VALUES 
    ('Cadre', 'CADRE', 2),
    ('Manager', 'MANAGER', 3),
    ('Collaborateur', 'COLLABORATEUR', 1);

-- 4. Insertion des employés (données d'exemple)
-- Service Applications
INSERT INTO employee (first_name, last_name, email, service_id, role_id, hierarchical_level_id, is_active) VALUES 
    ('Kofi', 'AMENYONOR', 'k.amenyonor@orabank.tg', 
     (SELECT id FROM service WHERE name = 'Applications'),
     (SELECT id FROM role WHERE name = 'Développeur'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Ama', 'TCHASSAN', 'a.tchassan@orabank.tg',
     (SELECT id FROM service WHERE name = 'Applications'),
     (SELECT id FROM role WHERE name = 'Développeur'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Komlan', 'ADJEKPLE', 'k.adjekple@orabank.tg',
     (SELECT id FROM service WHERE name = 'Applications'),
     (SELECT id FROM role WHERE name = 'Chef de projet'),
     (SELECT id FROM hierarchical_level WHERE code = 'CADRE'), true);

-- Service Infrastructure
INSERT INTO employee (first_name, last_name, email, service_id, role_id, hierarchical_level_id, is_active) VALUES 
    ('Folly', 'GANOU', 'f.ganou@orabank.tg',
     (SELECT id FROM service WHERE name = 'Infrastructure'),
     (SELECT id FROM role WHERE name = 'Administrateur réseau'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Esso', 'PITANG', 'e.pitang@orabank.tg',
     (SELECT id FROM service WHERE name = 'Infrastructure'),
     (SELECT id FROM role WHERE name = 'Administrateur réseau'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Akoussivi', 'BANITOKE', 'a.banitoke@orabank.tg',
     (SELECT id FROM service WHERE name = 'Infrastructure'),
     (SELECT id FROM role WHERE name = 'Administrateur système'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true);

-- Service Exploitation
INSERT INTO employee (first_name, last_name, email, service_id, role_id, hierarchical_level_id, is_active) VALUES 
    ('Dodzi', 'KPESSOU', 'd.kpessou@orabank.tg',
     (SELECT id FROM service WHERE name = 'Exploitation'),
     (SELECT id FROM role WHERE name = 'DBA'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Amétévé', 'KOKOU', 'a.kokou@orabank.tg',
     (SELECT id FROM service WHERE name = 'Exploitation'),
     (SELECT id FROM role WHERE name = 'Technicien support'),
     (SELECT id FROM hierarchical_level WHERE code = 'COLLABORATEUR'), true),
    ('Mana', 'ASSOGNA', 'm.assogna@orabank.tg',
     (SELECT id FROM service WHERE name = 'Exploitation'),
     (SELECT id FROM role WHERE name = 'Chef de projet'),
     (SELECT id FROM hierarchical_level WHERE code = 'MANAGER'), true);

-- 5. Insertion des jours fériés (exemple pour 2026)
INSERT INTO non_working_day (date, description, is_full_day, is_morning_only, is_afternoon_only) VALUES 
    ('2026-01-01', 'Jour de l''an', true, false, false),
    ('2026-01-13', 'Fête du Ramadan', true, false, false),
    ('2026-04-03', 'Vendredi Saint', true, false, false),
    ('2026-04-06', 'Lundi de Pâques', true, false, false),
    ('2026-05-01', 'Fête du Travail', true, false, false),
    ('2026-05-14', 'Ascension', true, false, false),
    ('2026-05-25', 'Lundi de Pentecôte', true, false, false),
    ('2026-06-04', 'Fête de Tabaski', true, false, false),
    ('2026-08-15', 'Assomption', true, false, false),
    ('2026-11-01', 'Toussaint', true, false, false),
    ('2026-12-25', 'Noël', true, false, false),
    ('2026-12-26', 'Lendemain de Noël', true, false, false);

-- 6. Demi-journées fériées (exemple)
INSERT INTO non_working_day (date, description, is_full_day, is_morning_only, is_afternoon_only) VALUES 
    ('2026-12-24', 'Veille de Noël (après-midi)', false, true, false),
    ('2026-12-31', 'Veille du Nouvel An (après-midi)', false, true, false);

-- 7. Exemples de congés pour tester la fonctionnalité
-- Congé annuel pour Kofi AMENYONOR du 15 au 19 juin 2026
INSERT INTO conge (employe_id, date_debut, date_fin, est_demi_journee_debut, est_demi_journee_fin, type_conge) VALUES 
    ((SELECT id FROM employee WHERE email = 'k.amenyonor@orabank.tg'), 
     '2026-06-15', '2026-06-19', false, false, 'CONGES_PAYES');

-- Congé pour Folly GANOU du 22 au 26 juin 2026 (semaine suivante)
INSERT INTO conge (employe_id, date_debut, date_fin, est_demi_journee_debut, est_demi_journee_fin, type_conge) VALUES 
    ((SELECT id FROM employee WHERE email = 'f.ganou@orabank.tg'), 
     '2026-06-22', '2026-06-26', false, false, 'CONGES_PAYES');

-- Demi-journée de congé pour Ama TCHASSAN (matin du 8 juin 2026)
INSERT INTO conge (employe_id, date_debut, date_fin, est_demi_journee_debut, est_demi_journee_fin, type_conge) VALUES 
    ((SELECT id FROM employee WHERE email = 'a.tchassan@orabank.tg'), 
     '2026-06-08', '2026-06-08', true, false, 'CONGES_PAYES');

-- Vérification des données insérées
SELECT 'Services créés: ' || COUNT(*) FROM service;
SELECT 'Rôles créés: ' || COUNT(*) FROM role;
SELECT 'Niveaux hiérarchiques créés: ' || COUNT(*) FROM hierarchical_level;
SELECT 'Employés créés: ' || COUNT(*) FROM employee;
SELECT 'Jours fériés créés: ' || COUNT(*) FROM non_working_day;
SELECT 'Congés créés: ' || COUNT(*) FROM conge;

-- 8. Exemples d'absences exceptionnelles pour tester la fonctionnalité
-- Absence maladie pour Esso PITANG du 10 au 12 juin 2026
INSERT INTO absences_exceptionnelles (employe_id, date_debut, date_fin, est_demi_journee_debut, est_demi_journee_fin, motif, commentaire, saisi_par, date_saisie, est_reaffectation_auto) VALUES
    ((SELECT id FROM employee WHERE email = 'e.pitang@orabank.tg'),
     '2026-06-10', '2026-06-12', false, false, 'MALADIE', 
     'Grippe avec fièvre', 'Admin DSI', CURRENT_DATE, true);

-- Absence imprévue pour Akoussivi BANITOKE (demi-journée le 15 juin 2026)
INSERT INTO absences_exceptionnelles (employe_id, date_debut, date_fin, est_demi_journee_debut, est_demi_journee_fin, motif, commentaire, saisi_par, date_saisie, est_reaffectation_auto) VALUES
    ((SELECT id FROM employee WHERE email = 'a.banitoke@orabank.tg'),
     '2026-06-15', '2026-06-15', true, false, 'IMPREVU', 
     'Problème de transport', 'Admin DSI', CURRENT_DATE, true);

SELECT 'Absences exceptionnelles créées: ' || COUNT(*) FROM absences_exceptionnelles;
