export interface AbsenceExceptionnelle {
  id?: number;
  employeeId: number;
  employeeFullName?: string;
  dateDebut: string;
  dateFin: string;
  estDemiJourneeDebut: boolean;
  estDemiJourneeFin: boolean;
  motif: string;
  commentaire?: string;
  saisiPar?: string;
  estReaffectationAuto: boolean;
}

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  active: boolean;
  roleName?: string;
  serviceName?: string;
}
