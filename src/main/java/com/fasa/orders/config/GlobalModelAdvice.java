package com.fasa.orders.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

import static com.fasa.orders.controller.UserAdminController.isAdmin;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void exposeCsrf(HttpServletRequest request, Model model, Authentication authentication) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            boolean admin = isAdmin(authentication);
            model.addAttribute("isAdmin", admin);
            model.addAttribute("_csrf", token);
        }
    }
}
