export type RoleName = 'ADMIN' | 'MANAGER' | 'EMPLOYEE' | 'CUSTOMER';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type TicketStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'ON_HOLD' | 'RESOLVED' | 'CLOSED' | 'REOPENED';
export type NotificationType = 'TICKET_CREATED' | 'TICKET_ASSIGNED' | 'STATUS_CHANGED' | 'COMMENT_ADDED' | 'SLA_WARNING' | 'SLA_BREACHED' | 'ESCALATION';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: RoleName;
  departmentId?: number;
  departmentName?: string;
  phone?: string;
  avatarUrl?: string;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  defaultPriority: TicketPriority;
  active: boolean;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
}

export interface Team {
  id: number;
  name: string;
  description?: string;
  departmentId?: number;
  departmentName?: string;
}

export interface TeamMember {
  id: number;
  userId: number;
  username: string;
  fullName: string;
  email: string;
  isTeamLead: boolean;
}

export interface TicketComment {
  id: number;
  ticketId: number;
  userId: number;
  username: string;
  userFullName: string;
  userRole: string;
  commentText: string;
  isInternal: boolean;
  createdAt: string;
}

export interface TicketHistory {
  id: number;
  ticketId: number;
  changedById?: number;
  changedByName?: string;
  action: string;
  fieldName?: string;
  oldValue?: string;
  newValue?: string;
  createdAt: string;
}

export interface TicketAttachment {
  id: number;
  ticketId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedByName: string;
  createdAt: string;
}

export interface Ticket {
  id: number;
  ticketCode: string;
  title: string;
  description: string;
  categoryId: number;
  categoryName: string;
  priority: TicketPriority;
  status: TicketStatus;
  createdById: number;
  createdByName: string;
  createdByEmail: string;
  assignedToId?: number;
  assignedToName?: string;
  teamId?: number;
  teamName?: string;
  dueDate?: string;
  resolvedAt?: string;
  closedAt?: string;
  resolutionNotes?: string;
  isOverdue?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TicketDetail extends Ticket {
  comments: TicketComment[];
  history: TicketHistory[];
  attachments: TicketAttachment[];
}

export interface SlaPolicy {
  id: number;
  name: string;
  priority: TicketPriority;
  maxResponseHours: number;
  maxResolutionHours: number;
  escalationThresholdHours: number;
  isActive: boolean;
}

export interface SlaEscalation {
  id: number;
  ticketId: number;
  ticketCode: string;
  ticketTitle: string;
  escalationLevel: number;
  escalatedToName: string;
  reason: string;
  resolved: boolean;
  createdAt: string;
}

export interface NotificationItem {
  id: number;
  userId: number;
  title: string;
  message: string;
  notificationType: NotificationType;
  isRead: boolean;
  referenceId?: number;
  referenceType?: string;
  createdAt: string;
}

export interface KnowledgeArticle {
  id: number;
  title: string;
  content: string;
  summary?: string;
  categoryId?: number;
  categoryName?: string;
  authorId?: number;
  authorName?: string;
  isPublished: boolean;
  viewsCount: number;
  tags?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface AuditLog {
  id: number;
  userId?: number;
  username: string;
  action: string;
  entityType: string;
  entityId?: number;
  changeSummary?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface ChartDataPoint {
  name: string;
  value: number;
  label?: string;
}

export interface WorkloadItem {
  userId: number;
  employeeName: string;
  email: string;
  activeTickets: number;
  resolvedTickets: number;
}

export interface AdminDashboardData {
  totalUsers: number;
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  overdueTickets: number;
  slaComplianceRate: number;
  ticketsByStatus: ChartDataPoint[];
  ticketsByPriority: ChartDataPoint[];
  ticketsTrend: ChartDataPoint[];
  teamWorkloads: WorkloadItem[];
}

export interface ManagerDashboardData {
  teamActiveTickets: number;
  teamPendingTickets: number;
  teamResolvedTickets: number;
  teamOverdueTickets: number;
  slaCompliancePercentage: number;
  employeeWorkloads: WorkloadItem[];
  ticketsByPriority: ChartDataPoint[];
  ticketsByStatus: ChartDataPoint[];
}

export interface EmployeeDashboardData {
  myAssignedTickets: number;
  myInProgressTickets: number;
  myResolvedTickets: number;
  myOverdueTickets: number;
  recentTickets: Ticket[];
  upcomingDeadlines: Ticket[];
}

export interface CustomerDashboardData {
  totalTickets: number;
  openTickets: number;
  resolvedTickets: number;
  recentTickets: Ticket[];
}

export interface AiAnalysisResult {
  suggestedCategoryId?: number;
  suggestedCategoryName?: string;
  suggestedPriority: TicketPriority;
  summary: string;
  confidence: number;
  isAiGenerated: boolean;
  recommendedArticles: KnowledgeArticle[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
