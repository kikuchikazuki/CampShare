package com.example.campshare.web;

import com.example.campshare.user.DuplicateEmailException;
import com.example.campshare.user.SignupForm;
import com.example.campshare.user.SignupService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupPage(@RequestParam(defaultValue = "false") boolean registered, Model model) {
        model.addAttribute("signupForm", new SignupForm());
        model.addAttribute("registered", registered);
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm signupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            signupService.register(signupForm);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            return "signup";
        }

        return "redirect:/signup?registered=true";
    }
}
