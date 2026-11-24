import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { MainLayout } from './layouts/MainLayout';
import { AuthLayout } from './layouts/AuthLayout';
import { ProtectedRoute } from './components/ProtectedRoute';

// Pages
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { ForgotPassword } from './pages/auth/ForgotPassword';

import { AdminDashboard } from './pages/dashboards/AdminDashboard';
import { ManagerDashboard } from './pages/dashboards/ManagerDashboard';
import { EmployeeDashboard } from './pages/dashboards/EmployeeDashboard';
import { CustomerDashboard } from './pages/dashboards/CustomerDashboard';

import { TicketList } from './pages/tickets/TicketList';
import { CreateTicket } from './pages/tickets/CreateTicket';
import { TicketDetailView } from './pages/tickets/TicketDetail';

import { TeamManagement } from './pages/teams/TeamManagement';
import { UserManagement } from './pages/users/UserManagement';
import { SlaConfiguration } from './pages/sla/SlaConfiguration';
import { AnalyticsPage } from './pages/analytics/AnalyticsPage';
import { KnowledgeBasePage } from './pages/knowledge/KnowledgeBasePage';
import { NotificationsPage } from './pages/notifications/NotificationsPage';
import { AuditLogsPage } from './pages/audit/AuditLogsPage';
import { UserProfilePage } from './pages/profile/UserProfilePage';
import { SettingsPage } from './pages/settings/SettingsPage';

const RootRedirect: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) return null;
  if (!isAuthenticated || !user) return <Navigate to="/login" replace />;

  const defaultRoute =
    user.role === 'ADMIN'
      ? '/admin/dashboard'
      : user.role === 'MANAGER'
      ? '/manager/dashboard'
      : user.role === 'EMPLOYEE'
      ? '/employee/dashboard'
      : '/customer/dashboard';

  return <Navigate to={defaultRoute} replace />;
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            {/* Public Auth Routes */}
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
            </Route>

            {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route element={<MainLayout />}>
                <Route path="/" element={<RootRedirect />} />

                {/* Role Specific Dashboards */}
                <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                  <Route index element={<AdminDashboard />} />
                </Route>
                <Route path="/manager/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
                  <Route index element={<ManagerDashboard />} />
                </Route>
                <Route path="/employee/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']} />}>
                  <Route index element={<EmployeeDashboard />} />
                </Route>
                <Route path="/customer/dashboard" element={<CustomerDashboard />} />

                {/* Ticket Operations */}
                <Route path="/tickets" element={<TicketList />} />
                <Route path="/tickets/create" element={<CreateTicket />} />
                <Route path="/tickets/:id" element={<TicketDetailView />} />

                {/* Management Routes */}
                <Route path="/teams" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
                  <Route index element={<TeamManagement />} />
                </Route>
                <Route path="/users" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
                  <Route index element={<UserManagement />} />
                </Route>
                <Route path="/sla-config" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
                  <Route index element={<SlaConfiguration />} />
                </Route>
                <Route path="/analytics" element={<ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
                  <Route index element={<AnalyticsPage />} />
                </Route>
                <Route path="/audit-logs" element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                  <Route index element={<AuditLogsPage />} />
                </Route>

                {/* Common Utilities */}
                <Route path="/knowledge-base" element={<KnowledgeBasePage />} />
                <Route path="/notifications" element={<NotificationsPage />} />
                <Route path="/profile" element={<UserProfilePage />} />
                <Route path="/settings" element={<SettingsPage />} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
