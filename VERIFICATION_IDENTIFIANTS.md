# Vérification et gestion des identifiants

## Problème rencontré
Erreur 401 "Identifiants incorrects" lors de la connexion, suivie d'une erreur 404.

## Solution appliquée

### 1. Création de la base de données avec utilisateurs de test

La base de données SQLite a été initialisée avec deux utilisateurs :

**Administrateur :**
- Username : `admin`
- Password : `admin123`
- Rôle : ROLE_ADMIN

**Utilisateur standard :**
- Username : `user`
- Password : `user123`
- Rôle : ROLE_USER

### 2. Fichiers créés/modifiés

- `/workspace/init_db.sql` - Script SQL pour créer les tables et utilisateurs
- `/workspace/var/data.db` - Base de données SQLite avec les utilisateurs
- `/workspace/src/Command/CreateUserCommand.php` - Commande pour créer un utilisateur
- `/workspace/src/Command/ListUsersCommand.php` - Commande pour lister les utilisateurs

### 3. Comment vérifier les identifiants sur Render

Sur Render, la base de données est probablement PostgreSQL. Voici comment procéder :

#### Option A : Via les logs Render
1. Connectez-vous à votre dashboard Render
2. Allez dans votre service backend
3. Consultez les logs pour voir les erreurs de connexion
4. Vérifiez que la variable `DATABASE_URL` est correctement configurée

#### Option B : Utiliser les commandes Symfony (RECOMMANDÉ)

**Lister les utilisateurs :**
```bash
php bin/console app:list-users
```

**Créer un nouvel utilisateur :**
```bash
php bin/console app:create-user admin admin123 Administrateur Système --email=admin@tfj.fr --admin
```

#### Option C : Créer un endpoint de debug (temporaire)
Ajoutez cette route dans `AuthController.php` pour tester :

```php
#[Route('/test-users', methods: ['GET'])]
public function testUsers(): JsonResponse
{
    $users = $this->userRepository->findAll();
    $data = [];
    foreach ($users as $user) {
        $data[] = [
            'username' => $user->getUsername(),
            'roles' => $user->getRoles(),
            'hasPassword' => !empty($user->getPassword())
        ];
    }
    return new JsonResponse(['users' => $data]);
}
```

### 4. Commandes utiles

**Vérifier la base de données locale :**
```bash
python3 << 'EOF'
import sqlite3
conn = sqlite3.connect('var/data.db')
cursor = conn.cursor()
cursor.execute("SELECT username, roles FROM user")
for row in cursor.fetchall():
    print(f"User: {row[0]}, Roles: {row[1]}")
conn.close()
EOF
```

**Tester l'API de login :**
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 5. Points de vérification pour Render

1. **Base de données :**
   - La base PostgreSQL est-elle bien connectée ?
   - Les tables ont-elles été créées (migrations exécutées) ?
   - Y a-t-il des utilisateurs dans la table `user` ?

2. **Variables d'environnement :**
   - `DATABASE_URL` est-elle correctement configurée ?
   - `APP_SECRET` est-elle définie ?

3. **Migrations :**
   ```bash
   php bin/console doctrine:migrations:migrate
   ```

4. **Logs :**
   - Vérifiez les logs Symfony pour des erreurs DBAL/Doctrine

5. **Exécuter les commandes sur Render :**
   - Connectez-vous en SSH à votre instance Render
   - Ou utilisez les "Shell Commands" dans le dashboard Render
   - Exécutez : `php bin/console app:list-users`

### 6. Commandes Symfony créées

Deux commandes ont été ajoutées pour gérer les utilisateurs :

**Lister tous les utilisateurs :**
```bash
php bin/console app:list-users
```

Affiche un tableau avec tous les utilisateurs, leurs rôles, emails, etc.

**Créer un utilisateur :**
```bash
php bin/console app:create-user <username> <password> <nom> <prenom> [--email=EMAIL] [--admin]
```

Exemples :
```bash
# Créer un admin
php bin/console app:create-user admin admin123 Administrateur Système --email=admin@tfj.fr --admin

# Créer un utilisateur simple
php bin/console app:create-user user user123 Utilisateur Test --email=user@tfj.fr
```

### 7. Procédure de déploiement sur Render

1. **Pousser les modifications :**
   ```bash
   git add .
   git commit -m "Add user commands and init DB"
   git push origin main
   ```

2. **Attendre le déploiement automatique sur Render**

3. **Se connecter en SSH à l'instance Render** (via le dashboard)

4. **Exécuter les migrations :**
   ```bash
   php bin/console doctrine:migrations:migrate
   ```

5. **Créer les utilisateurs :**
   ```bash
   php bin/console app:create-user admin admin123 Administrateur Système --admin
   ```

6. **Vérifier :**
   ```bash
   php bin/console app:list-users
   ```

7. **Tester l'API :**
   ```bash
   curl -X POST https://votre-backend.onrender.com/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```
