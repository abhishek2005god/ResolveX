package com.resolvex.service;

import com.resolvex.dto.AuditLogDto;
import com.resolvex.dto.PagedResponse;
import com.resolvex.entity.AuditLog;
import com.resolvex.entity.User;
import com.resolvex.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(User user, String username, String action, String entityType, Long entityId, String changeSummary, String ipAddress) {
        AuditLog log = new AuditLog(user, username, action, entityType, entityId, changeSummary, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> paged = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AuditLogDto> dtos = paged.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return new PagedResponse<>(dtos, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    private AuditLogDto toDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
        dto.setUsername(log.getUsername());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setChangeSummary(log.getChangeSummary());
        dto.setIpAddress(log.getIpAddress());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
