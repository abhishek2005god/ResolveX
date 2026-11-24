package com.resolvex.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sla_escalations")
public class SlaEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private SlaPolicy slaPolicy;

    @Column(name = "escalation_level", nullable = false)
    private Integer escalationLevel = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_id")
    private User escalatedTo;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (escalationLevel == null) escalationLevel = 1;
        if (resolved == null) resolved = false;
    }

    public SlaEscalation() {}

    public SlaEscalation(Ticket ticket, SlaPolicy slaPolicy, Integer escalationLevel, User escalatedTo, String reason) {
        this.ticket = ticket;
        this.slaPolicy = slaPolicy;
        this.escalationLevel = escalationLevel;
        this.escalatedTo = escalatedTo;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public SlaPolicy getSlaPolicy() { return slaPolicy; }
    public void setSlaPolicy(SlaPolicy slaPolicy) { this.slaPolicy = slaPolicy; }

    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }

    public User getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(User escalatedTo) { this.escalatedTo = escalatedTo; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
