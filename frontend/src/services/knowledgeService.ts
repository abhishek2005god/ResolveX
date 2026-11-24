import api from './api';
import { ApiResponse, PagedResponse, KnowledgeArticle } from '../types';

export const knowledgeService = {
  getArticles: async (page = 0, size = 10): Promise<PagedResponse<KnowledgeArticle>> => {
    const res = await api.get<ApiResponse<PagedResponse<KnowledgeArticle>>>('/knowledge-base', { params: { page, size } });
    return res.data.data;
  },

  searchArticles: async (q: string): Promise<KnowledgeArticle[]> => {
    const res = await api.get<ApiResponse<KnowledgeArticle[]>>('/knowledge-base/search', { params: { q } });
    return res.data.data;
  },

  getArticleById: async (id: number): Promise<KnowledgeArticle> => {
    const res = await api.get<ApiResponse<KnowledgeArticle>>(`/knowledge-base/${id}`);
    return res.data.data;
  },

  createArticle: async (data: { title: string; content: string; summary?: string; categoryId?: number; tags?: string; isPublished?: boolean }): Promise<KnowledgeArticle> => {
    const res = await api.post<ApiResponse<KnowledgeArticle>>('/knowledge-base', data);
    return res.data.data;
  },

  updateArticle: async (id: number, data: { title: string; content: string; summary?: string; categoryId?: number; tags?: string; isPublished?: boolean }): Promise<KnowledgeArticle> => {
    const res = await api.put<ApiResponse<KnowledgeArticle>>(`/knowledge-base/${id}`, data);
    return res.data.data;
  },
};
