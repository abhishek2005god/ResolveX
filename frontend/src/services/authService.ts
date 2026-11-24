import api from './api';
import { ApiResponse, AuthResponse, User } from '../types';

export const authService = {
  login: async (usernameOrEmail: string, password: string):Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', { usernameOrEmail, password });
    return res.data.data;
  },

  register: async (data: { username: string; email: string; password: string; fullName: string; role?: string; phone?: string; departmentId?: number }): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return res.data.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const res = await api.get<ApiResponse<User>>('/auth/me');
    return res.data.data;
  },

  updateProfile: async (data: { fullName: string; phone?: string; avatarUrl?: string }): Promise<User> => {
    const res = await api.put<ApiResponse<User>>('/auth/profile', data);
    return res.data.data;
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<string> => {
    const res = await api.post<ApiResponse<string>>('/auth/change-password', { currentPassword, newPassword });
    return res.data.data;
  },
};
