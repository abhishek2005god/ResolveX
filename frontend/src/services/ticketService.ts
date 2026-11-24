import api from './api';
import { ApiResponse, PagedResponse, Ticket, TicketDetail, TicketComment, TicketAttachment, TicketPriority, TicketStatus } from '../types';

export interface TicketFilters {
  search?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  categoryId?: number;
  teamId?: number;
  assignedToId?: number;
  isOverdue?: boolean;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

export const ticketService = {
  getTickets: async (filters: TicketFilters = {}): Promise<PagedResponse<Ticket>> => {
    const res = await api.get<ApiResponse<PagedResponse<Ticket>>>('/tickets', { params: filters });
    return res.data.data;
  },

  getTicketById: async (id: number): Promise<TicketDetail> => {
    const res = await api.get<ApiResponse<TicketDetail>>(`/tickets/${id}`);
    return res.data.data;
  },

  createTicket: async (data: { title: string; description: string; categoryId: number; priority?: TicketPriority; teamId?: number; assignedToId?: number }): Promise<Ticket> => {
    const res = await api.post<ApiResponse<Ticket>>('/tickets', data);
    return res.data.data;
  },

  updateTicket: async (id: number, data: { title?: string; description?: string; categoryId?: number; priority?: TicketPriority; teamId?: number }): Promise<Ticket> => {
    const res = await api.put<ApiResponse<Ticket>>(`/tickets/${id}`, data);
    return res.data.data;
  },

  updateStatus: async (id: number, status: TicketStatus, notes?: string): Promise<Ticket> => {
    const res = await api.patch<ApiResponse<Ticket>>(`/tickets/${id}/status`, { status, notes });
    return res.data.data;
  },

  assignTicket: async (id: number, assignedToId?: number, reason?: string): Promise<Ticket> => {
    const res = await api.post<ApiResponse<Ticket>>(`/tickets/${id}/assign`, { assignedToId, reason });
    return res.data.data;
  },

  addComment: async (ticketId: number, commentText: string, isInternal: boolean = false): Promise<TicketComment> => {
    const res = await api.post<ApiResponse<TicketComment>>(`/tickets/${ticketId}/comments`, { commentText, isInternal });
    return res.data.data;
  },

  uploadAttachment: async (ticketId: number, file: File): Promise<TicketAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await api.post<ApiResponse<TicketAttachment>>(`/tickets/${ticketId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },
};
