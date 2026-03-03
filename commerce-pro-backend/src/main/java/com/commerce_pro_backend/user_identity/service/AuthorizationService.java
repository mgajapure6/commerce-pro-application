// AuthorizationService.java
package com.commerce_pro_backend.user_identity.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.entity.UserRoleAssignment;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;
import com.commerce_pro_backend.user_identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permission) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoleAssignments().stream()
            .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
            .filter(a -> isAssignmentValid(a))
            .flatMap(a -> a.getRole().getAllPermissions().stream())
            .anyMatch(p -> p.getCode().equals(permission));
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permission, String resourceType, String resourceId) {
        // Check base permission
        if (!hasPermission(username, permission)) {
            return false;
        }

        // Additional resource-specific checks would go here
        // For example, checking if user owns the resource, or is in the same department, etc.

        return true;
    }

    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoleAssignments().stream()
            .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
            .filter(a -> isAssignmentValid(a))
            .flatMap(a -> a.getRole().getAllPermissions().stream())
            .map(p -> p.getCode())
            .collect(Collectors.toSet());
    }

    private boolean isAssignmentValid(UserRoleAssignment assignment) {
        LocalDateTime now = LocalDateTime.now();
        if (assignment.getValidFrom() != null && assignment.getValidFrom().isAfter(now)) {
            return false;
        }
        if (assignment.getValidUntil() != null && !assignment.getValidUntil().isAfter(now)) {
            return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoleAssignments().stream()
            .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
            .anyMatch(a -> a.getRole().getIsSuperAdmin());
    }
}