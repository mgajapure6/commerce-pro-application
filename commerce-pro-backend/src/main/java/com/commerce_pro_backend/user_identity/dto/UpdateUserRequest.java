// UpdateUserRequest.java
package com.commerce_pro_backend.user_identity.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean active;
    private Boolean mfaEnabled;
}



