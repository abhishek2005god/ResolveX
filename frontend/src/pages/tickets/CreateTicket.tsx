import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketService } from '../../services/ticketService';
import { userService } from '../../services/userService';
import { aiService } from '../../services/aiService';
import { useNotifications } from '../../context/NotificationContext';
import { Category, Team, TicketPriority, AiAnalysisResult } from '../../types';
import { Sparkles, ArrowRight, Check, AlertCircle, BookOpen } from 'lucide-react';

export const CreateTicket: React.FC = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [teamId, setTeamId] = useState<number | ''>('');
  const [categories, setCategories] = useState<Category[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [submitting, setSubmitting] = useState(false);

  // AI Assistant state
  const [analyzingAi, setAnalyzingAi] = useState(false);
  const [aiResult, setAiResult] = useState<AiAnalysisResult | null>(null);

  const { showToast } = useNotifications();
  const navigate = useNavigate();

  useEffect(() => {
    userService.getCategories().then((cats) => {
      setCategories(cats);
      if (cats.length > 0) {
        setCategoryId(cats[0].id);
      }
    });
    userService.getTeams().then(setTeams);
  }, []);

  const handleAiAnalyze = async () => {
    if (!title.trim() && !description.trim()) {
      showToast('Need Details', 'Please provide a title or description for AI analysis', 'warning');
      return;
    }
    setAnalyzingAi(true);
    try {
      const result = await aiService.analyzeTicket(title, description);
      setAiResult(result);
      showToast('AI Analysis Complete', 'Suggested category and priority generated', 'info');
    } catch (err) {
      console.error('AI analysis error:', err);
      showToast('Analysis Warning', 'Unable to complete AI analysis, using manual inputs', 'warning');
    } finally {
      setAnalyzingAi(false);
    }
  };

  const handleApplyAiSuggestions = () => {
    if (!aiResult) return;
    if (aiResult.suggestedCategoryId) {
      setCategoryId(aiResult.suggestedCategoryId);
    }
    if (aiResult.suggestedPriority) {
      setPriority(aiResult.suggestedPriority);
    }
    showToast('Applied', 'AI suggestions accepted and applied to form', 'success');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoryId) {
      showToast('Category Required', 'Please select a ticket category', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const newTicket = await ticketService.createTicket({
        title: title.trim(),
        description: description.trim(),
        categoryId: Number(categoryId),
        priority,
        teamId: teamId ? Number(teamId) : undefined,
      });

      showToast('Ticket Created', `Created ${newTicket.ticketCode}`, 'success');
      navigate(`/tickets/${newTicket.id}`);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to create ticket';
      showToast('Error', msg, 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Create Support Ticket</h1>
        <p className="text-xs text-slate-500 mt-1">Submit an incident or service request into the ResolveX engine</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Ticket Form */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Ticket Title / Summary *
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Production API latency spike on auth endpoint"
                className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Detailed Description *
              </label>
              <textarea
                required
                rows={5}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Provide comprehensive diagnostic information, error logs, and impact..."
                className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Category *
                </label>
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Priority Level *
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as TicketPriority)}
                  className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="LOW">Low (Standard request)</option>
                  <option value="MEDIUM">Medium (General business defect)</option>
                  <option value="HIGH">High (Major degradation)</option>
                  <option value="CRITICAL">Critical (Total system outage)</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Target Team (Optional)
              </label>
              <select
                value={teamId}
                onChange={(e) => setTeamId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              >
                <option value="">Auto-route by Assignment Engine</option>
                {teams.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="pt-3 flex items-center justify-between border-t border-slate-100">
              <button
                type="button"
                onClick={handleAiAnalyze}
                disabled={analyzingAi}
                className="inline-flex items-center px-3.5 py-2 text-xs font-semibold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 rounded-lg transition-colors border border-indigo-200"
              >
                <Sparkles className="w-3.5 h-3.5 mr-1.5 text-indigo-600" />
                {analyzingAi ? 'Analyzing Ticket...' : 'Analyze with AI'}
              </button>

              <button
                type="submit"
                disabled={submitting}
                className="inline-flex items-center px-5 py-2.5 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm disabled:opacity-50"
              >
                {submitting ? 'Creating Ticket...' : 'Submit Ticket'}
                <ArrowRight className="w-4 h-4 ml-2" />
              </button>
            </div>
          </form>
        </div>

        {/* AI Assistant & Suggested Articles Panel */}
        <div className="space-y-4">
          <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
            <div className="flex items-center space-x-2 text-slate-900 mb-3">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <h3 className="text-sm font-bold">AI Triage Assistant</h3>
            </div>

            {!aiResult ? (
              <div className="text-xs text-slate-500 space-y-2">
                <p>
                  Click <span className="font-semibold text-indigo-600">"Analyze with AI"</span> to automatically classify
                  the issue, calculate optimal severity, and suggest immediate self-service articles.
                </p>
                <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                  <span className="font-semibold text-slate-700 block">Transparent Fallback</span>
                  Runs resilient local heuristic classification if external LLM credentials are not configured.
                </div>
              </div>
            ) : (
              <div className="space-y-3 animate-in fade-in">
                <div className="p-3 bg-indigo-50/60 rounded-lg border border-indigo-100 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-semibold text-indigo-600 uppercase tracking-wider">
                      {aiResult.isAiGenerated ? 'External Gemini Analysis' : 'Local Heuristic Engine'}
                    </span>
                    <span className="text-[10px] font-medium text-indigo-700">
                      {Math.round(aiResult.confidence * 100)}% confidence
                    </span>
                  </div>

                  <div className="text-xs">
                    <span className="text-slate-500 block">Suggested Category:</span>
                    <span className="font-bold text-slate-900">{aiResult.suggestedCategoryName}</span>
                  </div>

                  <div className="text-xs">
                    <span className="text-slate-500 block">Suggested Priority:</span>
                    <span className="font-bold text-slate-900">{aiResult.suggestedPriority}</span>
                  </div>

                  <div className="text-xs">
                    <span className="text-slate-500 block">Summary:</span>
                    <p className="text-slate-700 italic mt-0.5">{aiResult.summary}</p>
                  </div>

                  <button
                    type="button"
                    onClick={handleApplyAiSuggestions}
                    className="w-full mt-2 py-1.5 px-3 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-md shadow-xs flex items-center justify-center space-x-1"
                  >
                    <Check className="w-3.5 h-3.5" />
                    <span>Apply Suggestions</span>
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Recommended Knowledge Base Articles */}
          {aiResult && aiResult.recommendedArticles.length > 0 && (
            <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-sm">
              <div className="flex items-center space-x-2 text-slate-900 mb-3">
                <BookOpen className="w-4 h-4 text-indigo-600" />
                <h4 className="text-sm font-bold">Recommended Solutions</h4>
              </div>
              <div className="space-y-2">
                {aiResult.recommendedArticles.map((art) => (
                  <div key={art.id} className="p-2.5 rounded-lg border border-slate-100 hover:bg-slate-50">
                    <h5 className="text-xs font-semibold text-indigo-600">{art.title}</h5>
                    <p className="text-[11px] text-slate-500 mt-0.5 line-clamp-2">{art.summary}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
