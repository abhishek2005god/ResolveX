import React, { useEffect, useState } from 'react';
import { userService } from '../../services/userService';
import { useNotifications } from '../../context/NotificationContext';
import { Team, Department, TeamMember, User } from '../../types';
import { Layers, Plus, Users, UserPlus, Trash2, Shield } from 'lucide-react';
import { Modal } from '../../components/Modal';

export const TeamManagement: React.FC = () => {
  const [teams, setTeams] = useState<Team[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [members, setMembers] = useState<TeamMember[]>([]);
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  // Modals
  const [createTeamModalOpen, setCreateTeamModalOpen] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [teamDeptId, setTeamDeptId] = useState<number | ''>('');
  const [teamDesc, setTeamDesc] = useState('');

  const [addMemberModalOpen, setAddMemberModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | ''>('');
  const [isTeamLead, setIsTeamLead] = useState(false);

  const { showToast } = useNotifications();

  const loadData = async () => {
    try {
      const [tList, dList, uList] = await Promise.all([
        userService.getTeams(),
        userService.getDepartments(),
        userService.getAllUsers(),
      ]);
      setTeams(tList);
      setDepartments(dList);
      setAvailableUsers(uList.filter((u) => u.active));
      if (tList.length > 0 && !selectedTeam) {
        setSelectedTeam(tList[0]);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedTeam) {
      userService.getTeamMembers(selectedTeam.id).then(setMembers).catch(console.error);
    }
  }, [selectedTeam]);

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!teamName.trim()) return;

    try {
      const newTeam = await userService.createTeam(
        teamName.trim(),
        teamDeptId ? Number(teamDeptId) : undefined,
        teamDesc.trim()
      );
      showToast('Team Created', `Created ${newTeam.name}`, 'success');
      setCreateTeamModalOpen(false);
      setTeamName('');
      setTeamDesc('');
      loadData();
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Failed to create team', 'error');
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTeam || !selectedUserId) return;

    try {
      await userService.addTeamMember(selectedTeam.id, Number(selectedUserId), isTeamLead);
      showToast('Member Added', 'User added to team roster', 'success');
      setAddMemberModalOpen(false);
      setSelectedUserId('');
      setIsTeamLead(false);
      const updated = await userService.getTeamMembers(selectedTeam.id);
      setMembers(updated);
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Could not add member', 'error');
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!selectedTeam) return;
    try {
      await userService.removeTeamMember(selectedTeam.id, userId);
      showToast('Member Removed', 'User removed from team roster', 'info');
      setMembers(members.filter((m) => m.userId !== userId));
    } catch (err: any) {
      showToast('Error', 'Failed to remove member', 'error');
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Team Operations & Pods</h1>
          <p className="text-xs text-slate-500 mt-1">Configure support departments, technical pods, and agent memberships</p>
        </div>
        <button
          onClick={() => setCreateTeamModalOpen(true)}
          className="inline-flex items-center px-4 py-2.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm"
        >
          <Plus className="w-4 h-4 mr-1.5" /> New Team Pod
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Teams List */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-4 space-y-2">
          <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider px-2 mb-2">Technical Pods</h3>
          {teams.map((t) => (
            <div
              key={t.id}
              onClick={() => setSelectedTeam(t)}
              className={`p-3.5 rounded-xl cursor-pointer transition-all border ${
                selectedTeam?.id === t.id
                  ? 'bg-indigo-50/70 border-indigo-200 shadow-xs'
                  : 'bg-slate-50/50 border-slate-100 hover:bg-slate-100/60'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-900">{t.name}</span>
                <span className="text-[10px] font-semibold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                  {t.departmentName || 'General'}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1 line-clamp-1">{t.description || 'No description'}</p>
            </div>
          ))}
        </div>

        {/* Selected Team Members */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          {selectedTeam ? (
            <div className="space-y-6">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div>
                  <h2 className="text-lg font-bold text-slate-900">{selectedTeam.name}</h2>
                  <p className="text-xs text-slate-500 mt-0.5">{selectedTeam.description}</p>
                </div>
                <button
                  onClick={() => setAddMemberModalOpen(true)}
                  className="px-3.5 py-2 text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-lg border border-indigo-200 flex items-center"
                >
                  <UserPlus className="w-3.5 h-3.5 mr-1.5" /> Add Member
                </button>
              </div>

              <div>
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                  Roster & Pod Leads ({members.length})
                </h4>

                {members.length === 0 ? (
                  <p className="text-xs text-slate-400 py-6 text-center">No active members in this team pod</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-slate-100 text-xs text-left">
                      <thead>
                        <tr className="bg-slate-50">
                          <th className="px-4 py-2.5 font-semibold text-slate-700">Name</th>
                          <th className="px-4 py-2.5 font-semibold text-slate-700">Email</th>
                          <th className="px-4 py-2.5 font-semibold text-slate-700">Role</th>
                          <th className="px-4 py-2.5 font-semibold text-slate-700 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {members.map((m) => (
                          <tr key={m.userId} className="hover:bg-slate-50">
                            <td className="px-4 py-3 font-semibold text-slate-900">{m.fullName}</td>
                            <td className="px-4 py-3 text-slate-600">{m.email}</td>
                            <td className="px-4 py-3">
                              {m.isTeamLead ? (
                                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
                                  <Shield className="w-3 h-3 mr-1" /> Team Lead
                                </span>
                              ) : (
                                <span className="text-[11px] text-slate-500">Specialist</span>
                              )}
                            </td>
                            <td className="px-4 py-3 text-right">
                              <button
                                onClick={() => handleRemoveMember(m.userId)}
                                className="p-1 text-slate-400 hover:text-red-600 rounded transition-colors"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-12 text-center">Select a team to view and manage members</p>
          )}
        </div>
      </div>

      {/* Create Team Modal */}
      <Modal isOpen={createTeamModalOpen} onClose={() => setCreateTeamModalOpen(false)} title="Create Technical Pod">
        <form onSubmit={handleCreateTeam} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Team Pod Name *
            </label>
            <input
              type="text"
              required
              value={teamName}
              onChange={(e) => setTeamName(e.target.value)}
              placeholder="e.g. Identity & Security Ops"
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Department
            </label>
            <select
              value={teamDeptId}
              onChange={(e) => setTeamDeptId(e.target.value ? Number(e.target.value) : '')}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">No Department Assigned</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Description
            </label>
            <textarea
              rows={3}
              value={teamDesc}
              onChange={(e) => setTeamDesc(e.target.value)}
              placeholder="Responsibilities, scope, and SLA ownership..."
              className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setCreateTeamModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Save Team
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Member Modal */}
      <Modal isOpen={addMemberModalOpen} onClose={() => setAddMemberModalOpen(false)} title="Add Member to Pod">
        <form onSubmit={handleAddMember} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Select User *
            </label>
            <select
              required
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">Choose employee...</option>
              {availableUsers.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.fullName} ({u.role} - {u.email})
                </option>
              ))}
            </select>
          </div>

          <label className="flex items-center space-x-2 text-xs text-slate-700 cursor-pointer pt-1">
            <input
              type="checkbox"
              checked={isTeamLead}
              onChange={(e) => setIsTeamLead(e.target.checked)}
              className="rounded text-indigo-600 focus:ring-indigo-500"
            />
            <span className="font-semibold">Assign as Team Pod Lead</span>
          </label>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setAddMemberModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Add to Team
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
