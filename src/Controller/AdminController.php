<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\UserRepository;
use App\Repository\PlanningRepository;
use App\Entity\User;
use App\Entity\Planning;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
class AdminController extends AbstractController
{
    #[Route('/admin', name: 'app_admin_dashboard')]
    public function dashboard(UserRepository $userRepository, PlanningRepository $planningRepository): Response
    {
        $users = $userRepository->findAll();
        $plannings = $planningRepository->findAllOrderByDate();
        
        return $this->render('admin/dashboard.html.twig', [
            'users' => $users,
            'plannings' => $plannings,
        ]);
    }

    #[Route('/admin/employes', name: 'app_admin_employees')]
    public function employees(UserRepository $userRepository): Response
    {
        $users = $userRepository->findAll();
        
        return $this->render('admin/employees.html.twig', [
            'users' => $users,
        ]);
    }

    #[Route('/admin/employes/ajouter', name: 'app_admin_employee_add')]
    public function addEmployee(
        Request $request, 
        UserRepository $userRepository, 
        UserPasswordHasherInterface $passwordHasher,
        EntityManagerInterface $entityManager
    ): Response {
        if ($request->isMethod('POST')) {
            $user = new User();
            $user->setUsername($request->request->get('username'));
            $user->setNom($request->request->get('nom'));
            $user->setPrenom($request->request->get('prenom'));
            $user->setEmail($request->request->get('email'));
            
            $hashedPassword = $passwordHasher->hashPassword($user, $request->request->get('password'));
            $user->setPassword($hashedPassword);
            
            $roles = $request->request->all('roles', []);
            $user->setRoles($roles);
            
            $userRepository->save($user, true);
            
            $this->addFlash('success', 'Employé ajouté avec succès.');
            
            return $this->redirectToRoute('app_admin_employees');
        }
        
        return $this->render('admin/employee_form.html.twig', [
            'action' => 'add',
        ]);
    }

    #[Route('/admin/employes/modifier/{id}', name: 'app_admin_employee_edit')]
    public function editEmployee(
        int $id,
        Request $request, 
        UserRepository $userRepository, 
        UserPasswordHasherInterface $passwordHasher,
        EntityManagerInterface $entityManager
    ): Response {
        $user = $userRepository->find($id);
        
        if (!$user) {
            throw $this->createNotFoundException('Employé non trouvé.');
        }
        
        if ($request->isMethod('POST')) {
            $user->setUsername($request->request->get('username'));
            $user->setNom($request->request->get('nom'));
            $user->setPrenom($request->request->get('prenom'));
            $user->setEmail($request->request->get('email'));
            
            if ($request->request->get('password')) {
                $hashedPassword = $passwordHasher->hashPassword($user, $request->request->get('password'));
                $user->setPassword($hashedPassword);
            }
            
            $roles = $request->request->all('roles', []);
            $user->setRoles($roles);
            
            $userRepository->save($user, true);
            
            $this->addFlash('success', 'Employé modifié avec succès.');
            
            return $this->redirectToRoute('app_admin_employees');
        }
        
        return $this->render('admin/employee_form.html.twig', [
            'action' => 'edit',
            'user' => $user,
        ]);
    }

    #[Route('/admin/employes/supprimer/{id}', name: 'app_admin_employee_delete', methods: ['POST'])]
    public function deleteEmployee(
        int $id,
        Request $request,
        UserRepository $userRepository,
        EntityManagerInterface $entityManager
    ): Response {
        $user = $userRepository->find($id);
        
        if (!$user) {
            throw $this->createNotFoundException('Employé non trouvé.');
        }
        
        $userRepository->remove($user, true);
        
        $this->addFlash('success', 'Employé supprimé avec succès.');
        
        return $this->redirectToRoute('app_admin_employees');
    }

    #[Route('/admin/planning', name: 'app_admin_planning')]
    public function planning(
        Request $request,
        PlanningRepository $planningRepository,
        EntityManagerInterface $entityManager
    ): Response {
        $plannings = $planningRepository->findAllOrderByDate();
        
        if ($request->isMethod('POST')) {
            $date = new \DateTime($request->request->get('date'));
            
            $planning = new Planning();
            $planning->setDate($date);
            $planning->setMatin($request->request->get('matin'));
            $planning->setApresMidi($request->request->get('apres_midi'));
            $planning->setSoir($request->request->get('soir'));
            $planning->setCommentaire($request->request->get('commentaire'));
            
            $planningRepository->save($planning, true);
            
            $this->addFlash('success', 'Planning généré avec succès.');
            
            return $this->redirectToRoute('app_admin_planning');
        }
        
        return $this->render('admin/planning.html.twig', [
            'plannings' => $plannings,
        ]);
    }

    #[Route('/admin/planning/supprimer/{id}', name: 'app_admin_planning_delete', methods: ['POST'])]
    public function deletePlanning(
        int $id,
        Request $request,
        PlanningRepository $planningRepository
    ): Response {
        $planning = $planningRepository->find($id);
        
        if (!$planning) {
            throw $this->createNotFoundException('Planning non trouvé.');
        }
        
        $planningRepository->remove($planning, true);
        
        $this->addFlash('success', 'Planning supprimé avec succès.');
        
        return $this->redirectToRoute('app_admin_planning');
    }
}
