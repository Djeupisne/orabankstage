<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\PlanningRepository;

class HomeController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function index(PlanningRepository $planningRepository): Response
    {
        $plannings = $planningRepository->findAllOrderByDate();
        
        return $this->render('home/index.html.twig', [
            'plannings' => $plannings,
        ]);
    }

    #[Route('/planning/download', name: 'app_planning_download')]
    public function downloadPlanning(PlanningRepository $planningRepository): Response
    {
        $plannings = $planningRepository->findAllOrderByDate();
        
        $csvData = "Date;Matin;Après-midi;Soir;Commentaire\n";
        
        foreach ($plannings as $planning) {
            $date = $planning->getDate() ? $planning->getDate()->format('d/m/Y') : '';
            $matin = str_replace(';', ',', $planning->getMatin() ?? '');
            $apresMidi = str_replace(';', ',', $planning->getApresMidi() ?? '');
            $soir = str_replace(';', ',', $planning->getSoir() ?? '');
            $commentaire = str_replace(';', ',', str_replace("\n", ' ', $planning->getCommentaire() ?? ''));
            
            $csvData .= "{$date};{$matin};{$apresMidi};{$soir};{$commentaire}\n";
        }
        
        $response = new Response($csvData);
        $response->headers->set('Content-Type', 'text/csv; charset=utf-8');
        $response->headers->set('Content-Disposition', 'attachment; filename="planning.csv"');
        
        return $response;
    }
}
