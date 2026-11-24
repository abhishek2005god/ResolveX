package com.resolvex.dto;

import java.util.List;
import java.util.Map;

public class DashboardDtos {

    public static class ChartDataPoint {
        private String name;
        private Number value;
        private String label;

        public ChartDataPoint() {}
        public ChartDataPoint(String name, Number value) {
            this.name = name;
            this.value = value;
        }
        public ChartDataPoint(String name, Number value, String label) {
            this.name = name;
            this.value = value;
            this.label = label;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Number getValue() { return value; }
        public void setValue(Number value) { this.value = value; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class WorkloadItem {
        private Long userId;
        private String employeeName;
        private String email;
        private long activeTickets;
        private long resolvedTickets;

        public WorkloadItem() {}
        public WorkloadItem(Long userId, String employeeName, String email, long activeTickets, long resolvedTickets) {
            this.userId = userId;
            this.employeeName = employeeName;
            this.email = email;
            this.activeTickets = activeTickets;
            this.resolvedTickets = resolvedTickets;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public long getActiveTickets() { return activeTickets; }
        public void setActiveTickets(long activeTickets) { this.activeTickets = activeTickets; }

        public long getResolvedTickets() { return resolvedTickets; }
        public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }
    }

    public static class AdminDashboardDto {
        private long totalUsers;
        private long totalTickets;
        private long openTickets;
        private long inProgressTickets;
        private long resolvedTickets;
        private long overdueTickets;
        private double slaComplianceRate;
        private List<ChartDataPoint> ticketsByStatus;
        private List<ChartDataPoint> ticketsByPriority;
        private List<ChartDataPoint> ticketsTrend;
        private List<WorkloadItem> teamWorkloads;

        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getTotalTickets() { return totalTickets; }
        public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

        public long getOpenTickets() { return openTickets; }
        public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

        public long getInProgressTickets() { return inProgressTickets; }
        public void setInProgressTickets(long inProgressTickets) { this.inProgressTickets = inProgressTickets; }

        public long getResolvedTickets() { return resolvedTickets; }
        public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }

        public long getOverdueTickets() { return overdueTickets; }
        public void setOverdueTickets(long overdueTickets) { this.overdueTickets = overdueTickets; }

        public double getSlaComplianceRate() { return slaComplianceRate; }
        public void setSlaComplianceRate(double slaComplianceRate) { this.slaComplianceRate = slaComplianceRate; }

        public List<ChartDataPoint> getTicketsByStatus() { return ticketsByStatus; }
        public void setTicketsByStatus(List<ChartDataPoint> ticketsByStatus) { this.ticketsByStatus = ticketsByStatus; }

        public List<ChartDataPoint> getTicketsByPriority() { return ticketsByPriority; }
        public void setTicketsByPriority(List<ChartDataPoint> ticketsByPriority) { this.ticketsByPriority = ticketsByPriority; }

        public List<ChartDataPoint> getTicketsTrend() { return ticketsTrend; }
        public void setTicketsTrend(List<ChartDataPoint> ticketsTrend) { this.ticketsTrend = ticketsTrend; }

        public List<WorkloadItem> getTeamWorkloads() { return teamWorkloads; }
        public void setTeamWorkloads(List<WorkloadItem> teamWorkloads) { this.teamWorkloads = teamWorkloads; }
    }

    public static class ManagerDashboardDto {
        private long teamActiveTickets;
        private long teamPendingTickets;
        private long teamResolvedTickets;
        private long teamOverdueTickets;
        private double slaCompliancePercentage;
        private List<WorkloadItem> employeeWorkloads;
        private List<ChartDataPoint> ticketsByPriority;
        private List<ChartDataPoint> ticketsByStatus;

        public long getTeamActiveTickets() { return teamActiveTickets; }
        public void setTeamActiveTickets(long teamActiveTickets) { this.teamActiveTickets = teamActiveTickets; }

        public long getTeamPendingTickets() { return teamPendingTickets; }
        public void setTeamPendingTickets(long teamPendingTickets) { this.teamPendingTickets = teamPendingTickets; }

        public long getTeamResolvedTickets() { return teamResolvedTickets; }
        public void setTeamResolvedTickets(long teamResolvedTickets) { this.teamResolvedTickets = teamResolvedTickets; }

        public long getTeamOverdueTickets() { return teamOverdueTickets; }
        public void setTeamOverdueTickets(long teamOverdueTickets) { this.teamOverdueTickets = teamOverdueTickets; }

        public double getSlaCompliancePercentage() { return slaCompliancePercentage; }
        public void setSlaCompliancePercentage(double slaCompliancePercentage) { this.slaCompliancePercentage = slaCompliancePercentage; }

        public List<WorkloadItem> getEmployeeWorkloads() { return employeeWorkloads; }
        public void setEmployeeWorkloads(List<WorkloadItem> employeeWorkloads) { this.employeeWorkloads = employeeWorkloads; }

        public List<ChartDataPoint> getTicketsByPriority() { return ticketsByPriority; }
        public void setTicketsByPriority(List<ChartDataPoint> ticketsByPriority) { this.ticketsByPriority = ticketsByPriority; }

        public List<ChartDataPoint> getTicketsByStatus() { return ticketsByStatus; }
        public void setTicketsByStatus(List<ChartDataPoint> ticketsByStatus) { this.ticketsByStatus = ticketsByStatus; }
    }

    public static class EmployeeDashboardDto {
        private long myAssignedTickets;
        private long myInProgressTickets;
        private long myResolvedTickets;
        private long myOverdueTickets;
        private List<TicketDtos.TicketDto> recentTickets;
        private List<TicketDtos.TicketDto> upcomingDeadlines;

        public long getMyAssignedTickets() { return myAssignedTickets; }
        public void setMyAssignedTickets(long myAssignedTickets) { this.myAssignedTickets = myAssignedTickets; }

        public long getMyInProgressTickets() { return myInProgressTickets; }
        public void setMyInProgressTickets(long myInProgressTickets) { this.myInProgressTickets = myInProgressTickets; }

        public long getMyResolvedTickets() { return myResolvedTickets; }
        public void setMyResolvedTickets(long myResolvedTickets) { this.myResolvedTickets = myResolvedTickets; }

        public long getMyOverdueTickets() { return myOverdueTickets; }
        public void setMyOverdueTickets(long myOverdueTickets) { this.myOverdueTickets = myOverdueTickets; }

        public List<TicketDtos.TicketDto> getRecentTickets() { return recentTickets; }
        public void setRecentTickets(List<TicketDtos.TicketDto> recentTickets) { this.recentTickets = recentTickets; }

        public List<TicketDtos.TicketDto> getUpcomingDeadlines() { return upcomingDeadlines; }
        public void setUpcomingDeadlines(List<TicketDtos.TicketDto> upcomingDeadlines) { this.upcomingDeadlines = upcomingDeadlines; }
    }

    public static class CustomerDashboardDto {
        private long totalTickets;
        private long openTickets;
        private long resolvedTickets;
        private List<TicketDtos.TicketDto> recentTickets;

        public long getTotalTickets() { return totalTickets; }
        public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

        public long getOpenTickets() { return openTickets; }
        public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

        public long getResolvedTickets() { return resolvedTickets; }
        public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }

        public List<TicketDtos.TicketDto> getRecentTickets() { return recentTickets; }
        public void setRecentTickets(List<TicketDtos.TicketDto> recentTickets) { this.recentTickets = recentTickets; }
    }
}
