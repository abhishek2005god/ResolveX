package com.resolvex.service;

import com.resolvex.dto.NotificationDto;
import com.resolvex.dto.PagedResponse;
import com.resolvex.entity.Notification;
import com.resolvex.entity.User;
import com.resolvex.enums.NotificationType;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationDto createAndSendNotification(User user, String title, String message,
                                                     NotificationType type, Long referenceId, String referenceType) {
        Notification notification = new Notification(user, title, message, type, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);

        NotificationDto dto = toDto(saved);

        // Send via WebSocket to user specific channel and global broadcast
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
            messagingTemplate.convertAndSend("/topic/notifications", dto);
        } catch (Exception e) {
            logger.warn("Could not dispatch WebSocket notification (fallback to DB only): {}", e.getMessage());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> paged = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDto> dtos = paged.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return new PagedResponse<>(dtos, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (notification.getUser().getId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setUserId(n.getUser().getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setNotificationType(n.getNotificationType());
        dto.setIsRead(n.getIsRead());
        dto.setReferenceId(n.getReferenceId());
        dto.setReferenceType(n.getReferenceType());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
