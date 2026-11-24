package com.resolvex.entity;

import com.resolvex.enums.TicketPriority;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private TicketPriority priority;

    @Column(name = "max_response_hours", nullable = false)
    private Integer maxResponseHours;

    @Column(name = "max_resolution_hours", nullable = false)
    private Integer maxResolutionHours;

    @Column(name = "escalation_threshold_hours", nullable = false)
    private Integer escalationThresholdHours;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (isActive == null) isActive = true;
    }

    public SlaPolicy() {}

    public SlaPolicy(String name, TicketPriority priority, Integer maxResponseHours, Integer maxResolutionHours, Integer escalationThresholdHours) {
        this.name = name;
        this.priority = priority;
        this.maxResponseHours = maxResponseHours;
        this.maxResolutionHours = maxResolutionHours;
        this.escalationThresholdHours = escalationThresholdHours;
    }

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
