package com.fasa.orders.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserAdminController {

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("sidebarActive", "users");
        return "users-new";
    }
}
