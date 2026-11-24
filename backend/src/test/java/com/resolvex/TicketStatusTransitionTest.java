package com.resolvex;

import com.resolvex.dto.TicketDtos;
import com.resolvex.entity.Category;
import com.resolvex.entity.Role;
import com.resolvex.entity.Ticket;
import com.resolvex.entity.User;
import com.resolvex.enums.RoleName;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import com.resolvex.exception.BadRequestException;
import com.resolvex.repository.*;
import com.resolvex.security.UserPrincipal;
import com.resolvex.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketStatusTransitionTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TicketCommentRepository commentRepository;
    @Mock private TicketHistoryRepository historyRepository;
    @Mock private TicketAttachmentRepository attachmentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentEngine assignmentEngine;
    @Mock private SlaService slaService;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private UserPrincipal adminPrincipal;
    private User adminUser;

    @BeforeEach
    void setUp() {
        Role role = new Role(RoleName.ADMIN, "Admin");
        adminUser = new User("admin", "admin@resolvex.com", "pass", "Admin User", role);
        adminUser.setId(1L);

        adminPrincipal = new UserPrincipal(1L, "admin", "admin@resolvex.com", "Admin User", "pass",
                "ADMIN", true, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        Category cat = new Category("Network", "Network issue", null, TicketPriority.HIGH);
        cat.setId(10L);

        ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketCode("TICK-1001");
        ticket.setTitle("Sample Issue");
        ticket.setDescription("Sample Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setCategory(cat);
        ticket.setCreatedBy(adminUser);
        ticket.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Valid transition: OPEN -> IN_PROGRESS should succeed")
    void validTransitionOpenToInProgress() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketDtos.TicketStatusUpdateRequest req = new TicketDtos.TicketStatusUpdateRequest();
        req.setStatus(TicketStatus.IN_PROGRESS);

        TicketDtos.TicketDto result = ticketService.updateStatus(100L, req, adminPrincipal);

        assertNotNull(result);
        assertEquals(TicketStatus.IN_PROGRESS, result.getStatus());
        verify(historyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Valid transition: IN_PROGRESS -> RESOLVED should record resolved timestamp")
    void validTransitionToResolved() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketDtos.TicketStatusUpdateRequest req = new TicketDtos.TicketStatusUpdateRequest();
        req.setStatus(TicketStatus.RESOLVED);
        req.setNotes("Fixed routing issue on switch");

        TicketDtos.TicketDto result = ticketService.updateStatus(100L, req, adminPrincipal);

        assertNotNull(result);
        assertEquals(TicketStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        assertEquals("Fixed routing issue on switch", result.getResolutionNotes());
    }

    @Test
    @DisplayName("Invalid transition: CLOSED -> IN_PROGRESS directly should throw BadRequestException")
    void invalidTransitionClosedToInProgressShouldFail() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        TicketDtos.TicketStatusUpdateRequest req = new TicketDtos.TicketStatusUpdateRequest();
        req.setStatus(TicketStatus.IN_PROGRESS);

        assertThrows(BadRequestException.class, () -> {
            ticketService.updateStatus(100L, req, adminPrincipal);
        });
    }
}
