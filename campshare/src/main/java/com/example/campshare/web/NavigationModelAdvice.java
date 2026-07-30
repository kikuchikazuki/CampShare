package com.example.campshare.web;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {

    @ModelAttribute
    void addNavigationState(Authentication authentication, Model model) {
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        boolean admin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("navAuthenticated", authenticated);
        model.addAttribute("navEmail", authenticated ? authentication.getName() : null);
        model.addAttribute("navAdmin", admin);
    }
}
