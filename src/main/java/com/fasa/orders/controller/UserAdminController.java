package com.fasa.orders.controller;

import com.fasa.orders.service.AppUserAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
public class UserAdminController {

    private final AppUserAdminService appUserAdminService;

    public UserAdminController(AppUserAdminService appUserAdminService) {
        this.appUserAdminService = appUserAdminService;
    }

    @GetMapping("/users/new")
    public String legacyCreateUserPath() {
        return "redirect:/users";
    }

    @GetMapping("/users")
    public String userManagement(Model model, Authentication authentication) {
        model.addAttribute("sidebarActive", "users");
        boolean admin = isAdmin(authentication);
        model.addAttribute("isAdmin", admin);
        model.addAttribute(
                "usernames",
                admin ? appUserAdminService.listUsernamesOrdered() : Collections.emptyList());
        model.addAttribute("currentUsername", authentication != null ? authentication.getName() : "");
        return "users-manage";
    }

    @PostMapping("/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "USER") String role,
            RedirectAttributes redirectAttributes) {
        try {
            appUserAdminService.createUser(username, password, role);
            redirectAttributes.addFlashAttribute("flashMessage", "User created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/users/reset-password")
    public String resetPassword(
            @RequestParam String targetUsername,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        boolean admin = isAdmin(authentication);
        String actor = authentication != null ? authentication.getName() : "";
        try {
            appUserAdminService.resetPassword(actor, admin, targetUsername, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("flashMessage", "Password updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/users";
    }

    private static boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
