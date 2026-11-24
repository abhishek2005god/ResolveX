import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ticketService } from '../../services/ticketService';
import { userService } from '../../services/userService';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { TicketDetail, TicketStatus, User } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { PriorityBadge } from '../../components/PriorityBadge';
import { Modal } from '../../components/Modal';
import {
  Clock,
  User as UserIcon,
  MessageSquare,
  Paperclip,
  History,
  CheckCircle2,
  ArrowRight,
  Send,
  Upload,
  UserCheck,
} from 'lucide-react';

export const TicketDetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [isInternal, setIsInternal] = useState(false);
  const [employees, setEmployees] = useState<User[]>([]);

  // Modals
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<TicketStatus>('IN_PROGRESS');
  const [statusNotes, setStatusNotes] = useState('');

  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [selectedAssignee, setSelectedAssignee] = useState<number | ''>('');
  const [assignReason, setAssignReason] = useState('');

  const { user } = useAuth();
  const { showToast } = useNotifications();

  const isStaff = user?.role === 'ADMIN' || user?.role === 'MANAGER' || user?.role === 'EMPLOYEE';
  const isManagerOrAdmin = user?.role === 'ADMIN' || user?.role === 'MANAGER';

  const loadTicket = async () => {
    if (!id) return;
    try {
      const data = await ticketService.getTicketById(Number(id));
      setTicket(data);
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Failed to load ticket', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTicket();
    if (isStaff) {
      userService.getAllUsers().then((users) => {
        setEmployees(users.filter((u) => u.role === 'EMPLOYEE' && u.active));
      });
    }
  }, [id]);

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim() || !ticket) return;

    try {
      await ticketService.addComment(ticket.id, commentText.trim(), isInternal);
      setCommentText('');
      showToast('Comment Posted', 'Comment added to ticket thread', 'success');
      loadTicket();
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Could not post comment', 'error');
    }
  };

  const handleUpdateStatus = async () => {
    if (!ticket) return;
    try {
      await ticketService.updateStatus(ticket.id, selectedStatus, statusNotes);
      showToast('Status Updated', `Ticket status changed to ${selectedStatus}`, 'success');
      setStatusModalOpen(false);
      setStatusNotes('');
      loadTicket();
    } catch (err: any) {
      showToast('Transition Error', err.response?.data?.message || 'Invalid status transition', 'error');
    }
  };

  const handleAssignTicket = async () => {
    if (!ticket) return;
    try {
      await ticketService.assignTicket(
        ticket.id,
        selectedAssignee ? Number(selectedAssignee) : undefined,
        assignReason
      );
      showToast('Assignment Saved', 'Ticket assignment updated', 'success');
      setAssignModalOpen(false);
      setAssignReason('');
      loadTicket();
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Assignment failed', 'error');
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!ticket || !e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];

    try {
      await ticketService.uploadAttachment(ticket.id, file);
      showToast('Upload Successful', `Attached ${file.name}`, 'success');
      loadTicket();
    } catch (err: any) {
      showToast('Upload Failed', err.response?.data?.message || 'File upload error', 'error');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!ticket) return null;

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header bar */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-3 mb-1.5">
              <span className="font-mono text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-0.5 rounded border border-indigo-100">
                {ticket.ticketCode}
              </span>
              <StatusBadge status={ticket.status} />
              <PriorityBadge priority={ticket.priority} />
            </div>
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">{ticket.title}</h1>
            <p className="text-xs text-slate-500 mt-1">
              Opened by {ticket.createdByName} on {new Date(ticket.createdAt).toLocaleString()}
            </p>
          </div>

          <div className="flex items-center space-x-2">
            {isManagerOrAdmin && (
              <button
                onClick={() => setAssignModalOpen(true)}
                className="px-3 py-2 text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg flex items-center"
              >
                <UserCheck className="w-3.5 h-3.5 mr-1.5" /> Reassign
              </button>
            )}

            <button
              onClick={() => {
                setSelectedStatus(ticket.status);
                setStatusModalOpen(true);
              }}
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm flex items-center"
            >
              Update Status <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Conversation & Details */}
        <div className="lg:col-span-2 space-y-6">
          {/* Description Card */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
            <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Description</h3>
            <div className="text-sm text-slate-800 leading-relaxed whitespace-pre-wrap">{ticket.description}</div>

            {ticket.resolutionNotes && (
              <div className="mt-4 p-3.5 bg-emerald-50 border border-emerald-200 rounded-lg">
                <span className="text-xs font-bold text-emerald-900 block mb-1">Resolution Summary</span>
                <p className="text-xs text-emerald-800">{ticket.resolutionNotes}</p>
              </div>
            )}
          </div>

          {/* Comments Feed */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-6">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-sm font-bold text-slate-900 flex items-center">
                <MessageSquare className="w-4 h-4 mr-2 text-indigo-600" /> Activity & Comments ({ticket.comments.length})
              </h3>
            </div>

            <div className="space-y-4">
              {ticket.comments.length === 0 ? (
                <p className="text-xs text-slate-400 text-center py-4">No comments on this ticket yet</p>
              ) : (
                ticket.comments.map((comment) => (
                  <div
                    key={comment.id}
                    className={`p-4 rounded-xl border ${
                      comment.isInternal
                        ? 'bg-amber-50/60 border-amber-200'
                        : 'bg-slate-50 border-slate-100'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1.5">
                      <div className="flex items-center space-x-2">
                        <span className="text-xs font-bold text-slate-900">{comment.userFullName}</span>
                        <span className="text-[10px] text-slate-400">({comment.userRole})</span>
                        {comment.isInternal && (
                          <span className="text-[10px] font-semibold text-amber-800 bg-amber-100 px-1.5 py-0.5 rounded">
                            Internal Staff Note
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-slate-400">
                        {new Date(comment.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })},{' '}
                        {new Date(comment.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                    <p className="text-xs text-slate-700 leading-relaxed whitespace-pre-wrap">{comment.commentText}</p>
                  </div>
                ))
              )}
            </div>

            {/* Post Comment Form */}
            <form onSubmit={handleAddComment} className="pt-2 border-t border-slate-100 space-y-3">
              <textarea
                rows={3}
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder="Write a response or update..."
                className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              />

              <div className="flex items-center justify-between">
                {isStaff ? (
                  <label className="flex items-center space-x-2 text-xs text-slate-600 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={isInternal}
                      onChange={(e) => setIsInternal(e.target.checked)}
                      className="rounded text-indigo-600 focus:ring-indigo-500"
                    />
                    <span>Mark as internal staff note</span>
                  </label>
                ) : (
                  <div></div>
                )}

                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm flex items-center"
                >
                  <Send className="w-3.5 h-3.5 mr-1.5" /> Post Comment
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Sidebar Metadata & Attachments */}
        <div className="space-y-6">
          {/* Metadata Card */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5 space-y-3.5">
            <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">Ticket Attributes</h3>

            <div className="text-xs space-y-2.5">
              <div className="flex justify-between py-1 border-b border-slate-50">
                <span className="text-slate-500">Category</span>
                <span className="font-semibold text-slate-900">{ticket.categoryName || 'General'}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-50">
                <span className="text-slate-500">Assigned Agent</span>
                <span className="font-semibold text-slate-900">{ticket.assignedToName || 'Unassigned'}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-50">
                <span className="text-slate-500">Assigned Team</span>
                <span className="font-semibold text-slate-900">{ticket.teamName || 'Support Operations'}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-50">
                <span className="text-slate-500">SLA Resolution Due</span>
                <span className={`font-semibold ${ticket.isOverdue ? 'text-red-600 font-bold' : 'text-slate-900'}`}>
                  {ticket.dueDate ? new Date(ticket.dueDate).toLocaleString() : 'N/A'}
                </span>
              </div>
              {ticket.resolvedAt && (
                <div className="flex justify-between py-1 border-b border-slate-50">
                  <span className="text-slate-500">Resolved At</span>
                  <span className="font-semibold text-emerald-600">{new Date(ticket.resolvedAt).toLocaleString()}</span>
                </div>
              )}
            </div>
          </div>

          {/* Attachments Card */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center">
                <Paperclip className="w-3.5 h-3.5 mr-1.5 text-indigo-600" /> Files & Logs ({ticket.attachments.length})
              </h3>
              <label className="cursor-pointer text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center">
                <Upload className="w-3.5 h-3.5 mr-1" /> Add
                <input type="file" onChange={handleFileUpload} className="hidden" />
              </label>
            </div>

            <div className="space-y-2">
              {ticket.attachments.length === 0 ? (
                <p className="text-xs text-slate-400 py-2 text-center">No attached files</p>
              ) : (
                ticket.attachments.map((att) => (
                  <div key={att.id} className="p-2 rounded-lg border border-slate-100 bg-slate-50 flex items-center justify-between">
                    <span className="text-xs font-medium text-slate-800 truncate max-w-[180px]">{att.fileName}</span>
                    <span className="text-[10px] text-slate-400">
                      {Math.round((att.fileSize || 0) / 1024)} KB
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Audit History Timeline */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
            <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center mb-3">
              <History className="w-3.5 h-3.5 mr-1.5 text-indigo-600" /> Audit Timeline
            </h3>

            <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
              {ticket.history.map((h) => (
                <div key={h.id} className="text-xs border-l-2 border-indigo-200 pl-2.5 py-0.5">
                  <p className="text-slate-800 font-medium">
                    {h.action} {h.newValue ? `- ${h.newValue}` : ''}
                  </p>
                  <span className="text-[10px] text-slate-400">
                    {h.changedByName || 'System'} • {new Date(h.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Update Status Modal */}
      <Modal isOpen={statusModalOpen} onClose={() => setStatusModalOpen(false)} title="Update Ticket Status">
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Select Target Status
            </label>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value as TicketStatus)}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="OPEN">Open</option>
              <option value="ASSIGNED">Assigned</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
              <option value="REOPENED">Reopened</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Transition / Resolution Notes
            </label>
            <textarea
              rows={3}
              value={statusNotes}
              onChange={(e) => setStatusNotes(e.target.value)}
              placeholder="State the root cause or reason for this status change..."
              className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              onClick={() => setStatusModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              onClick={handleUpdateStatus}
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Save Status Transition
            </button>
          </div>
        </div>
      </Modal>

      {/* Reassign Ticket Modal */}
      <Modal isOpen={assignModalOpen} onClose={() => setAssignModalOpen(false)} title="Assign / Reassign Ticket">
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Support Specialist
            </label>
            <select
              value={selectedAssignee}
              onChange={(e) => setSelectedAssignee(e.target.value ? Number(e.target.value) : '')}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">Auto-Assign (Lowest Active Workload)</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.fullName} ({emp.email})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Assignment Reason
            </label>
            <input
              type="text"
              value={assignReason}
              onChange={(e) => setAssignReason(e.target.value)}
              placeholder="e.g. Workload re-balancing or specialized domain expertise"
              className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              onClick={() => setAssignModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              onClick={handleAssignTicket}
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Confirm Assignment
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
