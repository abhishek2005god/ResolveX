package com.resolvex;

import com.resolvex.entity.SlaPolicy;
import com.resolvex.enums.TicketPriority;
import com.resolvex.repository.SlaEscalationRepository;
import com.resolvex.repository.SlaPolicyRepository;
import com.resolvex.repository.TicketRepository;
import com.resolvex.repository.UserRepository;
import com.resolvex.service.AuditService;
import com.resolvex.service.NotificationService;
import com.resolvex.service.SlaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlaServiceTest {

    @Mock
    private SlaPolicyRepository slaPolicyRepository;
    @Mock
    private SlaEscalationRepository slaEscalationRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private SlaService slaService;

    @Test
    @DisplayName("Should calculate due date from active SLA policy")
    void shouldCalculateDueDateFromPolicy() {
        SlaPolicy criticalPolicy = new SlaPolicy("Critical SLA", TicketPriority.CRITICAL, 1, 4, 3);
        when(slaPolicyRepository.findByPriorityAndIsActiveTrue(TicketPriority.CRITICAL))
                .thenReturn(Optional.of(criticalPolicy));

        OffsetDateTime baseTime = OffsetDateTime.now();
        OffsetDateTime dueDate = slaService.calculateDueDate(TicketPriority.CRITICAL, baseTime);

        assertNotNull(dueDate);
        assertEquals(baseTime.plusHours(4).getHour(), dueDate.getHour());
    }

    @Test
    @DisplayName("Should use fallback defaults when policy is not configured in DB")
    void shouldFallbackToDefaultsWhenPolicyNotFound() {
        when(slaPolicyRepository.findByPriorityAndIsActiveTrue(TicketPriority.HIGH))
                .thenReturn(Optional.empty());

        OffsetDateTime baseTime = OffsetDateTime.now();
        OffsetDateTime dueDate = slaService.calculateDueDate(TicketPriority.HIGH, baseTime);

        assertNotNull(dueDate);
        // Default high priority is 8 hours
        assertEquals(baseTime.plusHours(8).getHour(), dueDate.getHour());
    }
}
