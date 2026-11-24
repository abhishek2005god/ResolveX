import React, { useState } from 'react';
import { useNotifications } from '../../context/NotificationContext';
import { Settings, Shield, Bell, Database, Cpu, Sparkles, Check } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const [emailAlerts, setEmailAlerts] = useState(true);
  const [autoAssignment, setAutoAssignment] = useState(true);
  const [slaEscalations, setSlaEscalations] = useState(true);
  const { showToast } = useNotifications();

  const handleSave = () => {
    showToast('Preferences Saved', 'System configuration saved successfully', 'success');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">System & Platform Preferences</h1>
        <p className="text-xs text-slate-500 mt-1">Configure global automation rules, telemetry dispatch, and notifications</p>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-6">
        <div>
          <h3 className="text-sm font-bold text-slate-900 mb-1 flex items-center">
            <Cpu className="w-4 h-4 mr-2 text-indigo-600" /> Workload & Automation Engine
          </h3>
          <p className="text-xs text-slate-500">Autonomous ticket dispatch and rule-based assignment</p>

          <div className="mt-4 space-y-3">
            <label className="flex items-center justify-between p-3 rounded-lg border border-slate-100 bg-slate-50/50 cursor-pointer">
              <div>
                <span className="text-xs font-semibold text-slate-900 block">Automated Workload Routing</span>
                <span className="text-[11px] text-slate-500">
                  Automatically select the eligible support agent with lowest active ticket count upon creation.
                </span>
              </div>
              <input
                type="checkbox"
                checked={autoAssignment}
                onChange={(e) => setAutoAssignment(e.target.checked)}
                className="rounded text-indigo-600 focus:ring-indigo-500 h-4 w-4"
              />
            </label>

            <label className="flex items-center justify-between p-3 rounded-lg border border-slate-100 bg-slate-50/50 cursor-pointer">
              <div>
                <span className="text-xs font-semibold text-slate-900 block">Automated SLA Breach Escalation</span>
                <span className="text-[11px] text-slate-500">
                  Run scheduled 60s monitor and alert department managers when resolution deadlines elapse.
                </span>
              </div>
              <input
                type="checkbox"
                checked={slaEscalations}
                onChange={(e) => setSlaEscalations(e.target.checked)}
                className="rounded text-indigo-600 focus:ring-indigo-500 h-4 w-4"
              />
            </label>
          </div>
        </div>

        <div className="pt-4 border-t border-slate-100">
          <h3 className="text-sm font-bold text-slate-900 mb-1 flex items-center">
            <Bell className="w-4 h-4 mr-2 text-indigo-600" /> Notifications & WebSocket Broadcast
          </h3>
          <p className="text-xs text-slate-500">Real-time STOMP messaging and browser alerts</p>

          <div className="mt-4 space-y-3">
            <label className="flex items-center justify-between p-3 rounded-lg border border-slate-100 bg-slate-50/50 cursor-pointer">
              <div>
                <span className="text-xs font-semibold text-slate-900 block">Real-time Push Notifications</span>
                <span className="text-[11px] text-slate-500">
                  Display toast banners on assignment changes, new comments, and status transitions.
                </span>
              </div>
              <input
                type="checkbox"
                checked={emailAlerts}
                onChange={(e) => setEmailAlerts(e.target.checked)}
                className="rounded text-indigo-600 focus:ring-indigo-500 h-4 w-4"
              />
            </label>
          </div>
        </div>

        <div className="pt-4 border-t border-slate-100">
          <h3 className="text-sm font-bold text-slate-900 mb-1 flex items-center">
            <Sparkles className="w-4 h-4 mr-2 text-indigo-600" /> AI Classification Integration
          </h3>
          <p className="text-xs text-slate-500">Optional Google Gemini API configuration</p>

          <div className="mt-4 p-4 rounded-lg bg-indigo-50/50 border border-indigo-100 space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-indigo-900">
              <span>Status: Active Heuristic Fallback Engine</span>
              <span className="px-2 py-0.5 rounded bg-indigo-200/60 text-indigo-800 text-[10px]">Embedded</span>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              ResolveX contains a self-contained heuristic classification engine that parses ticket text, extracts high-frequency enterprise keywords, determines priority severity, and recommends relevant KB articles without requiring external cloud tokens. To connect an external Google Gemini API key, pass <code className="bg-white px-1.5 py-0.5 rounded border border-indigo-200">GEMINI_API_KEY</code> as an environment variable.
            </p>
          </div>
        </div>

        <div className="pt-4 flex justify-end">
          <button
            onClick={handleSave}
            className="px-5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm"
          >
            Save All Preferences
          </button>
        </div>
      </div>
    </div>
  );
};
