package com.resolvex.controller;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.SlaDtos;
import com.resolvex.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
@Tag(name = "SLA Policies", description = "Service Level Agreement configuration and escalations")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/policies")
    @Operation(summary = "Get all configured SLA policies")
    public ResponseEntity<ApiResponse<List<SlaDtos.SlaPolicyDto>>> getPolicies() {
        List<SlaDtos.SlaPolicyDto> policies = slaService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an SLA policy deadline thresholds")
    public ResponseEntity<ApiResponse<SlaDtos.SlaPolicyDto>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody SlaDtos.SlaPolicyDto request) {
        SlaDtos.SlaPolicyDto updated = slaService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA policy updated successfully", updated));
    }

    @GetMapping("/escalations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get active SLA escalations")
    public ResponseEntity<ApiResponse<List<SlaDtos.SlaEscalationDto>>> getEscalations() {
        List<SlaDtos.SlaEscalationDto> escalations = slaService.getRecentEscalations();
        return ResponseEntity.ok(ApiResponse.success(escalations));
    }
}
