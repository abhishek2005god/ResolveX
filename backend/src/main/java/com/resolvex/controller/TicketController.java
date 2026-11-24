package com.resolvex.controller;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.PagedResponse;
import com.resolvex.dto.TicketDtos;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import com.resolvex.security.UserPrincipal;
import com.resolvex.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Ticket creation, workflow status transitions, assignment, and comments")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<ApiResponse<TicketDtos.TicketDto>> createTicket(
            @Valid @RequestBody TicketDtos.CreateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketDto created = ticketService.createTicket(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket created successfully", created));
    }

    @GetMapping
    @Operation(summary = "Search and filter tickets with pagination and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<TicketDtos.TicketDto>>> getTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PagedResponse<TicketDtos.TicketDto> result = ticketService.searchTickets(
                search, status, priority, categoryId, teamId, assignedToId, isOverdue,
                startDate, endDate, page, size, sortBy, sortDir, currentUser
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed ticket by ID including comments, history, and attachments")
    public ResponseEntity<ApiResponse<TicketDtos.TicketDetailDto>> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketDetailDto ticket = ticketService.getTicketById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket details")
    public ResponseEntity<ApiResponse<TicketDtos.TicketDto>> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketDtos.UpdateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketDto updated = ticketService.updateTicket(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket status according to business workflow rules")
    public ResponseEntity<ApiResponse<TicketDtos.TicketDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketDtos.TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketDto updated = ticketService.updateStatus(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated to " + updated.getStatus(), updated));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Assign or reassign ticket to an employee or trigger automated assignment engine")
    public ResponseEntity<ApiResponse<TicketDtos.TicketDto>> assignTicket(
            @PathVariable Long id,
            @RequestBody TicketDtos.AssignTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketDto assigned = ticketService.assignTicket(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", assigned));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment or internal work note to ticket")
    public ResponseEntity<ApiResponse<TicketDtos.TicketCommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody TicketDtos.CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketCommentDto comment = ticketService.addComment(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Comment added", comment));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment for a ticket with safe extension and size validation")
    public ResponseEntity<ApiResponse<TicketDtos.TicketAttachmentDto>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDtos.TicketAttachmentDto attachment = ticketService.uploadAttachment(id, file, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", attachment));
    }
}
