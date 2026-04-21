<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity(repositoryClass: \App\Repository\PlanningRepository::class)]
#[ORM\Table(name: '`planning`')]
class Planning
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'date')]
    private ?\DateTimeInterface $date = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $matin = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $apresMidi = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $soir = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $commentaire = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $createdAt = null;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDate(): ?\DateTimeInterface
    {
        return $this->date;
    }

    public function setDate(\DateTimeInterface $date): static
    {
        $this->date = $date;
        return $this;
    }

    public function getMatin(): ?string
    {
        return $this->matin;
    }

    public function setMatin(?string $matin): static
    {
        $this->matin = $matin;
        return $this;
    }

    public function getApresMidi(): ?string
    {
        return $this->apresMidi;
    }

    public function setApresMidi(?string $apresMidi): static
    {
        $this->apresMidi = $apresMidi;
        return $this;
    }

    public function getSoir(): ?string
    {
        return $this->soir;
    }

    public function setSoir(?string $soir): static
    {
        $this->soir = $soir;
        return $this;
    }

    public function getCommentaire(): ?string
    {
        return $this->commentaire;
    }

    public function setCommentaire(?string $commentaire): static
    {
        $this->commentaire = $commentaire;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeImmutable
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeImmutable $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }
}
