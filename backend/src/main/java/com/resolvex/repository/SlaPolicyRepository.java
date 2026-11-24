package com.resolvex.repository;

import com.resolvex.entity.SlaPolicy;
import com.resolvex.enums.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriority(TicketPriority priority);
    Optional<SlaPolicy> findByPriorityAndIsActiveTrue(TicketPriority priority);
}
