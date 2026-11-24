package com.resolvex.specification;

import com.resolvex.entity.Ticket;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilters(
            String search,
            TicketStatus status,
            TicketPriority priority,
            Long categoryId,
            Long teamId,
            Long assignedToId,
            Long createdById,
            Boolean isOverdue,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("ticketCode")), term);
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), term);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), term);
                predicates.add(cb.or(codeMatch, titleMatch, descMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (teamId != null) {
                predicates.add(cb.equal(root.get("team").get("id"), teamId));
            }

            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }

            if (createdById != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), createdById));
            }

            if (isOverdue != null) {
                predicates.add(cb.equal(root.get("isOverdue"), isOverdue));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
