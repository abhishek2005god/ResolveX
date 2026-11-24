package com.resolvex.controller;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.DashboardDtos;
import com.resolvex.security.UserPrincipal;
import com.resolvex.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboards", description = "Role-specific analytics and metric dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get full system analytics, SLA compliance, trends, and department workloads")
    public ResponseEntity<ApiResponse<DashboardDtos.AdminDashboardDto>> getAdminDashboard() {
        DashboardDtos.AdminDashboardDto data = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get team ticket distribution, agent workloads, and SLA compliance")
    public ResponseEntity<ApiResponse<DashboardDtos.ManagerDashboardDto>> getManagerDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardDtos.ManagerDashboardDto data = dashboardService.getManagerDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get my assigned tickets, in-progress tasks, and upcoming SLA deadlines")
    public ResponseEntity<ApiResponse<DashboardDtos.EmployeeDashboardDto>> getEmployeeDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardDtos.EmployeeDashboardDto data = dashboardService.getEmployeeDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/customer")
    @Operation(summary = "Get customer request overview and recent updates")
    public ResponseEntity<ApiResponse<DashboardDtos.CustomerDashboardDto>> getCustomerDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardDtos.CustomerDashboardDto data = dashboardService.getCustomerDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
