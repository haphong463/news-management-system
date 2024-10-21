package com.windev.user_service.dto.response;

import com.windev.user_service.entity.Permission;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
public class RoleDto {
    private String roleName;
    private String description;
    private Set<Permission> permissions;
}
