import React, { useEffect, useState, useCallback } from 'react';
import { ticketService, TicketFilters } from '../../services/ticketService';
import { userService } from '../../services/userService';
import { Ticket, TicketStatus, TicketPriority, Category, PagedResponse } from '../../types';
import { Link, useSearchParams } from 'react-router-dom';
import { StatusBadge } from '../../components/StatusBadge';
import { PriorityBadge } from '../../components/PriorityBadge';
import { Search, Filter, PlusCircle, ArrowUpDown, ChevronLeft, ChevronRight, AlertCircle } from 'lucide-react';

export const TicketList: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [ticketsData, setTicketsData] = useState<PagedResponse<Ticket> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters state from search params
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [status, setStatus] = useState<TicketStatus | ''>((searchParams.get('status') as TicketStatus) || '');
  const [priority, setPriority] = useState<TicketPriority | ''>((searchParams.get('priority') as TicketPriority) || '');
  const [categoryId, setCategoryId] = useState<string>(searchParams.get('categoryId') || '');
  const [page, setPage] = useState(0);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const filters: TicketFilters = {
        search: search.trim() || undefined,
        status: status ? (status as TicketStatus) : undefined,
        priority: priority ? (priority as TicketPriority) : undefined,
        categoryId: categoryId ? Number(categoryId) : undefined,
        page,
        size: 10,
        sortBy,
        sortDir,
      };
      const res = await ticketService.getTickets(filters);
      setTicketsData(res);
    } catch (err) {
      console.error('Failed to fetch tickets:', err);
    } finally {
      setLoading(false);
    }
  }, [search, status, priority, categoryId, page, sortBy, sortDir]);

  useEffect(() => {
    userService.getCategories().then(setCategories).catch(console.error);
  }, []);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  };

  const handleResetFilters = () => {
    setSearch('');
    setStatus('');
    setPriority('');
    setCategoryId('');
    setPage(0);
    setSearchParams({});
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Enterprise Ticket Center</h1>
          <p className="text-xs text-slate-500 mt-1">Search, triage, and track service requests across the enterprise</p>
        </div>
        <Link
          to="/tickets/create"
          className="inline-flex items-center px-4 py-2.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-sm"
        >
          <PlusCircle className="w-4 h-4 mr-2" />
          Create New Ticket
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm space-y-3">
        <form onSubmit={handleSearchSubmit} className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search by code, title, or keywords..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value as TicketStatus);
                setPage(0);
              }}
              className="px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="ASSIGNED">Assigned</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
              <option value="REOPENED">Reopened</option>
            </select>

            <select
              value={priority}
              onChange={(e) => {
                setPriority(e.target.value as TicketPriority);
                setPage(0);
              }}
              className="px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">All Priorities</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>

            <select
              value={categoryId}
              onChange={(e) => {
                setCategoryId(e.target.value);
                setPage(0);
              }}
              className="px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            <button
              type="submit"
              className="px-3.5 py-2 text-xs font-semibold text-white bg-slate-900 rounded-lg hover:bg-slate-800"
            >
              Apply Filter
            </button>
            <button
              type="button"
              onClick={handleResetFilters}
              className="px-3 py-2 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200"
            >
              Reset
            </button>
          </div>
        </form>
      </div>

      {/* Tickets Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : !ticketsData || ticketsData.content.length === 0 ? (
          <div className="p-12 text-center">
            <AlertCircle className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <h3 className="text-sm font-semibold text-slate-900">No tickets found</h3>
            <p className="text-xs text-slate-500 mt-1">Try adjusting your filters or search keywords</p>
          </div>
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-xs text-left">
                <thead>
                  <tr className="bg-slate-50">
                    <th
                      className="px-4 py-3 font-semibold text-slate-700 cursor-pointer select-none"
                      onClick={() => {
                        setSortBy('ticketCode');
                        setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
                      }}
                    >
                      <div className="flex items-center space-x-1">
                        <span>Code</span>
                        <ArrowUpDown className="w-3 h-3 text-slate-400" />
                      </div>
                    </th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Title</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Category</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Priority</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Status</th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Assignee</th>
                    <th
                      className="px-4 py-3 font-semibold text-slate-700 cursor-pointer select-none"
                      onClick={() => {
                        setSortBy('createdAt');
                        setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
                      }}
                    >
                      <div className="flex items-center space-x-1">
                        <span>Created</span>
                        <ArrowUpDown className="w-3 h-3 text-slate-400" />
                      </div>
                    </th>
                    <th className="px-4 py-3 font-semibold text-slate-700">Resolution Due</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {ticketsData.content.map((ticket) => (
                    <tr key={ticket.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3.5 font-mono font-semibold">
                        <Link to={`/tickets/${ticket.id}`} className="text-indigo-600 hover:text-indigo-800 hover:underline">
                          {ticket.ticketCode}
                        </Link>
                      </td>
                      <td className="px-4 py-3.5 max-w-sm truncate">
                        <Link to={`/tickets/${ticket.id}`} className="font-medium text-slate-900 hover:text-indigo-600 block truncate">
                          {ticket.title}
                        </Link>
                        <span className="text-[10px] text-slate-400">Created by {ticket.createdByName}</span>
                      </td>
                      <td className="px-4 py-3.5 text-slate-600">{ticket.categoryName || 'General'}</td>
                      <td className="px-4 py-3.5">
                        <PriorityBadge priority={ticket.priority} />
                      </td>
                      <td className="px-4 py-3.5">
                        <StatusBadge status={ticket.status} />
                      </td>
                      <td className="px-4 py-3.5 text-slate-700 font-medium">
                        {ticket.assignedToName || <span className="text-slate-400 italic">Unassigned</span>}
                      </td>
                      <td className="px-4 py-3.5 text-slate-500">
                        {new Date(ticket.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3.5">
                        {ticket.dueDate ? (
                          <span className={`text-[11px] ${ticket.isOverdue ? 'font-bold text-red-600' : 'text-slate-600'}`}>
                            {new Date(ticket.dueDate).toLocaleDateString()}
                          </span>
                        ) : (
                          <span className="text-slate-400">N/A</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            <div className="flex items-center justify-between px-4 py-3 border-t border-slate-100 text-xs text-slate-600">
              <div>
                Showing page <span className="font-semibold text-slate-900">{ticketsData.page + 1}</span> of{' '}
                <span className="font-semibold text-slate-900">{Math.max(1, ticketsData.totalPages)}</span> ({ticketsData.totalElements} total tickets)
              </div>
              <div className="flex items-center space-x-2">
                <button
                  disabled={ticketsData.page === 0}
                  onClick={() => setPage(page - 1)}
                  className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed flex items-center"
                >
                  <ChevronLeft className="w-3.5 h-3.5 mr-1" /> Prev
                </button>
                <button
                  disabled={ticketsData.last}
                  onClick={() => setPage(page + 1)}
                  className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed flex items-center"
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
