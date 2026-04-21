<?php

namespace App\Repository;

use App\Entity\Planning;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Planning>
 */
class PlanningRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Planning::class);
    }

    public function save(Planning $planning, bool $flush = false): void
    {
        $this->getEntityManager()->persist($planning);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(Planning $planning, bool $flush = false): void
    {
        $this->getEntityManager()->remove($planning);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    /**
     * @return Planning[] Returns an array of Planning objects ordered by date
     */
    public function findAllOrderByDate(): array
    {
        return $this->createQueryBuilder('p')
            ->orderBy('p.date', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Planning[] Returns plannings for a specific month
     */
    public function findByMonth(int $year, int $month): array
    {
        return $this->createQueryBuilder('p')
            ->where('YEAR(p.date) = :year')
            ->andWhere('MONTH(p.date) = :month')
            ->setParameter('year', $year)
            ->setParameter('month', $month)
            ->orderBy('p.date', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
