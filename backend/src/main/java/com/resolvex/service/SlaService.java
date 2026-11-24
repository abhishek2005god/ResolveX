package com.resolvex.service;

import com.resolvex.dto.SlaDtos;
import com.resolvex.entity.*;
import com.resolvex.enums.NotificationType;
import com.resolvex.enums.RoleName;
import com.resolvex.enums.TicketPriority;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.SlaEscalationRepository;
import com.resolvex.repository.SlaPolicyRepository;
import com.resolvex.repository.TicketRepository;
import com.resolvex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class SlaService {

    private static final Logger logger = LoggerFactory.getLogger(SlaService.class);

    private final SlaPolicyRepository slaPolicyRepository;
    private final SlaEscalationRepository slaEscalationRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public SlaService(SlaPolicyRepository slaPolicyRepository,
                      SlaEscalationRepository slaEscalationRepository,
                      TicketRepository ticketRepository,
                      UserRepository userRepository,
                      NotificationService notificationService,
                      AuditService auditService) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.slaEscalationRepository = slaEscalationRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public OffsetDateTime calculateDueDate(TicketPriority priority, OffsetDateTime baseTime) {
        if (baseTime == null) baseTime = OffsetDateTime.now();

        Optional<SlaPolicy> policyOpt = slaPolicyRepository.findByPriorityAndIsActiveTrue(priority);
        int hours = 24; // default
        if (policyOpt.isPresent()) {
            hours = policyOpt.get().getMaxResolutionHours();
        } else {
            hours = switch (priority) {
                case CRITICAL -> 4;
                case HIGH -> 8;
                case MEDIUM -> 24;
                case LOW -> 48;
            };
        }

        return baseTime.plusHours(hours);
    }

    @Scheduled(fixedDelay = 60000) // check every 60 seconds
    @Transactional
    public void monitorSlaBreaches() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Ticket> overdueTickets = ticketRepository.findUnflaggedOverdueTickets(now);

        if (overdueTickets.isEmpty()) {
            return;
        }

        logger.info("SLA Monitor detected {} tickets exceeding due date", overdueTickets.size());

        List<User> managers = userRepository.findByRoleName(RoleName.MANAGER);

        for (Ticket ticket : overdueTickets) {
            ticket.setIsOverdue(true);
            ticketRepository.save(ticket);

            // Check if escalation already created for level 1 (idempotency guard against duplicates)
            if (!slaEscalationRepository.existsByTicketIdAndEscalationLevel(ticket.getId(), 1)) {
                SlaPolicy policy = slaPolicyRepository.findByPriority(ticket.getPriority()).orElse(null);
                User managerToEscalate = managers.isEmpty() ? null : managers.get(0);

                SlaEscalation escalation = new SlaEscalation(
                        ticket,
                        policy,
                        1,
                        managerToEscalate,
                        "Automatic SLA Breach Escalation: Resolution deadline exceeded for " + ticket.getTicketCode()
                );
                slaEscalationRepository.save(escalation);

                // Notify Assigned Employee if any
                if (ticket.getAssignedTo() != null) {
                    notificationService.createAndSendNotification(
                            ticket.getAssignedTo(),
                            "SLA Breached: " + ticket.getTicketCode(),
                            "Ticket " + ticket.getTicketCode() + " has breached its SLA resolution deadline.",
                            NotificationType.SLA_BREACHED,
                            ticket.getId(),
                            "TICKET"
                    );
                }

                // Notify Managers
                for (User mgr : managers) {
                    notificationService.createAndSendNotification(
                            mgr,
                            "Urgent SLA Escalation: " + ticket.getTicketCode(),
                            "Ticket " + ticket.getTicketCode() + " (" + ticket.getPriority() + ") has breached SLA and requires management attention.",
                            NotificationType.ESCALATION,
                            ticket.getId(),
                            "TICKET"
                    );
                }

                auditService.logAction(
                        null,
                        "SYSTEM_SLA_ENGINE",
                        "SLA_ESCALATION",
                        "TICKET",
                        ticket.getId(),
                        "Ticket marked overdue and escalated due to SLA resolution breach",
                        "SYSTEM"
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SlaDtos.SlaPolicyDto> getAllPolicies() {
        return slaPolicyRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SlaDtos.SlaPolicyDto updatePolicy(Long id, SlaDtos.SlaPolicyDto request) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SlaPolicy", "id", id));

        policy.setName(request.getName());
        policy.setMaxResponseHours(request.getMaxResponseHours());
        policy.setMaxResolutionHours(request.getMaxResolutionHours());
        policy.setEscalationThresholdHours(request.getEscalationThresholdHours());
        if (request.getIsActive() != null) {
            policy.setIsActive(request.getIsActive());
        }

        SlaPolicy saved = slaPolicyRepository.save(policy);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SlaDtos.SlaEscalationDto> getRecentEscalations() {
        return slaEscalationRepository.findByResolvedFalse().stream()
                .map(this::toEscalationDto)
                .collect(Collectors.toList());
    }

    private SlaDtos.SlaPolicyDto toDto(SlaPolicy p) {
        SlaDtos.SlaPolicyDto dto = new SlaDtos.SlaPolicyDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPriority(p.getPriority());
        dto.setMaxResponseHours(p.getMaxResponseHours());
        dto.setMaxResolutionHours(p.getMaxResolutionHours());
        dto.setEscalationThresholdHours(p.getEscalationThresholdHours());
        dto.setIsActive(p.getIsActive());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    private SlaDtos.SlaEscalationDto toEscalationDto(SlaEscalation e) {
        SlaDtos.SlaEscalationDto dto = new SlaDtos.SlaEscalationDto();
        dto.setId(e.getId());
        dto.setTicketId(e.getTicket().getId());
        dto.setTicketCode(e.getTicket().getTicketCode());
        dto.setTicketTitle(e.getTicket().getTitle());
        dto.setEscalationLevel(e.getEscalationLevel());
        dto.setEscalatedToName(e.getEscalatedTo() != null ? e.getEscalatedTo().getFullName() : "Unassigned");
        dto.setReason(e.getReason());
        dto.setResolved(e.getResolved());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
