import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { LogIn, ArrowRight, ShieldCheck } from 'lucide-react';

export const Login: React.FC = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { login } = useAuth();
  const { showToast } = useNotifications();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const user = await login(usernameOrEmail, password);
      showToast('Welcome back!', `Signed in as ${user.fullName}`, 'success');

      // Navigate based on role
      const defaultRoute =
        user.role === 'ADMIN'
          ? '/admin/dashboard'
          : user.role === 'MANAGER'
          ? '/manager/dashboard'
          : user.role === 'EMPLOYEE'
          ? '/employee/dashboard'
          : '/customer/dashboard';

      navigate(defaultRoute);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Invalid username or password';
      setError(msg);
      showToast('Login Failed', msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (user: string, pass: string) => {
    setUsernameOrEmail(user);
    setPassword(pass);
  };

  return (
    <div>
      <div className="mb-6">
        <h3 className="text-xl font-bold text-slate-900">Sign in to your account</h3>
        <p className="text-xs text-slate-500 mt-1">Enter your enterprise credentials or use demo roles below</p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
            Username or Email
          </label>
          <input
            type="text"
            required
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            placeholder="admin, tech.john, customer.alice"
            className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
          />
        </div>

        <div>
          <div className="flex items-center justify-between mb-1">
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider">
              Password
            </label>
            <Link to="/forgot-password" className="text-xs text-indigo-600 hover:text-indigo-800 font-medium">
              Forgot password?
            </Link>
          </div>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all disabled:opacity-50"
        >
          {loading ? (
            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          ) : (
            <>
              Sign In
              <ArrowRight className="w-4 h-4 ml-2" />
            </>
          )}
        </button>
      </form>

      {/* Demo Credentials Quick-Fill Section */}
      <div className="mt-6 pt-5 border-t border-slate-100">
        <div className="flex items-center space-x-1.5 text-xs font-semibold text-slate-600 mb-2">
          <ShieldCheck className="w-4 h-4 text-indigo-600" />
          <span>Quick Demo Logins (Password: Password@123)</span>
        </div>
        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={() => handleQuickLogin('admin', 'Password@123')}
            className="p-2 text-left rounded-lg border border-purple-200 bg-purple-50/50 hover:bg-purple-100/60 transition-colors"
          >
            <p className="text-xs font-bold text-purple-900">Admin</p>
            <p className="text-[10px] text-purple-700 truncate">admin</p>
          </button>
          <button
            type="button"
            onClick={() => handleQuickLogin('manager.it', 'Password@123')}
            className="p-2 text-left rounded-lg border border-blue-200 bg-blue-50/50 hover:bg-blue-100/60 transition-colors"
          >
            <p className="text-xs font-bold text-blue-900">IT Manager</p>
            <p className="text-[10px] text-blue-700 truncate">manager.it</p>
          </button>
          <button
            type="button"
            onClick={() => handleQuickLogin('tech.john', 'Password@123')}
            className="p-2 text-left rounded-lg border border-emerald-200 bg-emerald-50/50 hover:bg-emerald-100/60 transition-colors"
          >
            <p className="text-xs font-bold text-emerald-900">Support Agent</p>
            <p className="text-[10px] text-emerald-700 truncate">tech.john</p>
          </button>
          <button
            type="button"
            onClick={() => handleQuickLogin('customer.alice', 'Password@123')}
            className="p-2 text-left rounded-lg border border-amber-200 bg-amber-50/50 hover:bg-amber-100/60 transition-colors"
          >
            <p className="text-xs font-bold text-amber-900">Customer</p>
            <p className="text-[10px] text-amber-700 truncate">customer.alice</p>
          </button>
        </div>
      </div>

      <div className="mt-6 text-center">
        <p className="text-xs text-slate-500">
          Don't have an account?{' '}
          <Link to="/register" className="font-semibold text-indigo-600 hover:text-indigo-800">
            Sign up now
          </Link>
        </p>
      </div>
    </div>
  );
};
