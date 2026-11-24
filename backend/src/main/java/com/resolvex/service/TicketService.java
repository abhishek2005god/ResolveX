package com.resolvex.service;

import com.resolvex.dto.PagedResponse;
import com.resolvex.dto.TicketDtos;
import com.resolvex.entity.*;
import com.resolvex.enums.AssignmentType;
import com.resolvex.enums.NotificationType;
import com.resolvex.enums.RoleName;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import com.resolvex.exception.BadRequestException;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.*;
import com.resolvex.security.UserPrincipal;
import com.resolvex.specification.TicketSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TeamRepository teamRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentEngine assignmentEngine;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         TeamRepository teamRepository,
                         TicketCommentRepository commentRepository,
                         TicketHistoryRepository historyRepository,
                         TicketAttachmentRepository attachmentRepository,
                         AssignmentRepository assignmentRepository,
                         AssignmentEngine assignmentEngine,
                         SlaService slaService,
                         NotificationService notificationService,
                         AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.teamRepository = teamRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.attachmentRepository = attachmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentEngine = assignmentEngine;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public TicketDtos.TicketDto createTicket(TicketDtos.CreateTicketRequest request, UserPrincipal currentUser) {
        User creator = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        TicketPriority priority = request.getPriority() != null ? request.getPriority() : category.getDefaultPriority();

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId()).orElse(null);
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setCreatedBy(creator);
        ticket.setTeam(team);
        ticket.setStatus(TicketStatus.OPEN);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime dueDate = slaService.calculateDueDate(priority, now);
        ticket.setDueDate(dueDate);

        // Generate unique code
        String ticketCode = generateUniqueTicketCode();
        ticket.setTicketCode(ticketCode);

        // Assignment logic:
        User assignee = null;
        AssignmentType assignType = AssignmentType.AUTOMATIC;
        String assignReason = "Automated Workload-Based Assignment";

        if (request.getAssignedToId() != null && (isManagerOrAdmin(currentUser))) {
            assignee = userRepository.findById(request.getAssignedToId()).orElse(null);
            assignType = AssignmentType.MANUAL;
            assignReason = "Manual pre-assignment on ticket creation";
        } else {
            Optional<User> bestAssignee = assignmentEngine.findBestAssignee(category, team);
            if (bestAssignee.isPresent()) {
                assignee = bestAssignee.get();
            }
        }

        if (assignee != null) {
            ticket.setAssignedTo(assignee);
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        // Record history
        TicketHistory history = new TicketHistory(savedTicket, creator, "CREATE", null, null, "Ticket created with status " + savedTicket.getStatus());
        historyRepository.save(history);

        // Record assignment record if assigned
        if (assignee != null) {
            Assignment assignment = new Assignment(savedTicket, creator, assignee, assignReason, assignType);
            assignmentRepository.save(assignment);

            // Notify assignee
            notificationService.createAndSendNotification(
                    assignee,
                    "New Ticket Assigned: " + ticketCode,
                    "You have been assigned to ticket " + ticketCode + " (" + priority + "): " + ticket.getTitle(),
                    NotificationType.TICKET_ASSIGNED,
                    savedTicket.getId(),
                    "TICKET"
            );
        }

        // Notify user about ticket creation
        notificationService.createAndSendNotification(
                creator,
                "Ticket Submitted: " + ticketCode,
                "Your ticket " + ticketCode + " has been received and is currently " + savedTicket.getStatus(),
                NotificationType.TICKET_CREATED,
                savedTicket.getId(),
                "TICKET"
        );

        auditService.logAction(creator, creator.getUsername(), "CREATE_TICKET", "TICKET", savedTicket.getId(),
                "Created ticket " + ticketCode + " with priority " + priority, "API");

        return toDto(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketDtos.TicketDetailDto getTicketById(Long id, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Role check
        if (isCustomer(currentUser) && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this ticket");
        }

        TicketDtos.TicketDetailDto detailDto = toDetailDto(ticket);

        // Comments
        List<TicketComment> comments;
        if (isCustomer(currentUser)) {
            comments = commentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(id);
        } else {
            comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(id);
        }
        detailDto.setComments(comments.stream().map(this::toCommentDto).collect(Collectors.toList()));

        // History
        List<TicketHistory> histories = historyRepository.findByTicketIdOrderByCreatedAtDesc(id);
        detailDto.setHistory(histories.stream().map(this::toHistoryDto).collect(Collectors.toList()));

        // Attachments
        List<TicketAttachment> attachments = attachmentRepository.findByTicketIdOrderByCreatedAtDesc(id);
        detailDto.setAttachments(attachments.stream().map(this::toAttachmentDto).collect(Collectors.toList()));

        return detailDto;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketDtos.TicketDto> searchTickets(
            String search,
            TicketStatus status,
            TicketPriority priority,
            Long categoryId,
            Long teamId,
            Long assignedToId,
            Boolean isOverdue,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir,
            UserPrincipal currentUser
    ) {
        Long enforceCreatedBy = null;
        if (isCustomer(currentUser)) {
            enforceCreatedBy = currentUser.getId();
        }

        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                StringUtils.hasText(sortBy) ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Ticket> spec = TicketSpecification.withFilters(
                search, status, priority, categoryId, teamId, assignedToId, enforceCreatedBy, isOverdue, startDate, endDate
        );

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);
        List<TicketDtos.TicketDto> dtos = ticketPage.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return new PagedResponse<>(dtos, ticketPage.getNumber(), ticketPage.getSize(),
                ticketPage.getTotalElements(), ticketPage.getTotalPages(), ticketPage.isLast());
    }

    @Transactional
    public TicketDtos.TicketDto updateTicket(Long id, TicketDtos.UpdateTicketRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User user = userRepository.findById(currentUser.getId()).orElse(null);

        if (isCustomer(currentUser) && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to update this ticket");
        }

        if (StringUtils.hasText(request.getTitle()) && !request.getTitle().equals(ticket.getTitle())) {
            historyRepository.save(new TicketHistory(ticket, user, "UPDATE_TITLE", "title", ticket.getTitle(), request.getTitle()));
            ticket.setTitle(request.getTitle().trim());
        }

        if (StringUtils.hasText(request.getDescription()) && !request.getDescription().equals(ticket.getDescription())) {
            historyRepository.save(new TicketHistory(ticket, user, "UPDATE_DESCRIPTION", "description", ticket.getDescription(), request.getDescription()));
            ticket.setDescription(request.getDescription().trim());
        }

        if (request.getCategoryId() != null && !request.getCategoryId().equals(ticket.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            historyRepository.save(new TicketHistory(ticket, user, "UPDATE_CATEGORY", "category", ticket.getCategory().getName(), newCategory.getName()));
            ticket.setCategory(newCategory);
        }

        if (request.getPriority() != null && !request.getPriority().equals(ticket.getPriority())) {
            historyRepository.save(new TicketHistory(ticket, user, "UPDATE_PRIORITY", "priority", ticket.getPriority().name(), request.getPriority().name()));
            ticket.setPriority(request.getPriority());
            // Recalculate SLA due date
            ticket.setDueDate(slaService.calculateDueDate(ticket.getPriority(), ticket.getCreatedAt()));
        }

        if (request.getTeamId() != null) {
            Team newTeam = teamRepository.findById(request.getTeamId()).orElse(null);
            ticket.setTeam(newTeam);
        }

        Ticket saved = ticketRepository.save(ticket);
        return toDto(saved);
    }

    @Transactional
    public TicketDtos.TicketDto updateStatus(Long id, TicketDtos.TicketStatusUpdateRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            return toDto(ticket);
        }

        // Validate state transitions
        validateStatusTransition(currentStatus, targetStatus, currentUser);

        ticket.setStatus(targetStatus);
        OffsetDateTime now = OffsetDateTime.now();

        if (targetStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(now);
            if (StringUtils.hasText(request.getNotes())) {
                ticket.setResolutionNotes(request.getNotes());
            }
        } else if (targetStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(now);
        } else if (targetStatus == TicketStatus.REOPENED) {
            ticket.setResolvedAt(null);
            ticket.setClosedAt(null);
        }

        Ticket saved = ticketRepository.save(ticket);

        // Record history
        String noteMsg = StringUtils.hasText(request.getNotes()) ? " - Notes: " + request.getNotes() : "";
        TicketHistory history = new TicketHistory(saved, user, "STATUS_CHANGE", "status", currentStatus.name(), targetStatus.name() + noteMsg);
        historyRepository.save(history);

        // Notify ticket creator
        notificationService.createAndSendNotification(
                ticket.getCreatedBy(),
                "Ticket Status Changed: " + ticket.getTicketCode(),
                "Your ticket status changed from " + currentStatus + " to " + targetStatus,
                NotificationType.STATUS_CHANGED,
                ticket.getId(),
                "TICKET"
        );

        // Notify assignee if not the one changing status
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            notificationService.createAndSendNotification(
                    ticket.getAssignedTo(),
                    "Ticket Status Updated: " + ticket.getTicketCode(),
                    "Ticket status updated to " + targetStatus + " by " + currentUser.getFullName(),
                    NotificationType.STATUS_CHANGED,
                    ticket.getId(),
                    "TICKET"
            );
        }

        auditService.logAction(user, currentUser.getUsername(), "STATUS_CHANGE", "TICKET", ticket.getId(),
                "Changed status from " + currentStatus + " to " + targetStatus, "API");

        return toDto(saved);
    }

    @Transactional
    public TicketDtos.TicketDto assignTicket(Long id, TicketDtos.AssignTicketRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User assigner = userRepository.findById(currentUser.getId()).orElse(null);

        User assignee = null;
        AssignmentType type = AssignmentType.MANUAL;
        String reason = request.getReason();

        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToId()));
        } else {
            // Trigger auto-assignment engine
            type = AssignmentType.AUTOMATIC;
            reason = StringUtils.hasText(request.getReason()) ? request.getReason() : "Auto-reassignment requested by manager";
            Optional<User> best = assignmentEngine.findBestAssignee(ticket.getCategory(), ticket.getTeam());
            if (best.isPresent()) {
                assignee = best.get();
            } else {
                throw new BadRequestException("No eligible support employees found for auto-assignment.");
            }
        }

        String oldAssigneeName = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "Unassigned";
        ticket.setAssignedTo(assignee);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket saved = ticketRepository.save(ticket);

        // Save Assignment record
        Assignment assignment = new Assignment(saved, assigner, assignee, reason, type);
        assignmentRepository.save(assignment);

        // Save History
        TicketHistory history = new TicketHistory(saved, assigner, "ASSIGNMENT", "assignedTo", oldAssigneeName, assignee.getFullName());
        historyRepository.save(history);

        // Notify new assignee
        notificationService.createAndSendNotification(
                assignee,
                "Ticket Assigned: " + ticket.getTicketCode(),
                "You have been assigned to ticket " + ticket.getTicketCode() + ": " + ticket.getTitle(),
                NotificationType.TICKET_ASSIGNED,
                ticket.getId(),
                "TICKET"
        );

        auditService.logAction(assigner, currentUser.getUsername(), "ASSIGN_TICKET", "TICKET", ticket.getId(),
                "Assigned to " + assignee.getFullName() + " (" + type + ")", "API");

        return toDto(saved);
    }

    @Transactional
    public TicketDtos.TicketCommentDto addComment(Long ticketId, TicketDtos.CreateCommentRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User author = userRepository.findById(currentUser.getId()).orElse(null);

        // Customer cannot post internal comments
        boolean isInternal = Boolean.TRUE.equals(request.getIsInternal());
        if (isCustomer(currentUser)) {
            isInternal = false;
        }

        TicketComment comment = new TicketComment(ticket, author, request.getCommentText().trim(), isInternal);
        TicketComment saved = commentRepository.save(comment);

        // History
        String commentNote = isInternal ? "Added internal note" : "Added public comment";
        historyRepository.save(new TicketHistory(ticket, author, "COMMENT", null, null, commentNote));

        // Notifications
        if (!isInternal) {
            // Notify creator if author is employee/manager
            if (ticket.getCreatedBy() != null && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                notificationService.createAndSendNotification(
                        ticket.getCreatedBy(),
                        "New Comment on " + ticket.getTicketCode(),
                        currentUser.getFullName() + " commented: " + truncate(request.getCommentText(), 80),
                        NotificationType.COMMENT_ADDED,
                        ticket.getId(),
                        "TICKET"
                );
            }
            // Notify assignee if author is customer
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                notificationService.createAndSendNotification(
                        ticket.getAssignedTo(),
                        "New Customer Comment on " + ticket.getTicketCode(),
                        currentUser.getFullName() + " commented: " + truncate(request.getCommentText(), 80),
                        NotificationType.COMMENT_ADDED,
                        ticket.getId(),
                        "TICKET"
                );
            }
        }

        return toCommentDto(saved);
    }

    @Transactional
    public TicketDtos.TicketAttachmentDto uploadAttachment(Long ticketId, MultipartFile file, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User uploader = userRepository.findById(currentUser.getId()).orElse(null);

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new BadRequestException("File size exceeds 10MB maximum limit");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Safe file extension check
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";
        Set<String> safeExtensions = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt", ".csv", ".doc", ".docx", ".zip", ".log");
        if (!safeExtensions.contains(ext)) {
            throw new BadRequestException("File type not allowed: " + ext);
        }

        String storedFileName = UUID.randomUUID() + "_" + originalFilename;
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            TicketAttachment attachment = new TicketAttachment();
            attachment.setTicket(ticket);
            attachment.setFileName(originalFilename);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setStoragePath(targetLocation.toString());
            attachment.setUploadedBy(uploader);

            TicketAttachment saved = attachmentRepository.save(attachment);

            historyRepository.save(new TicketHistory(ticket, uploader, "ATTACHMENT", "file", null, "Uploaded " + originalFilename));

            return toAttachmentDto(saved);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }
    }

    private void validateStatusTransition(TicketStatus current, TicketStatus target, UserPrincipal user) {
        boolean valid = switch (current) {
            case OPEN -> (target == TicketStatus.ASSIGNED || target == TicketStatus.IN_PROGRESS || target == TicketStatus.ON_HOLD || target == TicketStatus.RESOLVED);
            case ASSIGNED -> (target == TicketStatus.IN_PROGRESS || target == TicketStatus.ON_HOLD || target == TicketStatus.RESOLVED);
            case IN_PROGRESS -> (target == TicketStatus.ON_HOLD || target == TicketStatus.RESOLVED);
            case ON_HOLD -> (target == TicketStatus.IN_PROGRESS || target == TicketStatus.RESOLVED);
            case RESOLVED -> (target == TicketStatus.CLOSED || target == TicketStatus.REOPENED);
            case CLOSED -> (target == TicketStatus.REOPENED);
            case REOPENED -> (target == TicketStatus.ASSIGNED || target == TicketStatus.IN_PROGRESS || target == TicketStatus.RESOLVED);
        };

        if (!valid) {
            throw new BadRequestException("Invalid ticket status transition from " + current + " to " + target);
        }
    }

    private String generateUniqueTicketCode() {
        long count = ticketRepository.count();
        int randomPart = (int) (Math.random() * 900) + 100;
        return "TICK-" + (1000 + count + 1) + (count % 3 == 0 ? "" : "-" + randomPart);
    }

    private boolean isManagerOrAdmin(UserPrincipal user) {
        return "ADMIN".equalsIgnoreCase(user.getRole()) || "MANAGER".equalsIgnoreCase(user.getRole());
    }

    private boolean isCustomer(UserPrincipal user) {
        return "CUSTOMER".equalsIgnoreCase(user.getRole());
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    public TicketDtos.TicketDto toDto(Ticket t) {
        TicketDtos.TicketDto dto = new TicketDtos.TicketDto();
        dto.setId(t.getId());
        dto.setTicketCode(t.getTicketCode());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryName(t.getCategory().getName());
        }
        dto.setPriority(t.getPriority());
        dto.setStatus(t.getStatus());
        if (t.getCreatedBy() != null) {
            dto.setCreatedById(t.getCreatedBy().getId());
            dto.setCreatedByName(t.getCreatedBy().getFullName());
            dto.setCreatedByEmail(t.getCreatedBy().getEmail());
        }
        if (t.getAssignedTo() != null) {
            dto.setAssignedToId(t.getAssignedTo().getId());
            dto.setAssignedToName(t.getAssignedTo().getFullName());
        }
        if (t.getTeam() != null) {
            dto.setTeamId(t.getTeam().getId());
            dto.setTeamName(t.getTeam().getName());
        }
        dto.setDueDate(t.getDueDate());
        dto.setResolvedAt(t.getResolvedAt());
        dto.setClosedAt(t.getClosedAt());
        dto.setResolutionNotes(t.getResolutionNotes());
        dto.setIsOverdue(t.getIsOverdue());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    private TicketDtos.TicketDetailDto toDetailDto(Ticket t) {
        TicketDtos.TicketDetailDto dto = new TicketDtos.TicketDetailDto();
        dto.setId(t.getId());
        dto.setTicketCode(t.getTicketCode());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryName(t.getCategory().getName());
        }
        dto.setPriority(t.getPriority());
        dto.setStatus(t.getStatus());
        if (t.getCreatedBy() != null) {
            dto.setCreatedById(t.getCreatedBy().getId());
            dto.setCreatedByName(t.getCreatedBy().getFullName());
            dto.setCreatedByEmail(t.getCreatedBy().getEmail());
        }
        if (t.getAssignedTo() != null) {
            dto.setAssignedToId(t.getAssignedTo().getId());
            dto.setAssignedToName(t.getAssignedTo().getFullName());
        }
        if (t.getTeam() != null) {
            dto.setTeamId(t.getTeam().getId());
            dto.setTeamName(t.getTeam().getName());
        }
        dto.setDueDate(t.getDueDate());
        dto.setResolvedAt(t.getResolvedAt());
        dto.setClosedAt(t.getClosedAt());
        dto.setResolutionNotes(t.getResolutionNotes());
        dto.setIsOverdue(t.getIsOverdue());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    private TicketDtos.TicketCommentDto toCommentDto(TicketComment c) {
        TicketDtos.TicketCommentDto dto = new TicketDtos.TicketCommentDto();
        dto.setId(c.getId());
        dto.setTicketId(c.getTicket().getId());
        if (c.getUser() != null) {
            dto.setUserId(c.getUser().getId());
            dto.setUsername(c.getUser().getUsername());
            dto.setUserFullName(c.getUser().getFullName());
            dto.setUserRole(c.getUser().getRole().getName().name());
        }
        dto.setCommentText(c.getCommentText());
        dto.setIsInternal(c.getIsInternal());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private TicketDtos.TicketHistoryDto toHistoryDto(TicketHistory h) {
        TicketDtos.TicketHistoryDto dto = new TicketDtos.TicketHistoryDto();
        dto.setId(h.getId());
        dto.setTicketId(h.getTicket().getId());
        if (h.getChangedBy() != null) {
            dto.setChangedById(h.getChangedBy().getId());
            dto.setChangedByName(h.getChangedBy().getFullName());
        }
        dto.setAction(h.getAction());
        dto.setFieldName(h.getFieldName());
        dto.setOldValue(h.getOldValue());
        dto.setNewValue(h.getNewValue());
        dto.setCreatedAt(h.getCreatedAt());
        return dto;
    }

    private TicketDtos.TicketAttachmentDto toAttachmentDto(TicketAttachment a) {
        TicketDtos.TicketAttachmentDto dto = new TicketDtos.TicketAttachmentDto();
        dto.setId(a.getId());
        dto.setTicketId(a.getTicket().getId());
        dto.setFileName(a.getFileName());
        dto.setFileType(a.getFileType());
        dto.setFileSize(a.getFileSize());
        dto.setUploadedByName(a.getUploadedBy() != null ? a.getUploadedBy().getFullName() : "Unknown");
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
