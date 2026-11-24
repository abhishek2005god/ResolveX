import React, { useEffect, useState } from 'react';
import { slaService } from '../../services/slaService';
import { useNotifications } from '../../context/NotificationContext';
import { SlaPolicy, SlaEscalation } from '../../types';
import { ShieldAlert, Edit2, AlertCircle, CheckCircle } from 'lucide-react';
import { Modal } from '../../components/Modal';
import { PriorityBadge } from '../../components/PriorityBadge';

export const SlaConfiguration: React.FC = () => {
  const [policies, setPolicies] = useState<SlaPolicy[]>([]);
  const [escalations, setEscalations] = useState<SlaEscalation[]>([]);
  const [selectedPolicy, setSelectedPolicy] = useState<SlaPolicy | null>(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  // Form state
  const [maxResponse, setMaxResponse] = useState<number>(4);
  const [maxResolution, setMaxResolution] = useState<number>(24);
  const [threshold, setThreshold] = useState<number>(20);

  const { showToast } = useNotifications();

  const loadData = async () => {
    try {
      const [pList, eList] = await Promise.all([slaService.getPolicies(), slaService.getEscalations()]);
      setPolicies(pList);
      setEscalations(eList);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleOpenEdit = (p: SlaPolicy) => {
    setSelectedPolicy(p);
    setMaxResponse(p.maxResponseHours);
    setMaxResolution(p.maxResolutionHours);
    setThreshold(p.escalationThresholdHours);
    setEditModalOpen(true);
  };

  const handleSavePolicy = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPolicy) return;

    try {
      const updated = await slaService.updatePolicy(selectedPolicy.id, {
        name: selectedPolicy.name,
        maxResponseHours: Number(maxResponse),
        maxResolutionHours: Number(maxResolution),
        escalationThresholdHours: Number(threshold),
      });

      showToast('SLA Policy Updated', `Updated ${updated.name}`, 'success');
      setEditModalOpen(false);
      setPolicies(policies.map((p) => (p.id === updated.id ? updated : p)));
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Failed to update policy', 'error');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Service Level Agreements (SLA)</h1>
        <p className="text-xs text-slate-500 mt-1">
          Configure response deadlines, resolution thresholds, and automatic breach escalation triggers
        </p>
      </div>

      {/* SLA Policies Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h3 className="text-sm font-bold text-slate-900">Configured Priority Policies</h3>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
            <thead>
              <tr className="bg-slate-50">
                <th className="px-4 py-2.5 font-semibold text-slate-700">Policy Name</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Priority Tier</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Max Response</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Max Resolution</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Escalation Threshold</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700">Status</th>
                <th className="px-4 py-2.5 font-semibold text-slate-700 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {policies.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3.5 font-semibold text-slate-900">{p.name}</td>
                  <td className="px-4 py-3.5">
                    <PriorityBadge priority={p.priority} />
                  </td>
                  <td className="px-4 py-3.5 font-medium text-slate-700">{p.maxResponseHours} hours</td>
                  <td className="px-4 py-3.5 font-bold text-indigo-600">{p.maxResolutionHours} hours</td>
                  <td className="px-4 py-3.5 text-slate-600">{p.escalationThresholdHours} hours</td>
                  <td className="px-4 py-3.5">
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                      Active
                    </span>
                  </td>
                  <td className="px-4 py-3.5 text-right">
                    <button
                      onClick={() => handleOpenEdit(p)}
                      className="px-2.5 py-1 text-xs font-semibold text-indigo-600 hover:text-indigo-800 border border-indigo-200 rounded-md hover:bg-indigo-50 inline-flex items-center"
                    >
                      <Edit2 className="w-3 h-3 mr-1" /> Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Escalation Log Section */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="w-4 h-4 text-red-600" />
            <h3 className="text-sm font-bold text-slate-900">Active SLA Breach Escalations ({escalations.length})</h3>
          </div>
          <span className="text-xs text-slate-400">Triggered by scheduled SLA monitoring engine</span>
        </div>

        {escalations.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            <CheckCircle className="w-8 h-8 text-emerald-500 mx-auto mb-1" />
            Zero active SLA escalations. All resolution deadlines are currently compliant.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-100 text-xs text-left">
              <thead>
                <tr className="bg-slate-50">
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Ticket</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Reason</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Escalated To</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Level</th>
                  <th className="px-4 py-2.5 font-semibold text-slate-700">Escalated At</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {escalations.map((esc) => (
                  <tr key={esc.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-semibold text-indigo-600">
                      {esc.ticketCode}: {esc.ticketTitle}
                    </td>
                    <td className="px-4 py-3 text-red-600 font-medium">{esc.reason}</td>
                    <td className="px-4 py-3 font-medium text-slate-800">{esc.escalatedToName}</td>
                    <td className="px-4 py-3 font-bold text-slate-900">Level {esc.escalationLevel}</td>
                    <td className="px-4 py-3 text-slate-500">{new Date(esc.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit SLA Policy Modal */}
      <Modal isOpen={editModalOpen} onClose={() => setEditModalOpen(false)} title={`Edit ${selectedPolicy?.name}`}>
        <form onSubmit={handleSavePolicy} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Max Response Time (Hours)
            </label>
            <input
              type="number"
              min={1}
              required
              value={maxResponse}
              onChange={(e) => setMaxResponse(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Max Resolution Time (Hours)
            </label>
            <input
              type="number"
              min={1}
              required
              value={maxResolution}
              onChange={(e) => setMaxResolution(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Warning / Escalation Threshold (Hours)
            </label>
            <input
              type="number"
              min={1}
              required
              value={threshold}
              onChange={(e) => setThreshold(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setEditModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Save Policy
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
