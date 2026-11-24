import React from 'react';
import { Outlet } from 'react-router-dom';
import { ToastContainer } from '../components/ToastContainer';

export const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-indigo-600 text-white font-bold text-xl shadow-lg shadow-indigo-600/40 mb-4">
          R
        </div>
        <h2 className="text-3xl font-extrabold text-white tracking-tight">ResolveX</h2>
        <p className="mt-1 text-sm text-indigo-200">Intelligent Enterprise Service Management</p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 shadow-2xl rounded-2xl sm:px-10 border border-slate-100">
          <Outlet />
        </div>
      </div>
      <ToastContainer />
    </div>
  );
};
