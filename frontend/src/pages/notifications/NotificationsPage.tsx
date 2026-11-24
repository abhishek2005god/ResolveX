import React, { useEffect, useState } from 'react';
import { notificationService } from '../../services/notificationService';
import { useNotifications } from '../../context/NotificationContext';
import { NotificationItem, PagedResponse } from '../../types';
import { Bell, Check, CheckCheck, Clock, ShieldAlert, MessageSquare } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const NotificationsPage: React.FC = () => {
  const [data, setData] = useState<PagedResponse<NotificationItem>>({
    content: [],
    page: 0,
    size: 15,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const [loading, setLoading] = useState(true);
  const [filterUnread, setFilterUnread] = useState(false);

  const { markAsRead, markAllAsRead, refreshNotifications } = useNotifications();
  const navigate = useNavigate();

  const loadNotifications = async (page = 0) => {
    try {
      const res = await notificationService.getNotifications(page, 15);
      setData(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications(0);
  }, []);

  const handleItemClick = async (n: NotificationItem) => {
    if (!n.isRead) {
      await markAsRead(n.id);
      setData((prev) => ({
        ...prev,
        content: prev.content.map((item) => (item.id === n.id ? { ...item, isRead: true } : item)),
      }));
    }
    if (n.referenceType === 'TICKET' && n.referenceId) {
      navigate(`/tickets/${n.referenceId}`);
    }
  };

  const handleMarkAll = async () => {
    await markAllAsRead();
    setData((prev) => ({
      ...prev,
      content: prev.content.map((item) => ({ ...item, isRead: true })),
    }));
  };

  const displayedList = filterUnread ? data.content.filter((n) => !n.isRead) : data.content;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">System Alerts & Notifications</h1>
          <p className="text-xs text-slate-500 mt-1">Real-time alerts on ticket assignments, status changes, and SLA warnings</p>
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setFilterUnread(!filterUnread)}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-colors ${
              filterUnread
                ? 'bg-indigo-600 text-white border-indigo-600'
                : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
            }`}
          >
            Unread Only
          </button>
          <button
            onClick={handleMarkAll}
            className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 flex items-center"
          >
            <CheckCheck className="w-3.5 h-3.5 mr-1.5" /> Mark All as Read
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : displayedList.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-400">
            <Bell className="w-8 h-8 text-slate-300 mx-auto mb-2" />
            No notifications to display
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {displayedList.map((n) => (
              <div
                key={n.id}
                onClick={() => handleItemClick(n)}
                className={`p-4 flex items-start space-x-4 hover:bg-slate-50/80 cursor-pointer transition-colors ${
                  !n.isRead ? 'bg-indigo-50/30' : ''
                }`}
              >
                <div
                  className={`p-2 rounded-lg shrink-0 ${
                    n.notificationType.includes('BREACHED') || n.notificationType.includes('ESCALATION')
                      ? 'bg-red-100 text-red-600'
                      : n.notificationType.includes('ASSIGNED')
                      ? 'bg-indigo-100 text-indigo-600'
                      : 'bg-blue-100 text-blue-600'
                  }`}
                >
                  {n.notificationType.includes('BREACHED') ? (
                    <ShieldAlert className="w-4 h-4" />
                  ) : n.notificationType.includes('COMMENT') ? (
                    <MessageSquare className="w-4 h-4" />
                  ) : (
                    <Bell className="w-4 h-4" />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <p className={`text-xs ${!n.isRead ? 'font-bold text-slate-900' : 'font-medium text-slate-700'}`}>
                      {n.title}
                    </p>
                    <span className="text-[10px] text-slate-400 flex items-center">
                      <Clock className="w-3 h-3 mr-1" />
                      {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })},{' '}
                      {new Date(n.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 mt-1 leading-relaxed">{n.message}</p>
                </div>

                {!n.isRead && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      markAsRead(n.id);
                    }}
                    className="p-1 text-slate-400 hover:text-indigo-600 rounded"
                    title="Mark as read"
                  >
                    <Check className="w-4 h-4" />
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
