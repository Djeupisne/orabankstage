export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  phone?: string;
  active: boolean;
  roleName?: string;
  serviceName?: string;
  roleId?: number;
  serviceId?: number;
  hierarchicalLevelId?: number;
  isSoloInGroup?: boolean;
}

export interface DashboardStats {
  totalEmployees: number;
  activeEmployees: number;
  inactiveEmployees: number;
  totalSchedules: number;
  tfjSchedules: number;
  permanenceSchedules: number;
}

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
