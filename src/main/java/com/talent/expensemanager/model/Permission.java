package com.talent.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "permissions") // FIXED: Was "roles"
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends AbstractEntity {
    @Id
    @Column(name = "permission_id", nullable = false, unique = true)
    private String permission_id;

    @Column(name = "permission_name", nullable = false)
    private String permission_name;
}