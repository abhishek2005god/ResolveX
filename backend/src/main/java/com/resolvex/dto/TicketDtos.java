package com.resolvex.dto;

import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public class TicketDtos {

    public static class CreateTicketRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        private TicketPriority priority; // defaults to category default if null
        private Long teamId;
        private Long assignedToId; // optional manual pre-assignment

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public TicketPriority getPriority() { return priority; }
        public void setPriority(TicketPriority priority) { this.priority = priority; }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public Long getAssignedToId() { return assignedToId; }
        public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    }

    public static class UpdateTicketRequest {
        private String title;
        private String description;
        private Long categoryId;
        private TicketPriority priority;
        private Long teamId;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public TicketPriority getPriority() { return priority; }
        public void setPriority(TicketPriority priority) { this.priority = priority; }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
    }

    public static class TicketStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private TicketStatus status;

        private String notes;

        public TicketStatus getStatus() { return status; }
        public void setStatus(TicketStatus status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class AssignTicketRequest {
        private Long assignedToId; // null means auto-assign using algorithm
        private Long teamId;
        private String reason;

        public Long getAssignedToId() { return assignedToId; }
        public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CreateCommentRequest {
        @NotBlank(message = "Comment text is required")
        private String commentText;

        private Boolean isInternal = false;

        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }

        public Boolean getIsInternal() { return isInternal; }
        public void setIsInternal(Boolean internal) { isInternal = internal; }
    }

    public static class TicketCommentDto {
        private Long id;
        private Long ticketId;
        private Long userId;
        private String username;
        private String userFullName;
        private String userRole;
        private String commentText;
        private Boolean isInternal;
        private OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getUserFullName() { return userFullName; }
        public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }

        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }

        public Boolean getIsInternal() { return isInternal; }
        public void setIsInternal(Boolean internal) { isInternal = internal; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class TicketHistoryDto {
        private Long id;
        private Long ticketId;
        private Long changedById;
        private String changedByName;
        private String action;
        private String fieldName;
        private String oldValue;
        private String newValue;
        private OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public Long getChangedById() { return changedById; }
        public void setChangedById(Long changedById) { this.changedById = changedById; }

        public String getChangedByName() { return changedByName; }
        public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }

        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class TicketAttachmentDto {
        private Long id;
        private Long ticketId;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String uploadedByName;
        private OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getUploadedByName() { return uploadedByName; }
        public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class TicketDto {
        private Long id;
        private String ticketCode;
        private String title;
        private String description;
        private Long categoryId;
        private String categoryName;
        private TicketPriority priority;
        private TicketStatus status;
        private Long createdById;
        private String createdByName;
        private String createdByEmail;
        private Long assignedToId;
        private String assignedToName;
        private Long teamId;
        private String teamName;
        private OffsetDateTime dueDate;
        private OffsetDateTime resolvedAt;
        private OffsetDateTime closedAt;
        private String resolutionNotes;
        private Boolean isOverdue;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTicketCode() { return ticketCode; }
        public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public TicketPriority getPriority() { return priority; }
        public void setPriority(TicketPriority priority) { this.priority = priority; }

        public TicketStatus getStatus() { return status; }
        public void setStatus(TicketStatus status) { this.status = status; }

        public Long getCreatedById() { return createdById; }
        public void setCreatedById(Long createdById) { this.createdById = createdById; }

        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

        public String getCreatedByEmail() { return createdByEmail; }
        public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }

        public Long getAssignedToId() { return assignedToId; }
        public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

        public String getAssignedToName() { return assignedToName; }
        public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public OffsetDateTime getDueDate() { return dueDate; }
        public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }

        public OffsetDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

        public OffsetDateTime getClosedAt() { return closedAt; }
        public void setClosedAt(OffsetDateTime closedAt) { this.closedAt = closedAt; }

        public String getResolutionNotes() { return resolutionNotes; }
        public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

        public Boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(Boolean overdue) { isOverdue = overdue; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class TicketDetailDto extends TicketDto {
        private List<TicketCommentDto> comments;
        private List<TicketHistoryDto> history;
        private List<TicketAttachmentDto> attachments;

        public List<TicketCommentDto> getComments() { return comments; }
        public void setComments(List<TicketCommentDto> comments) { this.comments = comments; }

        public List<TicketHistoryDto> getHistory() { return history; }
        public void setHistory(List<TicketHistoryDto> history) { this.history = history; }

        public List<TicketAttachmentDto> getAttachments() { return attachments; }
        public void setAttachments(List<TicketAttachmentDto> attachments) { this.attachments = attachments; }
    }
}
