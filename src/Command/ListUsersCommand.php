<?php

namespace App\Command;

use App\Repository\UserRepository;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:list-users',
    description: 'Liste tous les utilisateurs de la base de données',
)]
class ListUsersCommand extends Command
{
    public function __construct(
        private UserRepository $userRepository
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $users = $this->userRepository->findAll();

        if (empty($users)) {
            $io->warning('Aucun utilisateur trouvé dans la base de données.');
            return Command::SUCCESS;
        }

        $io->title("Utilisateurs enregistrés (" . count($users) . ")");

        $tableData = [];
        foreach ($users as $user) {
            $tableData[] = [
                $user->getId(),
                $user->getUsername(),
                $user->getNom(),
                $user->getPrenom() ?? 'N/A',
                $user->getEmail() ?? 'N/A',
                implode(', ', $user->getRoles()),
                !empty($user->getPassword()) ? 'Oui' : 'Non'
            ];
        }

        $io->table(
            ['ID', 'Username', 'Nom', 'Prénom', 'Email', 'Rôles', 'Mot de passe'],
            $tableData
        );

        return Command::SUCCESS;
    }
}
