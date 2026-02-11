package com.talent.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class AbstractEntity {

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "updated_at")
    private LocalDateTime updatedDatetime;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedDatetime;

    @PrePersist
    protected void onCreate() {
        this.createdDatetime = LocalDateTime.now();
        this.updatedDatetime = LocalDateTime.now();
        this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDatetime = LocalDateTime.now();
    }
}