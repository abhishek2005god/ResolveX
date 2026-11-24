package com.resolvex.repository;

import com.resolvex.entity.Ticket;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByTicketCode(String ticketCode);

    Page<Ticket> findByCreatedById(Long createdById, Pageable pageable);
    Page<Ticket> findByAssignedToId(Long assignedToId, Pageable pageable);
    Page<Ticket> findByTeamId(Long teamId, Pageable pageable);

    long countByStatus(TicketStatus status);
    long countByPriority(TicketPriority priority);
    long countByIsOverdueTrue();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :userId AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveTicketsByAssignedTo(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.team.id = :teamId AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveTicketsByTeam(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdBy.id = :userId")
    long countByCreatedById(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdBy.id = :userId AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveTicketsByCreatedById(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdBy.id = :userId AND t.status IN ('RESOLVED', 'CLOSED')")
    long countResolvedTicketsByCreatedById(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND t.dueDate IS NOT NULL AND t.dueDate < :now AND (t.isOverdue IS NULL OR t.isOverdue = false)")
    List<Ticket> findUnflaggedOverdueTickets(@Param("now") OffsetDateTime now);

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countGroupedByPriority();

    @Query("SELECT FUNCTION('to_char', t.createdAt, 'YYYY-MM-DD'), COUNT(t) FROM Ticket t WHERE t.createdAt >= :since GROUP BY FUNCTION('to_char', t.createdAt, 'YYYY-MM-DD') ORDER BY FUNCTION('to_char', t.createdAt, 'YYYY-MM-DD') ASC")
    List<Object[]> countTicketsTrendLastDays(@Param("since") OffsetDateTime since);
}
