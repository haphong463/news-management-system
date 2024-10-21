package com.windev.user_service.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", nullable = false, unique=true, length=50)
    private String permissionName;

    @Column(length=200)
    private String description;
}
