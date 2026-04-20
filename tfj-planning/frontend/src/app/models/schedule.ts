export interface Schedule {
  id: number;
  employeeId: number;
  employeeFullName: string;
  employeeEmail: string;
  roleName: string;
  serviceName: string;
  date: string;
  dayOfWeek: string;
  type: 'TFJ' | 'PERMANENCE';
  notes: string;
  isConfirmed: boolean;
  isSoloInGroup: boolean;
}
