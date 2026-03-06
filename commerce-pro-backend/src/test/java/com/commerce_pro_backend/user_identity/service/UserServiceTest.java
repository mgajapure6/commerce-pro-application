package com.commerce_pro_backend.user_identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.commerce_pro_backend.common.exception.ApiException;
import com.commerce_pro_backend.user_identity.dto.CreateUserRequest;
import com.commerce_pro_backend.user_identity.dto.RoleAssignmentRequest;
import com.commerce_pro_backend.user_identity.dto.UserDTO;
import com.commerce_pro_backend.user_identity.entity.Role;
import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.entity.UserRoleAssignment;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;
import com.commerce_pro_backend.user_identity.enums.RoleType;
import com.commerce_pro_backend.user_identity.repository.RoleRepository;
import com.commerce_pro_backend.user_identity.repository.UserRepository;
import com.commerce_pro_backend.user_identity.repository.UserRoleAssignmentRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserRoleAssignmentRepository assignmentRepository;
    @Mock AuditService auditService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @Mock EmailNotificationService emailNotificationService;

    @InjectMocks UserService userService;

    private User existingUser;
    private Role editorRole;

    @BeforeEach
    void setUp() {
        editorRole = Role.builder()
            .id("role-1")
            .code("EDITOR")
            .name("Editor")
            .type(RoleType.GLOBAL)
            .isSystem(false)
            .isSuperAdmin(false)
            .build();

        existingUser = User.builder()
            .id("user-1")
            .username("alice")
            .email("alice@example.com")
            .passwordHash("$encoded$")
            .isActive(true)
            .mfaEnabled(false)
            .failedLoginAttempts(0)
            .roleAssignments(new HashSet<>())
            .build();
    }

    // -------------------------------------------------------------------------
    // Issue 9: No manual UUID in createUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createUser() should save user without manually setting ID (let JPA generate)")
    void createUser_shouldNotSetIdManually() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$encoded$");

        User savedUser = User.builder()
            .id("generated-uuid-by-jpa")
            .username("newuser")
            .email("new@example.com")
            .passwordHash("$encoded$")
            .isActive(true)
            .isEmailVerified(false)
            .mfaEnabled(false)
            .failedLoginAttempts(0)
            .roleAssignments(new HashSet<>())
            .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findById(any())).thenReturn(Optional.of(
            User.builder().username("admin").build()));

        CreateUserRequest req = CreateUserRequest.builder()
            .username("newuser").email("new@example.com").password("password").build();

        UserDTO dto = userService.createUser(req, "admin-id");
        assertThat(dto.getId()).isEqualTo("generated-uuid-by-jpa");

        // Verify the saved entity had NO manually set id (id is null before persist)
        verify(userRepository).save(argThat(user -> user.getId() == null));
    }

    @Test
    @DisplayName("createUser() should throw conflict on duplicate username")
    void createUser_shouldThrowOnDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        CreateUserRequest req = CreateUserRequest.builder()
            .username("alice").email("other@example.com").password("pass").build();

        assertThatThrownBy(() -> userService.createUser(req, "admin"))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Username");
    }

    @Test
    @DisplayName("createUser() should send welcome email")
    void createUser_shouldSendWelcomeEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$encoded$");

        User saved = User.builder()
            .id("id-1").username("newuser").email("new@example.com")
            .isActive(true).isEmailVerified(false).mfaEnabled(false)
            .failedLoginAttempts(0).roleAssignments(new HashSet<>()).build();
        when(userRepository.save(any())).thenReturn(saved);
        when(userRepository.findById(any())).thenReturn(Optional.of(
            User.builder().username("admin").build()));

        CreateUserRequest req = CreateUserRequest.builder()
            .username("newuser").email("new@example.com").password("myPassword123").build();
        userService.createUser(req, "admin-id");

        verify(emailNotificationService).sendWelcomeEmail("new@example.com", "newuser", "myPassword123");
    }

    // -------------------------------------------------------------------------
    // Issue 6: Duplicate role assignment prevention
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("assignRole() should throw conflict when role is already actively assigned")
    void assignRole_shouldThrowOnDuplicateActiveAssignment() {
        // Set up an existing ACTIVE assignment for EDITOR role
        UserRoleAssignment existingAssignment = UserRoleAssignment.builder()
            .id("assign-1")
            .user(existingUser)
            .role(editorRole)
            .status(AssignmentStatus.ACTIVE)
            .assignedAt(LocalDateTime.now())
            .build();
        existingUser.getRoleAssignments().add(existingAssignment);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByCode("EDITOR")).thenReturn(Optional.of(editorRole));

        RoleAssignmentRequest req = RoleAssignmentRequest.builder()
            .roleCode("EDITOR").build();

        assertThatThrownBy(() -> userService.assignRole("user-1", req, "admin-id"))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("already");
    }

    @Test
    @DisplayName("assignRole() should succeed when same role was previously revoked")
    void assignRole_shouldSucceedForRevokedRole() {
        // REVOKED assignment — should be allowed to re-assign
        UserRoleAssignment revokedAssignment = UserRoleAssignment.builder()
            .id("assign-1")
            .user(existingUser)
            .role(editorRole)
            .status(AssignmentStatus.REVOKED)
            .assignedAt(LocalDateTime.now().minusDays(1))
            .build();
        existingUser.getRoleAssignments().add(revokedAssignment);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByCode("EDITOR")).thenReturn(Optional.of(editorRole));
        when(userRepository.findById("admin-id")).thenReturn(Optional.of(
            User.builder().username("admin").build()));

        RoleAssignmentRequest req = RoleAssignmentRequest.builder()
            .roleCode("EDITOR").build();

        // Should NOT throw — revoked roles can be re-assigned
        userService.assignRole("user-1", req, "admin-id");
        verify(userRepository).save(existingUser);
    }

    // -------------------------------------------------------------------------
    // resetPassword — sends email when notifyUser=true
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("resetPassword() should send email when notifyUser is true")
    void resetPassword_shouldSendEmailWhenRequested() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(any())).thenReturn("$encoded$");
        when(userRepository.findById("admin-id")).thenReturn(Optional.of(
            User.builder().username("admin").build()));

        userService.resetPassword("user-1", "admin-id", true);

        verify(emailNotificationService).sendPasswordResetNotification(
            eq(existingUser.getEmail()), eq(existingUser.getUsername()), anyString());
    }

    @Test
    @DisplayName("resetPassword() should NOT send email when notifyUser is false")
    void resetPassword_shouldNotSendEmailWhenNotRequested() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(any())).thenReturn("$encoded$");
        when(userRepository.findById("admin-id")).thenReturn(Optional.of(
            User.builder().username("admin").build()));

        userService.resetPassword("user-1", "admin-id", false);

        verifyNoInteractions(emailNotificationService);
    }
}
