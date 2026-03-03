// SuperAdminAuthorizationFilter.java
package com.commerce_pro_backend.user_identity.service;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.commerce_pro_backend.user_identity.config.SuperAdminConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminAuthorizationFilter extends OncePerRequestFilter {

    private final SuperAdminConfig superAdminConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && isSuperAdmin(authentication)) {
            // Check IP restrictions
            if (!isIpAllowed(request)) {
                log.warn("Super admin access denied from IP: {}", request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied from this IP");
                return;
            }

            // Add super admin headers for frontend
            response.setHeader("X-Super-Admin", "true");
            response.setHeader("X-Config-Editable", "true");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_SUPER_ADMIN"));
    }

    private boolean isIpAllowed(HttpServletRequest request) {
        List<String> allowedRanges = superAdminConfig.getSecurityPolicy().getAllowedIpRanges();
        if (allowedRanges == null || allowedRanges.isEmpty()) {
            return true;
        }

        // String remoteIp = request.getRemoteAddr();
        // IP range checking logic would go here
        // For now, allow all if list is not empty but doesn't match (implement proper
        // CIDR check)
        return true;
    }
}