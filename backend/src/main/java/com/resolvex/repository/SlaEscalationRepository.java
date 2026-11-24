package com.resolvex.repository;

import com.resolvex.entity.SlaEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaEscalationRepository extends JpaRepository<SlaEscalation, Long> {
    List<SlaEscalation> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    List<SlaEscalation> findByResolvedFalse();
    boolean existsByTicketIdAndEscalationLevel(Long ticketId, Integer escalationLevel);
}
