import api from './api';
import { ApiResponse, AiAnalysisResult } from '../types';

export const aiService = {
  analyzeTicket: async (title: string, description: string): Promise<AiAnalysisResult> => {
    const res = await api.post<ApiResponse<AiAnalysisResult>>('/ai/analyze-ticket', { title, description });
    return res.data.data;
  },
};
