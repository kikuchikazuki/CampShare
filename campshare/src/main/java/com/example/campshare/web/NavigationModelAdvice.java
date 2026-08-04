package com.example.campshare.web;

import com.example.campshare.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {

    private final ObjectProvider<UserRepository> userRepositories;

    public NavigationModelAdvice(ObjectProvider<UserRepository> userRepositories) {
        this.userRepositories = userRepositories;
    }

    @ModelAttribute
    void addNavigationState(Authentication authentication, Model model) {
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        boolean admin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("navAuthenticated", authenticated);
        String email = authenticated ? authentication.getName() : null;
        UserRepository userRepository = userRepositories.getIfAvailable();
        String displayName = authenticated && userRepository != null
                ? userRepository.findByEmail(email).map(user -> user.getDisplayName()).orElse(email)
                : email;
        model.addAttribute("navEmail", email);
        model.addAttribute("navDisplayName", displayName);
        model.addAttribute("navAdmin", admin);
    }
}
