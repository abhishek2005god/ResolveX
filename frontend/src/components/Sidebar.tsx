import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Ticket,
  PlusCircle,
  Users,
  ShieldAlert,
  BarChart3,
  BookOpen,
  Bell,
  FileText,
  Settings,
  Layers,
  Sparkles,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const role = user?.role;

  const dashboardPath =
    role === 'ADMIN'
      ? '/admin/dashboard'
      : role === 'MANAGER'
      ? '/manager/dashboard'
      : role === 'EMPLOYEE'
      ? '/employee/dashboard'
      : '/customer/dashboard';

  const navItemClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
      isActive
        ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    }`;

  return (
    <aside className="w-64 bg-white border-r border-slate-200 flex flex-col shrink-0 min-h-screen">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-slate-200">
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold shadow-md shadow-indigo-600/30">
            R
          </div>
          <div>
            <span className="text-lg font-bold text-slate-900 tracking-tight">ResolveX</span>
            <span className="block text-[10px] font-semibold text-indigo-600 uppercase tracking-wider">Enterprise</span>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 px-4 py-5 space-y-6 overflow-y-auto">
        <div>
          <span className="px-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Workspace</span>
          <nav className="mt-2 space-y-1">
            <NavLink to={dashboardPath} className={navItemClass}>
              <LayoutDashboard className="w-4 h-4 mr-3" />
              Dashboard
            </NavLink>
            <NavLink to="/tickets" className={navItemClass}>
              <Ticket className="w-4 h-4 mr-3" />
              Tickets
            </NavLink>
            <NavLink to="/tickets/create" className={navItemClass}>
              <PlusCircle className="w-4 h-4 mr-3" />
              New Ticket
            </NavLink>
            <NavLink to="/knowledge-base" className={navItemClass}>
              <BookOpen className="w-4 h-4 mr-3" />
              Knowledge Base
            </NavLink>
          </nav>
        </div>

        {/* Role-Specific Management Section */}
        {(role === 'ADMIN' || role === 'MANAGER') && (
          <div>
            <span className="px-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Management</span>
            <nav className="mt-2 space-y-1">
              <NavLink to="/teams" className={navItemClass}>
                <Layers className="w-4 h-4 mr-3" />
                Team Operations
              </NavLink>
              <NavLink to="/users" className={navItemClass}>
                <Users className="w-4 h-4 mr-3" />
                User Directory
              </NavLink>
              <NavLink to="/sla-config" className={navItemClass}>
                <ShieldAlert className="w-4 h-4 mr-3" />
                SLA Policies
              </NavLink>
              <NavLink to="/analytics" className={navItemClass}>
                <BarChart3 className="w-4 h-4 mr-3" />
                Analytics & KPI
              </NavLink>
            </nav>
          </div>
        )}

        {/* Administration Section */}
        {role === 'ADMIN' && (
          <div>
            <span className="px-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Governance</span>
            <nav className="mt-2 space-y-1">
              <NavLink to="/audit-logs" className={navItemClass}>
                <FileText className="w-4 h-4 mr-3" />
                Audit Logs
              </NavLink>
            </nav>
          </div>
        )}

        {/* Personal Links */}
        <div>
          <span className="px-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">System</span>
          <nav className="mt-2 space-y-1">
            <NavLink to="/notifications" className={navItemClass}>
              <Bell className="w-4 h-4 mr-3" />
              Notifications
            </NavLink>
            <NavLink to="/settings" className={navItemClass}>
              <Settings className="w-4 h-4 mr-3" />
              Settings
            </NavLink>
          </nav>
        </div>
      </div>

      {/* AI Assistant Banner */}
      <div className="p-4 m-4 rounded-xl bg-gradient-to-br from-indigo-50 to-purple-50 border border-indigo-100">
        <div className="flex items-center space-x-2 text-indigo-700">
          <Sparkles className="w-4 h-4 text-indigo-600" />
          <span className="text-xs font-bold">AI Triage Active</span>
        </div>
        <p className="mt-1 text-[11px] text-slate-600 leading-normal">
          Automated routing and instant category classification enabled.
        </p>
      </div>
    </aside>
  );
};
