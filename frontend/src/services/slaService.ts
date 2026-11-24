import api from './api';
import { ApiResponse, SlaPolicy, SlaEscalation } from '../types';

export const slaService = {
  getPolicies: async (): Promise<SlaPolicy[]> => {
    const res = await api.get<ApiResponse<SlaPolicy[]>>('/sla/policies');
    return res.data.data;
  },

  updatePolicy: async (id: number, data: Partial<SlaPolicy>): Promise<SlaPolicy> => {
    const res = await api.put<ApiResponse<SlaPolicy>>(`/sla/policies/${id}`, data);
    return res.data.data;
  },

  getEscalations: async (): Promise<SlaEscalation[]> => {
    const res = await api.get<ApiResponse<SlaEscalation[]>>('/sla/escalations');
    return res.data.data;
  },
};
