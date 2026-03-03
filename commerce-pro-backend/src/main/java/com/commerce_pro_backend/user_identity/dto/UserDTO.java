// UserDTO.java
package com.commerce_pro_backend.user_identity.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private boolean active;
    private boolean emailVerified;
    private boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Set<String> roleCodes;
}