export interface User {
  id?: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
  createdAt?: string;
  employeeId?: number;
  employeeFullName?: string;
  fullName?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
  role: string;
  employeeId?: number;
  fullName?: string;
}

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
  totalUsers: number;
  adminUsers: number;
  gestionnaireUsers: number;
  operateurUsers: number;
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
