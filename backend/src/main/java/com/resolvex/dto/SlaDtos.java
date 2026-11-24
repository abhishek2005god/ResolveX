package com.resolvex.dto;

import com.resolvex.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class SlaDtos {

    public static class SlaPolicyDto {
        private Long id;
        private String name;
        @NotNull(message = "Priority is required")
        private TicketPriority priority;
        @NotNull(message = "Max response hours is required")
        private Integer maxResponseHours;
        @NotNull(message = "Max resolution hours is required")
        private Integer maxResolutionHours;
        @NotNull(message = "Escalation threshold hours is required")
        private Integer escalationThresholdHours;
        private Boolean isActive;
        private OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public TicketPriority getPriority() { return priority; }
        public void setPriority(TicketPriority priority) { this.priority = priority; }

        public Integer getMaxResponseHours() { return maxResponseHours; }
        public void setMaxResponseHours(Integer maxResponseHours) { this.maxResponseHours = maxResponseHours; }

        public Integer getMaxResolutionHours() { return maxResolutionHours; }
        public void setMaxResolutionHours(Integer maxResolutionHours) { this.maxResolutionHours = maxResolutionHours; }

        public Integer getEscalationThresholdHours() { return escalationThresholdHours; }
        public void setEscalationThresholdHours(Integer escalationThresholdHours) { this.escalationThresholdHours = escalationThresholdHours; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean active) { isActive = active; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class SlaEscalationDto {
        private Long id;
        private Long ticketId;
        private String ticketCode;
        private String ticketTitle;
        private Integer escalationLevel;
        private String escalatedToName;
        private String reason;
        private Boolean resolved;
        private OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public String getTicketCode() { return ticketCode; }
        public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

        public String getTicketTitle() { return ticketTitle; }
        public void setTicketTitle(String ticketTitle) { this.ticketTitle = ticketTitle; }

        public Integer getEscalationLevel() { return escalationLevel; }
        public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }

        public String getEscalatedToName() { return escalatedToName; }
        public void setEscalatedToName(String escalatedToName) { this.escalatedToName = escalatedToName; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public Boolean getResolved() { return resolved; }
        public void setResolved(Boolean resolved) { this.resolved = resolved; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }
}
