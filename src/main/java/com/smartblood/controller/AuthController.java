package com.smartblood.controller;

import com.smartblood.model.AppUser;
import com.smartblood.model.Role;
import com.smartblood.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
    public String register(@ModelAttribute AppUser user,
                           @RequestParam(name = "selectedRoles", required = false) List<Role> selectedRoles,
                           RedirectAttributes redirect) {
        try {
            if (selectedRoles != null && !selectedRoles.isEmpty()) {
                user.setRoles(new HashSet<>(selectedRoles));
            } else if (user.getRole() != null) {
                user.setRoles(new HashSet<>(Collections.singleton(user.getRole())));
            } else {
                throw new IllegalArgumentException("Please select at least one role (Donor and/or Patient).");
            }
            users.register(user);
            redirect.addFlashAttribute("success", "Registration successful. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }
}
