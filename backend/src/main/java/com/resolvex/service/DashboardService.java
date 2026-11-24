package com.resolvex.service;

import com.resolvex.dto.DashboardDtos;
import com.resolvex.dto.TicketDtos;
import com.resolvex.entity.Ticket;
import com.resolvex.entity.User;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import com.resolvex.repository.TicketRepository;
import com.resolvex.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    public DashboardService(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.AdminDashboardDto getAdminDashboard() {
        DashboardDtos.AdminDashboardDto dto = new DashboardDtos.AdminDashboardDto();

        dto.setTotalUsers(userRepository.count());
        long totalTickets = ticketRepository.count();
        dto.setTotalTickets(totalTickets);

        long openCount = ticketRepository.countByStatus(TicketStatus.OPEN);
        long assignedCount = ticketRepository.countByStatus(TicketStatus.ASSIGNED);
        dto.setOpenTickets(openCount + assignedCount);
        dto.setInProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS) + ticketRepository.countByStatus(TicketStatus.ON_HOLD));

        long resolvedCount = ticketRepository.countByStatus(TicketStatus.RESOLVED) + ticketRepository.countByStatus(TicketStatus.CLOSED);
        dto.setResolvedTickets(resolvedCount);

        long overdueCount = ticketRepository.countByIsOverdueTrue();
        dto.setOverdueTickets(overdueCount);

        double compliance = totalTickets > 0 ? Math.max(0.0, Math.min(100.0, ((double) (totalTickets - overdueCount) / totalTickets) * 100)) : 100.0;
        dto.setSlaComplianceRate(Math.round(compliance * 10.0) / 10.0);

        // Status Chart Data
        List<DashboardDtos.ChartDataPoint> statusPoints = new ArrayList<>();
        for (TicketStatus status : TicketStatus.values()) {
            long count = ticketRepository.countByStatus(status);
            statusPoints.add(new DashboardDtos.ChartDataPoint(status.name(), count));
        }
        dto.setTicketsByStatus(statusPoints);

        // Priority Chart Data
        List<DashboardDtos.ChartDataPoint> priorityPoints = new ArrayList<>();
        for (TicketPriority priority : TicketPriority.values()) {
            long count = ticketRepository.countByPriority(priority);
            priorityPoints.add(new DashboardDtos.ChartDataPoint(priority.name(), count));
        }
        dto.setTicketsByPriority(priorityPoints);

        // Tickets Trend (Last 7 days)
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        List<Object[]> trendRows = ticketRepository.countTicketsTrendLastDays(sevenDaysAgo);
        List<DashboardDtos.ChartDataPoint> trendPoints = new ArrayList<>();
        for (Object[] row : trendRows) {
            String dateLabel = String.valueOf(row[0]);
            Number count = (Number) row[1];
            trendPoints.add(new DashboardDtos.ChartDataPoint(dateLabel, count));
        }
        if (trendPoints.isEmpty()) {
            trendPoints.add(new DashboardDtos.ChartDataPoint("Today", totalTickets));
        }
        dto.setTicketsTrend(trendPoints);

        // Workload distribution
        List<User> employees = userRepository.findAllActiveEmployees();
        List<DashboardDtos.WorkloadItem> workloads = employees.stream().map(emp -> {
            long active = ticketRepository.countActiveTicketsByAssignedTo(emp.getId());
            return new DashboardDtos.WorkloadItem(emp.getId(), emp.getFullName(), emp.getEmail(), active, 0);
        }).collect(Collectors.toList());
        dto.setTeamWorkloads(workloads);

        return dto;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.ManagerDashboardDto getManagerDashboard(Long managerUserId) {
        DashboardDtos.ManagerDashboardDto dto = new DashboardDtos.ManagerDashboardDto();

        User manager = userRepository.findById(managerUserId).orElse(null);
        Long deptId = manager != null && manager.getDepartment() != null ? manager.getDepartment().getId() : null;

        List<User> teamEmployees = deptId != null ? userRepository.findByDepartmentId(deptId) : userRepository.findAllActiveEmployees();

        long activeCount = 0;
        List<DashboardDtos.WorkloadItem> workloads = new ArrayList<>();
        for (User emp : teamEmployees) {
            if ("EMPLOYEE".equalsIgnoreCase(emp.getRole().getName().name())) {
                long active = ticketRepository.countActiveTicketsByAssignedTo(emp.getId());
                activeCount += active;
                workloads.add(new DashboardDtos.WorkloadItem(emp.getId(), emp.getFullName(), emp.getEmail(), active, 0));
            }
        }
        dto.setEmployeeWorkloads(workloads);
        dto.setTeamActiveTickets(activeCount);
        dto.setTeamPendingTickets(ticketRepository.countByStatus(TicketStatus.OPEN) + ticketRepository.countByStatus(TicketStatus.ASSIGNED));
        dto.setTeamResolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED));

        long overdue = ticketRepository.countByIsOverdueTrue();
        dto.setTeamOverdueTickets(overdue);

        long total = activeCount + dto.getTeamResolvedTickets();
        double compliance = total > 0 ? Math.max(0.0, Math.min(100.0, ((double) (total - overdue) / total) * 100)) : 100.0;
        dto.setSlaCompliancePercentage(Math.round(compliance * 10.0) / 10.0);

        List<DashboardDtos.ChartDataPoint> priorityPoints = new ArrayList<>();
        for (TicketPriority priority : TicketPriority.values()) {
            priorityPoints.add(new DashboardDtos.ChartDataPoint(priority.name(), ticketRepository.countByPriority(priority)));
        }
        dto.setTicketsByPriority(priorityPoints);

        List<DashboardDtos.ChartDataPoint> statusPoints = new ArrayList<>();
        for (TicketStatus status : TicketStatus.values()) {
            statusPoints.add(new DashboardDtos.ChartDataPoint(status.name(), ticketRepository.countByStatus(status)));
        }
        dto.setTicketsByStatus(statusPoints);

        return dto;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.EmployeeDashboardDto getEmployeeDashboard(Long employeeUserId) {
        DashboardDtos.EmployeeDashboardDto dto = new DashboardDtos.EmployeeDashboardDto();

        long assigned = ticketRepository.countActiveTicketsByAssignedTo(employeeUserId);
        dto.setMyAssignedTickets(assigned);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> page = ticketRepository.findByAssignedToId(employeeUserId, pageable);
        List<Ticket> myTickets = page.getContent();

        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED).count();
        long overdue = myTickets.stream().filter(t -> Boolean.TRUE.equals(t.getIsOverdue())).count();

        dto.setMyInProgressTickets(inProgress);
        dto.setMyResolvedTickets(resolved);
        dto.setMyOverdueTickets(overdue);

        dto.setRecentTickets(myTickets.stream().limit(5).map(ticketService::toDto).collect(Collectors.toList()));

        // Upcoming deadlines: non-resolved tickets sorted by due date ascending
        List<TicketDtos.TicketDto> deadlines = myTickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED && t.getDueDate() != null)
                .sorted(Comparator.comparing(Ticket::getDueDate))
                .limit(5)
                .map(ticketService::toDto)
                .collect(Collectors.toList());
        dto.setUpcomingDeadlines(deadlines);

        return dto;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.CustomerDashboardDto getCustomerDashboard(Long customerUserId) {
        DashboardDtos.CustomerDashboardDto dto = new DashboardDtos.CustomerDashboardDto();

        long total = ticketRepository.countByCreatedById(customerUserId);
        long active = ticketRepository.countActiveTicketsByCreatedById(customerUserId);
        long resolved = ticketRepository.countResolvedTicketsByCreatedById(customerUserId);

        dto.setTotalTickets(total);
        dto.setOpenTickets(active);
        dto.setResolvedTickets(resolved);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> page = ticketRepository.findByCreatedById(customerUserId, pageable);
        dto.setRecentTickets(page.getContent().stream().map(ticketService::toDto).collect(Collectors.toList()));

        return dto;
    }
}
