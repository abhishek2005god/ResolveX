import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { AdminDashboardData } from '../../types';
import {
  Users,
  Ticket,
  AlertTriangle,
  CheckCircle2,
  Clock,
  TrendingUp,
  ShieldCheck,
  ArrowUpRight,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from 'recharts';
import { Link } from 'react-router-dom';

const STATUS_COLORS: Record<string, string> = {
  OPEN: '#3b82f6',
  ASSIGNED: '#6366f1',
  IN_PROGRESS: '#f59e0b',
  ON_HOLD: '#ea580c',
  RESOLVED: '#10b981',
  CLOSED: '#64748b',
  REOPENED: '#ef4444',
};

const PRIORITY_COLORS: Record<string, string> = {
  LOW: '#94a3b8',
  MEDIUM: '#38bdf8',
  HIGH: '#f97316',
  CRITICAL: '#ef4444',
};

export const AdminDashboard: React.FC = () => {
  const [data, setData] = useState<AdminDashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await dashboardService.getAdminDashboard();
        setData(res);
      } catch (err) {
        console.error('Failed to load admin dashboard:', err);
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
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Executive Dashboard</h1>
          <p className="text-xs text-slate-500 mt-1">
            Global service management telemetry, SLA thresholds, and team distribution
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Link
            to="/tickets/create"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm transition-all"
          >
            Create Ticket
          </Link>
          <Link
            to="/analytics"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition-all"
          >
            Deep Analytics
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Users</span>
            <Users className="w-4 h-4 text-purple-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-slate-900">{data.totalUsers}</p>
          <span className="text-[11px] text-slate-400">Active accounts</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Tickets</span>
            <Ticket className="w-4 h-4 text-blue-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-slate-900">{data.totalTickets}</p>
          <span className="text-[11px] text-slate-400">Recorded tickets</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Open Backlog</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-600">{data.openTickets}</p>
          <span className="text-[11px] text-slate-400">Pending resolution</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">In Progress</span>
            <TrendingUp className="w-4 h-4 text-indigo-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-indigo-600">{data.inProgressTickets}</p>
          <span className="text-[11px] text-slate-400">Actively worked</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">Resolved</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{data.resolvedTickets}</p>
          <span className="text-[11px] text-slate-400">Successfully closed</span>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">SLA Overdue</span>
            <AlertTriangle className="w-4 h-4 text-red-600" />
          </div>
          <p className="mt-2 text-2xl font-bold text-red-600">{data.overdueTickets}</p>
          <span className="text-[11px] font-medium text-emerald-600">
            {data.slaComplianceRate}% compliance
          </span>
        </div>
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Status Distribution Pie */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-semibold text-slate-900 mb-4">Tickets by Status</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data.ticketsByStatus}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  innerRadius={45}
                  paddingAngle={3}
                >
                  {data.ticketsByStatus.map((entry) => (
                    <Cell key={entry.name} fill={STATUS_COLORS[entry.name] || '#94a3b8'} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-2 grid grid-cols-2 gap-2 text-[11px]">
            {data.ticketsByStatus.map((s) => (
              <div key={s.name} className="flex items-center justify-between text-slate-600">
                <span className="flex items-center">
                  <span
                    className="w-2.5 h-2.5 rounded-full mr-1.5"
                    style={{ backgroundColor: STATUS_COLORS[s.name] || '#94a3b8' }}
                  ></span>
                  {s.name}
                </span>
                <span className="font-semibold text-slate-900">{s.value}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Priority Breakdown Bar */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-semibold text-slate-900 mb-4">Tickets by Priority</h3>
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
          <div className="mt-2 grid grid-cols-2 gap-2 text-[11px]">
            {data.ticketsByPriority.map((p) => (
              <div key={p.name} className="flex items-center justify-between text-slate-600">
                <span className="flex items-center">
                  <span
                    className="w-2.5 h-2.5 rounded-full mr-1.5"
                    style={{ backgroundColor: PRIORITY_COLORS[p.name] || '#6366f1' }}
                  ></span>
                  {p.name}
                </span>
                <span className="font-semibold text-slate-900">{p.value}</span>
              </div>
            ))}
          </div>
        </div>

        {/* 7-Day Trend Area Chart */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-semibold text-slate-900 mb-4">Ticket Inflow Trend</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data.ticketsTrend}>
                <defs>
                  <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Area type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#trendGradient)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
          <p className="mt-3 text-xs text-slate-500 text-center">Inflow trajectory across recent days</p>
        </div>
      </div>

      {/* Team Workloads Table */}
      <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-sm font-semibold text-slate-900">Support Specialist Workload</h3>
            <p className="text-xs text-slate-500">Live ticket counts assigned to technical personnel</p>
          </div>
          <Link to="/teams" className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center">
            Manage Teams <ArrowUpRight className="w-3.5 h-3.5 ml-1" />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
            <thead>
              <tr className="bg-slate-50">
                <th className="px-4 py-2.5 font-semibold text-slate-700">Specialist</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Email</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Active Load</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Workload Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.teamWorkloads.map((emp) => {
                const loadStatus =
                  emp.activeTickets >= 4
                    ? 'High Load'
                    : emp.activeTickets >= 2
                    ? 'Moderate'
                    : 'Optimal';
                const statusBadgeColor =
                  emp.activeTickets >= 4
                    ? 'bg-red-50 text-red-700 border-red-200'
                    : emp.activeTickets >= 2
                    ? 'bg-amber-50 text-amber-700 border-amber-200'
                    : 'bg-emerald-50 text-emerald-700 border-emerald-200';

                return (
                  <tr key={emp.userId} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-semibold text-slate-900">{emp.employeeName}</td>
                    <td className="px-4 py-3 text-slate-600">{emp.email}</td>
                    <td className="px-4 py-3 font-bold text-slate-900">{emp.activeTickets} tickets</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${statusBadgeColor}`}>
                        {loadStatus}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
