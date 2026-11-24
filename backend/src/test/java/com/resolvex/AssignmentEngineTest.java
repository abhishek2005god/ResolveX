package com.resolvex;

import com.resolvex.entity.Category;
import com.resolvex.entity.Role;
import com.resolvex.entity.Team;
import com.resolvex.entity.User;
import com.resolvex.enums.RoleName;
import com.resolvex.enums.TicketPriority;
import com.resolvex.repository.TeamMemberRepository;
import com.resolvex.repository.TicketRepository;
import com.resolvex.repository.UserRepository;
import com.resolvex.service.AssignmentEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentEngineTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AssignmentEngine assignmentEngine;

    private User employee1;
    private User employee2;
    private Team team;
    private Category category;

    @BeforeEach
    void setUp() {
        Role role = new Role(RoleName.EMPLOYEE, "Employee");

        employee1 = new User("emp1", "emp1@resolvex.com", "hash", "Employee One", role);
        employee1.setId(101L);
        employee1.setActive(true);

        employee2 = new User("emp2", "emp2@resolvex.com", "hash", "Employee Two", role);
        employee2.setId(102L);
        employee2.setActive(true);

        team = new Team("Cloud Team", null, "Cloud");
        team.setId(1L);

        category = new Category("Cloud Infrastructure", "Cloud", null, TicketPriority.HIGH);
        category.setId(1L);
    }

    @Test
    @DisplayName("Should select employee with lowest active ticket workload")
    void shouldSelectEmployeeWithLowestWorkload() {
        when(userRepository.findActiveMembersByTeamId(1L)).thenReturn(List.of(employee1, employee2));
        // Employee 1 has 5 active tickets, Employee 2 has 2 active tickets
        when(ticketRepository.countActiveTicketsByAssignedTo(101L)).thenReturn(5L);
        when(ticketRepository.countActiveTicketsByAssignedTo(102L)).thenReturn(2L);

        Optional<User> selected = assignmentEngine.findBestAssignee(category, team);

        assertTrue(selected.isPresent());
        assertEquals(102L, selected.get().getId(), "Should pick Employee Two with 2 active tickets");
    }

    @Test
    @DisplayName("Should use deterministic ID tie-breaker when workloads are identical")
    void shouldBreakTieDeterministicallyByUserId() {
        when(userRepository.findActiveMembersByTeamId(1L)).thenReturn(List.of(employee2, employee1));
        // Both have 3 active tickets
        when(ticketRepository.countActiveTicketsByAssignedTo(101L)).thenReturn(3L);
        when(ticketRepository.countActiveTicketsByAssignedTo(102L)).thenReturn(3L);

        Optional<User> selected = assignmentEngine.findBestAssignee(category, team);

        assertTrue(selected.isPresent());
        assertEquals(101L, selected.get().getId(), "Should pick Employee One due to lower ID tie-breaker");
    }

    @Test
    @DisplayName("Should return empty optional when no eligible active employees exist")
    void shouldReturnEmptyWhenNoEligibleCandidates() {
        when(userRepository.findActiveMembersByTeamId(1L)).thenReturn(List.of());
        when(userRepository.findAllActiveEmployees()).thenReturn(List.of());

        Optional<User> selected = assignmentEngine.findBestAssignee(category, team);

        assertTrue(selected.isEmpty(), "Should return empty when no candidates are available");
    }
}
