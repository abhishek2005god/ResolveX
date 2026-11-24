package com.resolvex.entity;

import com.resolvex.enums.TicketPriority;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_priority", nullable = false, length = 20)
    private TicketPriority defaultPriority = TicketPriority.MEDIUM;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (active == null) active = true;
        if (defaultPriority == null) defaultPriority = TicketPriority.MEDIUM;
    }

    public Category() {}

    public Category(String name, String description, Department department, TicketPriority defaultPriority) {
        this.name = name;
        this.description = description;
        this.department = department;
        this.defaultPriority = defaultPriority;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public TicketPriority getDefaultPriority() { return defaultPriority; }
    public void setDefaultPriority(TicketPriority defaultPriority) { this.defaultPriority = defaultPriority; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
