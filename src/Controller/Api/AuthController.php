<?php

namespace App\Controller\Api;

use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/auth')]
class AuthController extends AbstractController
{
    public function __construct(
        private UserRepository $userRepository,
        private UserPasswordHasherInterface $passwordHasher
    ) {}

    #[Route('/login', methods: ['POST'])]
    public function login(Request $request): JsonResponse
    {
        $data = json_decode($request->getContent(), true);

        if (empty($data['username']) || empty($data['password'])) {
            return new JsonResponse([
                'message' => 'Identifiants requis'
            ], Response::HTTP_BAD_REQUEST);
        }

        $user = $this->userRepository->findOneBy(['username' => $data['username']]);

        if (!$user) {
            return new JsonResponse([
                'message' => 'Identifiants incorrects'
            ], Response::HTTP_UNAUTHORIZED);
        }

        if (!$this->passwordHasher->isPasswordValid($user, $data['password'])) {
            return new JsonResponse([
                'message' => 'Identifiants incorrects'
            ], Response::HTTP_UNAUTHORIZED);
        }

        // Générer un token basique (à remplacer par JWT en production)
        $token = base64_encode(random_bytes(32));
        
        // Stocker le token en session pour validation ultérieure
        $session = $request->getSession();
        $session->set('auth_token_' . $token, [
            'user_id' => $user->getId(),
            'username' => $user->getUsername(),
            'email' => $user->getEmail(),
            'role' => $user->getRoles()[0] ?? 'USER'
        ]);

        return new JsonResponse([
            'token' => $token,
            'username' => $user->getUsername(),
            'email' => $user->getEmail(),
            'role' => $user->getRoles()[0] ?? 'USER',
            'fullName' => $user->getNom() . ' ' . $user->getPrenom()
        ]);
    }

    #[Route('/logout', methods: ['POST'])]
    public function logout(Request $request): JsonResponse
    {
        $token = $request->headers->get('Authorization');
        if ($token && str_starts_with($token, 'Bearer ')) {
            $token = substr($token, 7);
            $session = $request->getSession();
            $session->remove('auth_token_' . $token);
        }

        return new JsonResponse(['message' => 'Déconnexion réussie']);
    }
}
