package com.fasa.orders.service;

import com.fasa.orders.entity.AppUserEntity;
import com.fasa.orders.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class AppUserAdminService {

    private static final Set<String> ASSIGNABLE_ROLES = new HashSet<>(Arrays.asList("ADMIN", "USER"));

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserService appUserService;

    public AppUserAdminService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, AppUserService appUserService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserService = appUserService;
    }

    @Transactional
    public void createUser(String usernameRaw, String passwordRaw, String roleRaw) {
        String username = normalizeUsername(usernameRaw);
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("That username is already in use.");
        }
        validatePassword(passwordRaw);
        String role = normalizeRole(roleRaw);
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role.");
        }
        AppUserEntity user = new AppUserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(passwordRaw));
        user.setRole(role);
        user.setEnabled(true);
        appUserRepository.save(user);
    }

    /**
     * Change password for the currently authenticated account only (not other users).
     */
    @Transactional
    public void changeOwnPassword(String actorUsername, String currentPasswordRaw, String newPasswordRaw) {
        String target = normalizeUsername(actorUsername);
        if (target.isEmpty()) {
            throw new IllegalArgumentException("Not authenticated.");
        }
        AppUserEntity user = appUserService.getByUsername(target);;
        if (currentPasswordRaw == null || currentPasswordRaw.isEmpty()) {
            throw new IllegalArgumentException("Current password is required.");
        }
        if (!passwordEncoder.matches(currentPasswordRaw, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        validatePassword(newPasswordRaw);
        if (passwordEncoder.matches(newPasswordRaw, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPasswordRaw));
        appUserRepository.save(user);
    }

    private static void validatePassword(String passwordRaw) {
        if (passwordRaw == null || passwordRaw.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }

    private static String normalizeUsername(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    private static String normalizeRole(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "USER";
        }
        return raw.trim().toUpperCase();
    }
}
