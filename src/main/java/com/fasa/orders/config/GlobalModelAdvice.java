package com.fasa.orders.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.fasa.orders.controller.UserAdminController.getRoles;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void exposeCsrf(HttpServletRequest request, Model model, Authentication authentication) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            List <String> roles = getRoles(authentication);
            model.addAttribute("isAdmin", roles.contains("ROLE_ADMIN"));
            model.addAttribute("isSuperAdmin", roles.contains("ROLE_SUPER_ADMIN")
                                                    && roles.contains("ROLE_ADMIN"));
            model.addAttribute("_csrf", token);
        }
    }
}
