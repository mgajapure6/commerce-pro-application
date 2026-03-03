// CustomUserDetailsService.java
package com.commerce_pro_backend.user_identity.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;
import com.commerce_pro_backend.user_identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User is deactivated: " + username);
        }
        
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new UsernameNotFoundException("Account is locked: " + username);
        }

        var authorities = user.getRoleAssignments().stream()
            .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
            .flatMap(a -> a.getRole().getAllPermissions().stream())
            .map(p -> new SimpleGrantedAuthority(p.getCode()))
            .collect(Collectors.toSet());
        
        // Add role authorities
        user.getRoleAssignments().stream()
            .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE)
            .forEach(a -> authorities.add(new SimpleGrantedAuthority("ROLE_" + a.getRole().getCode())));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            user.getIsActive(),
            true,
            true,
            user.getLockedUntil() == null || user.getLockedUntil().isBefore(java.time.LocalDateTime.now()),
            authorities
        );
    }
}