import api from './api';
import { ApiResponse, User, Category, Department, Team, TeamMember, AuditLog, PagedResponse } from '../types';

export const userService = {
  getAllUsers: async (): Promise<User[]> => {
    const res = await api.get<ApiResponse<User[]>>('/users');
    return res.data.data;
  },

  getUserById: async (id: number): Promise<User> => {
    const res = await api.get<ApiResponse<User>>(`/users/${id}`);
    return res.data.data;
  },

  toggleUserActive: async (id: number): Promise<User> => {
    const res = await api.patch<ApiResponse<User>>(`/users/${id}/toggle-active`);
    return res.data.data;
  },

  getCategories: async (): Promise<Category[]> => {
    const res = await api.get<ApiResponse<Category[]>>('/categories');
    return res.data.data;
  },

  getDepartments: async (): Promise<Department[]> => {
    const res = await api.get<ApiResponse<Department[]>>('/departments');
    return res.data.data;
  },

  getTeams: async (): Promise<Team[]> => {
    const res = await api.get<ApiResponse<Team[]>>('/teams');
    return res.data.data;
  },

  createTeam: async (name: string, departmentId?: number, description?: string): Promise<Team> => {
    const res = await api.post<ApiResponse<Team>>('/teams', { name, departmentId, description });
    return res.data.data;
  },

  getTeamMembers: async (teamId: number): Promise<TeamMember[]> => {
    const res = await api.get<ApiResponse<TeamMember[]>>(`/teams/${teamId}/members`);
    return res.data.data;
  },

  addTeamMember: async (teamId: number, userId: number, isTeamLead: boolean = false): Promise<void> => {
    await api.post(`/teams/${teamId}/members`, { userId, isTeamLead });
  },

  removeTeamMember: async (teamId: number, userId: number): Promise<void> => {
    await api.delete(`/teams/${teamId}/members/${userId}`);
  },

  getAuditLogs: async (page = 0, size = 20): Promise<PagedResponse<AuditLog>> => {
    const res = await api.get<ApiResponse<PagedResponse<AuditLog>>>('/audit-logs', { params: { page, size } });
    return res.data.data;
  },
};
