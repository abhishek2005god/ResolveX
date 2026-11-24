package com.resolvex.service;

import com.resolvex.entity.Category;
import com.resolvex.entity.Team;
import com.resolvex.entity.User;
import com.resolvex.repository.TeamMemberRepository;
import com.resolvex.repository.TicketRepository;
import com.resolvex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AssignmentEngine {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentEngine.class);

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TicketRepository ticketRepository;

    public AssignmentEngine(UserRepository userRepository,
                            TeamMemberRepository teamMemberRepository,
                            TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Deterministic rule-based auto-assignment algorithm.
     * Evaluates active eligible support employees in the assigned team (or category's department),
     * calculates active ticket workload, and selects the candidate with the lowest active workload.
     * Tie-breaker: lowest user ID.
     */
    public Optional<User> findBestAssignee(Category category, Team team) {
        List<User> candidates = new ArrayList<>();

        if (team != null) {
            candidates = userRepository.findActiveMembersByTeamId(team.getId());
        }

        if (candidates.isEmpty() && category != null && category.getDepartment() != null) {
            List<User> deptEmployees = userRepository.findByDepartmentId(category.getDepartment().getId());
            for (User u : deptEmployees) {
                if ("EMPLOYEE".equalsIgnoreCase(u.getRole().getName().name()) && Boolean.TRUE.equals(u.getActive())) {
                    candidates.add(u);
                }
            }
        }

        if (candidates.isEmpty()) {
            candidates = userRepository.findAllActiveEmployees();
        }

        if (candidates.isEmpty()) {
            logger.warn("No active eligible employees found for auto-assignment (Category: {}, Team: {})",
                    category != null ? category.getName() : "None",
                    team != null ? team.getName() : "None");
            return Optional.empty();
        }

        // Rank by active workload ascending, tie-breaker: user.id ascending
        User bestAssignee = candidates.stream()
                .min(Comparator
                        .comparingLong((User u) -> ticketRepository.countActiveTicketsByAssignedTo(u.getId()))
                        .thenComparing(User::getId)
                )
                .orElse(null);

        if (bestAssignee != null) {
            long currentWorkload = ticketRepository.countActiveTicketsByAssignedTo(bestAssignee.getId());
            logger.info("AssignmentEngine selected employee {} (ID: {}) with active workload: {} tickets",
                    bestAssignee.getFullName(), bestAssignee.getId(), currentWorkload);
        }

        return Optional.ofNullable(bestAssignee);
    }
}
