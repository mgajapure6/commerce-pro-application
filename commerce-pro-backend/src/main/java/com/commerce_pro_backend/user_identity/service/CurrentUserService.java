package com.commerce_pro_backend.user_identity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commerce_pro_backend.common.exception.ApiException;
import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw ApiException.unauthorized("Unauthenticated request");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> ApiException.unauthorized("Authenticated user not found"));

        return user.getId();
    }
}
