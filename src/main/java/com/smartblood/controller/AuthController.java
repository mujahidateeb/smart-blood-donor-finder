package com.smartblood.controller;

import com.smartblood.model.AppUser;
import com.smartblood.model.Role;
import com.smartblood.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new AppUser());
        model.addAttribute("roles", new Role[]{Role.DONOR, Role.PATIENT});
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute AppUser user, RedirectAttributes redirect) {
        try {
            users.register(user);
            redirect.addFlashAttribute("success", "Registration successful. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }
}
