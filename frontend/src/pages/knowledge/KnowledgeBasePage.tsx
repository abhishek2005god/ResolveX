import React, { useEffect, useState } from 'react';
import { knowledgeService } from '../../services/knowledgeService';
import { userService } from '../../services/userService';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { KnowledgeArticle, Category, PagedResponse } from '../../types';
import { Search, BookOpen, Plus, Eye, Tag, Calendar, User as UserIcon } from 'lucide-react';
import { Modal } from '../../components/Modal';

export const KnowledgeBasePage: React.FC = () => {
  const [articlesData, setArticlesData] = useState<PagedResponse<KnowledgeArticle> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState('');
  const [selectedArticle, setSelectedArticle] = useState<KnowledgeArticle | null>(null);
  const [articleModalOpen, setArticleModalOpen] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  // Create article form
  const [newTitle, setNewTitle] = useState('');
  const [newContent, setNewContent] = useState('');
  const [newSummary, setNewSummary] = useState('');
  const [newCategoryId, setNewCategoryId] = useState<number | ''>('');
  const [newTags, setNewTags] = useState('');

  const { user } = useAuth();
  const { showToast } = useNotifications();
  const canPublish = user?.role === 'ADMIN' || user?.role === 'MANAGER';

  const loadArticles = async () => {
    try {
      if (search.trim()) {
        const results = await knowledgeService.searchArticles(search.trim());
        setArticlesData({
          content: results,
          page: 0,
          size: results.length,
          totalElements: results.length,
          totalPages: 1,
          last: true,
        });
      } else {
        const res = await knowledgeService.getArticles(0, 12);
        setArticlesData(res);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    userService.getCategories().then(setCategories);
    loadArticles();
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadArticles();
  };

  const handleReadArticle = async (id: number) => {
    try {
      const art = await knowledgeService.getArticleById(id);
      setSelectedArticle(art);
      setArticleModalOpen(true);
    } catch (err) {
      showToast('Error', 'Failed to load article content', 'error');
    }
  };

  const handleCreateArticle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim() || !newContent.trim()) return;

    try {
      await knowledgeService.createArticle({
        title: newTitle.trim(),
        content: newContent.trim(),
        summary: newSummary.trim(),
        categoryId: newCategoryId ? Number(newCategoryId) : undefined,
        tags: newTags.trim(),
      });
      showToast('Article Published', 'New knowledge article is now accessible', 'success');
      setCreateModalOpen(false);
      setNewTitle('');
      setNewContent('');
      setNewSummary('');
      setNewTags('');
      loadArticles();
    } catch (err: any) {
      showToast('Error', err.response?.data?.message || 'Failed to publish article', 'error');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Enterprise Knowledge Base</h1>
          <p className="text-xs text-slate-500 mt-1">Self-service solutions, configuration runbooks, and SOPs</p>
        </div>
        {canPublish && (
          <button
            onClick={() => setCreateModalOpen(true)}
            className="inline-flex items-center px-4 py-2.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm"
          >
            <Plus className="w-4 h-4 mr-1.5" /> Publish New Article
          </button>
        )}
      </div>

      {/* Search Header */}
      <div className="p-6 bg-gradient-to-r from-indigo-900 to-slate-900 rounded-2xl text-white shadow-md">
        <h2 className="text-lg font-bold">How can we help resolve your issue today?</h2>
        <p className="text-xs text-indigo-200 mt-1 mb-4">Search our verified troubleshooting repository</p>
        <form onSubmit={handleSearchSubmit} className="relative max-w-xl">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search keywords (e.g. vpn, docker, sso, macbook)..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 text-xs bg-white text-slate-900 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </form>
      </div>

      {/* Articles Grid */}
      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : !articlesData || articlesData.content.length === 0 ? (
        <div className="p-12 bg-white rounded-xl border border-slate-200 text-center">
          <BookOpen className="w-10 h-10 text-slate-300 mx-auto mb-2" />
          <h3 className="text-sm font-semibold text-slate-900">No matching articles found</h3>
          <p className="text-xs text-slate-500 mt-1">Try another search keyword or clear the search query</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {articlesData.content.map((art) => (
            <div
              key={art.id}
              onClick={() => handleReadArticle(art.id)}
              className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:shadow-md hover:border-indigo-200 transition-all cursor-pointer flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between text-[11px] text-slate-400 mb-2">
                  <span className="font-semibold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                    {art.categoryName || 'General Guide'}
                  </span>
                  <span className="flex items-center">
                    <Eye className="w-3.5 h-3.5 mr-1" /> {art.viewsCount} reads
                  </span>
                </div>
                <h3 className="text-sm font-bold text-slate-900 line-clamp-2">{art.title}</h3>
                <p className="text-xs text-slate-600 mt-2 line-clamp-3 leading-relaxed">
                  {art.summary || art.content}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
                <span>By {art.authorName || 'Staff'}</span>
                <span>{new Date(art.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Read Article Modal */}
      <Modal
        isOpen={articleModalOpen}
        onClose={() => setArticleModalOpen(false)}
        title={selectedArticle?.title || 'Article'}
        maxWidth="max-w-2xl"
      >
        {selectedArticle && (
          <div className="space-y-4">
            <div className="flex items-center space-x-3 text-xs text-slate-500 pb-3 border-b border-slate-100">
              <span className="font-semibold text-indigo-600">{selectedArticle.categoryName}</span>
              <span>•</span>
              <span>Author: {selectedArticle.authorName}</span>
              <span>•</span>
              <span>Published: {new Date(selectedArticle.createdAt).toLocaleDateString()}</span>
            </div>

            <div className="text-sm text-slate-800 leading-relaxed whitespace-pre-wrap">
              {selectedArticle.content}
            </div>

            {selectedArticle.tags && (
              <div className="pt-3 border-t border-slate-100 flex flex-wrap gap-1.5">
                {selectedArticle.tags.split(',').map((tag) => (
                  <span key={tag} className="px-2 py-0.5 text-[10px] font-medium bg-slate-100 text-slate-600 rounded">
                    #{tag.trim()}
                  </span>
                ))}
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Publish Article Modal */}
      <Modal isOpen={createModalOpen} onClose={() => setCreateModalOpen(false)} title="Publish Knowledge Article" maxWidth="max-w-2xl">
        <form onSubmit={handleCreateArticle} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Article Title *
            </label>
            <input
              type="text"
              required
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              placeholder="e.g. How to connect to Corporate VPN on Windows 11"
              className="w-full px-3.5 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Category
              </label>
              <select
                value={newCategoryId}
                onChange={(e) => setNewCategoryId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              >
                <option value="">General</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Tags (Comma separated)
              </label>
              <input
                type="text"
                value={newTags}
                onChange={(e) => setNewTags(e.target.value)}
                placeholder="vpn, windows, remote"
                className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Short Summary
            </label>
            <input
              type="text"
              value={newSummary}
              onChange={(e) => setNewSummary(e.target.value)}
              placeholder="Brief 1-line overview of the resolution steps..."
              className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Full Content & Resolution Steps *
            </label>
            <textarea
              required
              rows={8}
              value={newContent}
              onChange={(e) => setNewContent(e.target.value)}
              placeholder="Detailed step-by-step resolution walkthrough..."
              className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 font-mono"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={() => setCreateModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Publish Article
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
