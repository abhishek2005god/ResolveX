import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { CustomerDashboardData } from '../../types';
import { PlusCircle, Ticket, CheckCircle2, Clock, ArrowRight, HelpCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../../components/StatusBadge';
import { PriorityBadge } from '../../components/PriorityBadge';

export const CustomerDashboard: React.FC = () => {
  const [data, setData] = useState<CustomerDashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await dashboardService.getCustomerDashboard();
        setData(res);
      } catch (err) {
        console.error('Failed to load customer dashboard:', err);
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
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Customer Portal</h1>
          <p className="text-xs text-slate-500 mt-1">Submit support tickets, view status updates, and access help articles</p>
        </div>
        <Link
          to="/tickets/create"
          className="inline-flex items-center px-4 py-2.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm"
        >
          <PlusCircle className="w-4 h-4 mr-2" />
          Submit New Request
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Requests</span>
            <Ticket className="w-4 h-4 text-indigo-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-indigo-600">{data.totalTickets}</p>
          <span className="text-[11px] text-slate-400">All submitted tickets</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Active Requests</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-600">{data.openTickets}</p>
          <span className="text-[11px] text-slate-400">Being processed</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Resolved Requests</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{data.resolvedTickets}</p>
          <span className="text-[11px] text-slate-400">Completed support</span>
        </div>
      </div>

      {/* Knowledge base helper banner */}
      <div className="p-5 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-xl border border-indigo-100 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 bg-indigo-600 text-white rounded-lg">
            <HelpCircle className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-semibold text-slate-900">Need instant answers?</h4>
            <p className="text-xs text-slate-600 mt-0.5">Explore our searchable self-service Knowledge Base for troubleshooting guides.</p>
          </div>
        </div>
        <Link
          to="/knowledge-base"
          className="px-3.5 py-2 text-xs font-semibold text-indigo-700 bg-white border border-indigo-200 rounded-lg hover:bg-indigo-50 shrink-0"
        >
          Browse Articles
        </Link>
      </div>

      {/* Recent Requests Table */}
      <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-slate-900">Recent Service Requests</h3>
          <Link to="/tickets" className="text-xs font-semibold text-indigo-600 hover:text-indigo-800">
            View All My Requests
          </Link>
        </div>

        {data.recentTickets.length === 0 ? (
          <p className="text-xs text-slate-400 py-6 text-center">You have not submitted any requests yet</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
              <thead>
                <tr className="bg-slate-50">
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Code</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Title</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Category</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Priority</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Status</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Submitted</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.recentTickets.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-semibold text-indigo-600">{t.ticketCode}</td>
                    <td className="px-4 py-3 font-medium text-slate-900 truncate max-w-xs">{t.title}</td>
                    <td className="px-4 py-3 text-slate-600">{t.categoryName || 'General'}</td>
                    <td className="px-4 py-3">
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={t.status} />
                    </td>
                    <td className="px-4 py-3 text-slate-500">{new Date(t.createdAt).toLocaleDateString()}</td>
                    <td className="px-4 py-3">
                      <Link
                        to={`/tickets/${t.id}`}
                        className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center"
                      >
                        Track <ArrowRight className="w-3.5 h-3.5 ml-1" />
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
