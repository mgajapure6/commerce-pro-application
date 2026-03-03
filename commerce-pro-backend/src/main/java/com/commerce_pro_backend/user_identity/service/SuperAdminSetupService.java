package com.commerce_pro_backend.user_identity.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce_pro_backend.user_identity.config.PermissionRegistry;
import com.commerce_pro_backend.user_identity.config.SuperAdminConfig;
import com.commerce_pro_backend.user_identity.entity.Permission;
import com.commerce_pro_backend.user_identity.entity.Role;
import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.entity.UserRoleAssignment;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;
import com.commerce_pro_backend.user_identity.enums.RoleType;
import com.commerce_pro_backend.user_identity.repository.PermissionRepository;
import com.commerce_pro_backend.user_identity.repository.RoleRepository;
import com.commerce_pro_backend.user_identity.repository.UserRepository;
import com.commerce_pro_backend.user_identity.repository.UserRoleAssignmentRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminSetupService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final PermissionRegistry permissionRegistry;
    private final SuperAdminConfig superAdminConfig;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initializeSystem() {
        log.info("Initializing user-identity module...");

        // 1. Sync permissions from registry
        syncPermissions();

        // 2. Create SUPER_ADMIN role if not exists
        Role superAdminRole = createSuperAdminRole();

        // 3. Create default super admin user if configured
        if (superAdminConfig.getDefaultCredentials().isEnabled()) {
            createDefaultSuperAdmin(superAdminRole);
        }

        log.info("User-Identity module initialization complete");
    }

    private void syncPermissions() {
        permissionRegistry.getSystemPermissions().forEach((code, def) -> {
            permissionRepository.findById(code).orElseGet(() -> {
                Permission perm = Permission.builder()
                        .code(code)
                        .name(def.name())
                        .description("System permission: " + def.name())
                        .category(def.category())
                        .isSystem(true)
                        .requiresApproval(def.requiresApproval())
                        .riskLevel(def.riskLevel())
                        .applicableScopes(Set.of("any", "own", "system"))
                        .build();
                return permissionRepository.save(perm);
            });
        });
        log.info("Synchronized {} system permissions", permissionRegistry.getSystemPermissions().size());
    }

    private Role createSuperAdminRole() {
        return roleRepository.findByCode("SUPER_ADMIN")
                .orElseGet(() -> {
                    // Get all system permissions
                    Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

                    Role role = Role.builder()
                            .code("SUPER_ADMIN")
                            .name("Super Administrator")
                            .description("Full system access. Can manage all identity module configurations.")
                            .type(RoleType.SYSTEM)
                            .isSystem(true)
                            .isSuperAdmin(true)
                            .permissions(allPermissions)
                            .requiresMfa(superAdminConfig.getSecurityPolicy().isRequireMfa())
                            .sessionTimeoutMinutes(superAdminConfig.getSecurityPolicy().getSessionTimeoutMinutes())
                            .build();

                    Role saved = roleRepository.save(role);
                    log.info("Created SUPER_ADMIN role with {} permissions", allPermissions.size());
                    return saved;
                });
    }

    private void createDefaultSuperAdmin(Role superAdminRole) {
        String username = superAdminConfig.getDefaultCredentials().getUsername();

        userRepository.findByUsername(username).ifPresentOrElse(
                user -> log.info("Default super admin already exists: {}", username),
                () -> {
                    User superAdmin = User.builder()
                            .username(username)
                            .email(superAdminConfig.getDefaultCredentials().getEmail())
                            .passwordHash(passwordEncoder.encode(
                                    superAdminConfig.getDefaultCredentials().getPassword()))
                            .firstName("Super")
                            .lastName("Admin")
                            .isActive(true)
                            .isEmailVerified(true)
                            .mustChangePassword(
                                    superAdminConfig.getDefaultCredentials().isForcePasswordChangeOnFirstLogin())
                            .passwordChangedAt(LocalDateTime.now())
                            .build();

                    User saved = userRepository.save(superAdmin);

                    // Assign SUPER_ADMIN role
                    UserRoleAssignment assignment = UserRoleAssignment.builder()
                            .user(saved)
                            .role(superAdminRole)
                            .assignedBy("SYSTEM")
                            .assignedAt(LocalDateTime.now())
                            .status(AssignmentStatus.ACTIVE)
                            .build();

                    assignmentRepository.save(assignment);

                    log.warn("============================================================");
                    log.warn("DEFAULT SUPER ADMIN CREATED");
                    log.warn("Username: {}", username);
                    log.warn("IMPORTANT: Change default password immediately!");
                    log.warn("============================================================");
                });
    }

    @Transactional(readOnly = true)
    public boolean canModifySuperAdmin(String modifierId, String targetId) {
        if (modifierId.equals(targetId)) {
            // Check if super admin can modify own role
            return superAdminConfig.getConfigManagement().isCanModifyOwnRole();
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean canDeleteSuperAdmin(String deleterId) {
        // Count active super admins
        long superAdminCount = userRepository.countByRoleCodeAndStatus("SUPER_ADMIN", AssignmentStatus.ACTIVE);

        if (superAdminCount <= superAdminConfig.getConfigManagement().getMinSuperAdmins()) {
            log.warn("Prevented deletion of super admin. Minimum {} required.",
                    superAdminConfig.getConfigManagement().getMinSuperAdmins());
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(User::hasSuperAdminRole)
                .orElse(false);
    }
}