-- Script d'initialisation de la base de données
-- Pour créer un utilisateur admin par défaut

-- Table user
CREATE TABLE IF NOT EXISTS "user" (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(180) NOT NULL UNIQUE,
    roles JSON NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    email VARCHAR(255)
);

-- Utilisateur admin par défaut
-- username: admin
-- password: admin123 (hashé avec bcrypt)
INSERT INTO "user" (username, roles, password, nom, prenom, email) 
VALUES (
    'admin',
    '["ROLE_ADMIN"]',
    '$2b$13$OyC5U8w7GTtcufIpKZ4BvebUFO3bkWyCCbRCx9wt7jCCLj8GLYqMS',
    'Administrateur',
    'Système',
    'admin@tfj.fr'
);

-- Utilisateur test
-- username: user
-- password: user123 (hashé avec bcrypt)
INSERT INTO "user" (username, roles, password, nom, prenom, email) 
VALUES (
    'user',
    '["ROLE_USER"]',
    '$2b$13$sLJ0SAnCz7XRGqS22tNIluKZKMYpyUCu9ewEnKxA5pYU/m541BBc2',
    'Utilisateur',
    'Test',
    'user@tfj.fr'
);
