import api from './api';
import { ApiResponse, AdminDashboardData, ManagerDashboardData, EmployeeDashboardData, CustomerDashboardData } from '../types';

export const dashboardService = {
  getAdminDashboard: async (): Promise<AdminDashboardData> => {
    const res = await api.get<ApiResponse<AdminDashboardData>>('/dashboard/admin');
    return res.data.data;
  },

  getManagerDashboard: async (): Promise<ManagerDashboardData> => {
    const res = await api.get<ApiResponse<ManagerDashboardData>>('/dashboard/manager');
    return res.data.data;
  },

  getEmployeeDashboard: async (): Promise<EmployeeDashboardData> => {
    const res = await api.get<ApiResponse<EmployeeDashboardData>>('/dashboard/employee');
    return res.data.data;
  },

  getCustomerDashboard: async (): Promise<CustomerDashboardData> => {
    const res = await api.get<ApiResponse<CustomerDashboardData>>('/dashboard/customer');
    return res.data.data;
  },
};
