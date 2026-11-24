import React, { useEffect, useState } from 'react';
import { userService } from '../../services/userService';
import { AuditLog, PagedResponse } from '../../types';
import { FileText, Shield, Clock, Search, ChevronLeft, ChevronRight } from 'lucide-react';

export const AuditLogsPage: React.FC = () => {
  const [logsData, setLogsData] = useState<PagedResponse<AuditLog>>({
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    userService
      .getAuditLogs(page, 20)
      .then(setLogsData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">System Audit & Compliance Logs</h1>
          <p className="text-xs text-slate-500 mt-1">
            Immutable system audit trails, authentication events, state transitions, and SLA actions
          </p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : logsData.content.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-400">No audit logs recorded</div>
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
                <thead>
                  <tr className="bg-slate-50">
                    <th className="px-4 py-3 font-semibold text-slate-700">Timestamp</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Actor</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Action</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Entity</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Change Summary</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Origin IP</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                  {logsData.content.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-slate-500 whitespace-nowrap">
                        {new Date(log.createdAt).toLocaleString()}
                      </td>
                      <td className="px-4 py-3 font-semibold text-slate-900">
                        {log.username || 'SYSTEM'}
                      </td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-0.5 rounded bg-slate-100 font-bold text-slate-800">
                          {log.action}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-indigo-600">
                        {log.entityType} #{log.entityId}
                      </td>
                      <td className="px-4 py-3 font-sans text-slate-700 max-w-md truncate">
                        {log.changeSummary}
                      </td>
                      <td className="px-4 py-3 text-slate-400">{log.ipAddress || '127.0.0.1'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between px-4 py-3 border-t border-slate-100 text-xs text-slate-600">
              <div>
                Page <span className="font-semibold text-slate-900">{logsData.page + 1}</span> of{' '}
                <span className="font-semibold text-slate-900">{Math.max(1, logsData.totalPages)}</span> ({logsData.totalElements} records)
              </div>
              <div className="flex items-center space-x-2">
                <button
                  disabled={logsData.page === 0}
                  onClick={() => setPage(page - 1)}
                  className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40 flex items-center"
                >
                  <ChevronLeft className="w-3.5 h-3.5 mr-1" /> Prev
                </button>
                <button
                  disabled={logsData.last}
                  onClick={() => setPage(page + 1)}
                  className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40 flex items-center"
                >
                  Next <ChevronRight className="w-3.5 h-3.5 ml-1" />
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
