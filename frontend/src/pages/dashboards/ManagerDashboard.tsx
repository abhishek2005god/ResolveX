import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { ManagerDashboardData } from '../../types';
import { Ticket, Users, AlertTriangle, CheckCircle2, ShieldCheck, ArrowRight } from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Cell } from 'recharts';
import { Link } from 'react-router-dom';

const PRIORITY_COLORS: Record<string, string> = {
  LOW: '#94a3b8',
  MEDIUM: '#38bdf8',
  HIGH: '#f97316',
  CRITICAL: '#ef4444',
};

export const ManagerDashboard: React.FC = () => {
  const [data, setData] = useState<ManagerDashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await dashboardService.getManagerDashboard();
        setData(res);
      } catch (err) {
        console.error('Failed to load manager dashboard:', err);
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
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Team Operations Dashboard</h1>
          <p className="text-xs text-slate-500 mt-1">Supervise department workloads, ticket assignment, and SLA commitments</p>
        </div>
        <div className="flex items-center space-x-3">
          <Link
            to="/sla-config"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 rounded-lg hover:bg-slate-50"
          >
            Review SLA Escalations
          </Link>
          <Link
            to="/tickets"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700"
          >
            Manage Queue
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Team Active Load</span>
            <Ticket className="w-4 h-4 text-indigo-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-indigo-600">{data.teamActiveTickets}</p>
          <span className="text-[11px] text-slate-400">Currently in-flight</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Pending Assignment</span>
            <Users className="w-4 h-4 text-amber-500" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-600">{data.teamPendingTickets}</p>
          <span className="text-[11px] text-slate-400">Needs routing</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Resolved Tickets</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{data.teamResolvedTickets}</p>
          <span className="text-[11px] text-slate-400">Completed by team</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">SLA Escalations</span>
            <AlertTriangle className="w-4 h-4 text-red-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-red-600">{data.teamOverdueTickets}</p>
          <span className="text-[11px] font-medium text-emerald-600">
            {data.slaCompliancePercentage}% SLA compliance
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Priority Breakdown */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-semibold text-slate-900 mb-4">Team Tickets by Priority</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.ticketsByPriority}>
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {data.ticketsByPriority.map((entry) => (
                    <Cell key={entry.name} fill={PRIORITY_COLORS[entry.name] || '#6366f1'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Staff Workload Distribution */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-slate-900">Agent Capacity Allocation</h3>
            <span className="text-xs text-slate-500">{data.employeeWorkloads.length} agents</span>
          </div>

          <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
            {data.employeeWorkloads.map((emp) => {
              const maxLoad = 6;
              const pct = Math.min(100, Math.round((emp.activeTickets / maxLoad) * 100));
              const barColor = pct > 75 ? 'bg-red-500' : pct > 40 ? 'bg-amber-500' : 'bg-emerald-500';

              return (
                <div key={emp.userId} className="p-2.5 rounded-lg border border-slate-100 bg-slate-50/50">
                  <div className="flex items-center justify-between text-xs mb-1">
                    <span className="font-semibold text-slate-900">{emp.employeeName}</span>
                    <span className="text-slate-600 font-medium">{emp.activeTickets} active tickets</span>
                  </div>
                  <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
                    <div className={`h-full ${barColor} rounded-full transition-all duration-500`} style={{ width: `${pct}%` }}></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
