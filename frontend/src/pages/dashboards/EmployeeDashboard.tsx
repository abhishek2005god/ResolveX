import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { EmployeeDashboardData } from '../../types';
import { Ticket, CheckCircle2, Clock, AlertTriangle, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../../components/StatusBadge';
import { PriorityBadge } from '../../components/PriorityBadge';

export const EmployeeDashboard: React.FC = () => {
  const [data, setData] = useState<EmployeeDashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await dashboardService.getEmployeeDashboard();
        setData(res);
      } catch (err) {
        console.error('Failed to load employee dashboard:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Agent Workstation</h1>
          <p className="text-xs text-slate-500 mt-1">Your active tasks, upcoming deadlines, and ticket queue</p>
        </div>
        <Link
          to="/tickets"
          className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm"
        >
          View All Tickets
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Assigned to Me</span>
            <Ticket className="w-4 h-4 text-indigo-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-indigo-600">{data.myAssignedTickets}</p>
          <span className="text-[11px] text-slate-400">Total in queue</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">In Progress</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-600">{data.myInProgressTickets}</p>
          <span className="text-[11px] text-slate-400">Under active work</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Resolved</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{data.myResolvedTickets}</p>
          <span className="text-[11px] text-slate-400">Completed tickets</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Approaching / Overdue</span>
            <AlertTriangle className="w-4 h-4 text-red-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-red-600">{data.myOverdueTickets}</p>
          <span className="text-[11px] text-slate-400">Attention required</span>
        </div>
      </div>

      {/* Deadlines Section */}
      <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-900 mb-3">Upcoming SLA Deadlines</h3>
        {data.upcomingDeadlines.length === 0 ? (
          <p className="text-xs text-slate-400 py-4 text-center">No upcoming critical deadlines</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
              <thead>
                <tr className="bg-slate-50">
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Code</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Title</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Priority</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Status</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Resolution Due</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.upcomingDeadlines.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-semibold text-indigo-600">{t.ticketCode}</td>
                    <td className="px-4 py-3 font-medium text-slate-900 truncate max-w-xs">{t.title}</td>
                    <td className="px-4 py-3">
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={t.status} />
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {t.dueDate ? new Date(t.dueDate).toLocaleString() : 'N/A'}
                    </td>
                    <td className="px-4 py-3">
                      <Link
                        to={`/tickets/${t.id}`}
                        className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center"
                      >
                        Open <ArrowRight className="w-3.5 h-3.5 ml-1" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
