package com.smartblood.controller;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService users;

    public ProfileController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String viewProfile(Authentication auth, Model model) {
        AppUser user = users.byEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @ModelAttribute AppUser profileData,
            @RequestParam(required = false) String newPassword,
            Authentication auth,
            RedirectAttributes redirect) {
        try {
            String oldEmail = auth.getName();
            AppUser updatedUser = users.updateProfile(oldEmail, profileData, newPassword);

            if (!oldEmail.equalsIgnoreCase(updatedUser.getEmail())) {
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUser.getEmail(),
                        auth.getCredentials(),
                        auth.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            redirect.addFlashAttribute("success", "Profile updated successfully.");
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profile";
        }
    }
}
