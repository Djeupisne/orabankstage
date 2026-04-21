<?php

namespace App\Command;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:create-user',
    description: 'Crée un nouvel utilisateur dans la base de données',
)]
class CreateUserCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $em,
        private UserPasswordHasherInterface $passwordHasher
    ) {
        parent::__construct();
    }

    protected function configure(): void
    {
        $this
            ->addArgument('username', InputArgument::REQUIRED, 'Nom d\'utilisateur')
            ->addArgument('password', InputArgument::REQUIRED, 'Mot de passe')
            ->addArgument('nom', InputArgument::REQUIRED, 'Nom de famille')
            ->addArgument('prenom', InputArgument::REQUIRED, 'Prénom')
            ->addOption('email', null, InputOption::VALUE_OPTIONAL, 'Adresse email')
            ->addOption('admin', null, InputOption::VALUE_NONE, 'Donner le rôle ADMIN');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $username = $input->getArgument('username');
        $password = $input->getArgument('password');
        $nom = $input->getArgument('nom');
        $prenom = $input->getArgument('prenom');
        $email = $input->getOption('email');
        $isAdmin = $input->getOption('admin');

        // Vérifier si l'utilisateur existe déjà
        $existingUser = $this->em->getRepository(User::class)->findOneBy(['username' => $username]);
        if ($existingUser) {
            $io->error("L'utilisateur '$username' existe déjà.");
            return Command::FAILURE;
        }

        $user = new User();
        $user->setUsername($username);
        $user->setNom($nom);
        $user->setPrenom($prenom);
        $user->setEmail($email);
        $user->setRoles($isAdmin ? ['ROLE_ADMIN'] : ['ROLE_USER']);

        // Hacher le mot de passe
        $hashedPassword = $this->passwordHasher->hashPassword($user, $password);
        $user->setPassword($hashedPassword);

        $this->em->persist($user);
        $this->em->flush();

        $io->success("Utilisateur '$username' créé avec succès !");
        $io->table(
            ['Champ', 'Valeur'],
            [
                ['Username', $username],
                ['Nom', $nom],
                ['Prénom', $prenom],
                ['Email', $email ?? 'N/A'],
                ['Rôle', $isAdmin ? 'ADMIN' : 'USER'],
            ]
        );

        return Command::SUCCESS;
    }
}
