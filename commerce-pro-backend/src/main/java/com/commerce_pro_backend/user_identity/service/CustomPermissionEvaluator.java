// PermissionEvaluator.java
package com.commerce_pro_backend.user_identity.service;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        String perm = (String) permission;
        
        return authorizationService.hasPermission(username, perm);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        String perm = (String) permission;
        
        // Check resource-specific permission
        return authorizationService.hasPermission(username, perm, targetType, targetId.toString());
    }
}