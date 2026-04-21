#!/bin/bash

echo "=== Test de l'API de login ==="
echo ""

# Test avec admin/admin123
echo "Test 1: Connexion avec admin/admin123"
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -w "\nCode HTTP: %{http_code}\n" 2>/dev/null || echo "Erreur: Backend non disponible sur le port 8000"

echo ""
echo "Test 2: Connexion avec user/user123"
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' \
  -w "\nCode HTTP: %{http_code}\n" 2>/dev/null || echo "Erreur: Backend non disponible sur le port 8000"

echo ""
echo "Test 3: Mauvais mot de passe"
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' \
  -w "\nCode HTTP: %{http_code}\n" 2>/dev/null || echo "Erreur: Backend non disponible sur le port 8000"

echo ""
echo "Test 4: Utilisateur inexistant"
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"inexistant","password":"password"}' \
  -w "\nCode HTTP: %{http_code}\n" 2>/dev/null || echo "Erreur: Backend non disponible sur le port 8000"
