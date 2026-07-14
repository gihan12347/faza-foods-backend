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

import java.util.ArrayList;
import java.util.List;

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
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String actor = authentication != null ? authentication.getName() : "";
        try {
            appUserAdminService.changeOwnPassword(actor, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("flashMessage", "Password updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/users";
    }

    //TODO: need to implement multiple role support feature
    public static List<String> getRoles(Authentication authentication) {
        List<String> roles = new ArrayList<>();
        if (authentication == null) {
            return roles;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }
}
