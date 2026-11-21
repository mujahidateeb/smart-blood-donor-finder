package com.smartblood.controller;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.Role;
import com.smartblood.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        model.addAttribute("allRoles", new Role[]{Role.DONOR, Role.PATIENT});
        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @ModelAttribute AppUser profileData,
            @RequestParam(name = "selectedRoles", required = false) List<Role> selectedRoles,
            @RequestParam(name = "rolesSubmitted", required = false, defaultValue = "false") boolean rolesSubmitted,
            @RequestParam(required = false) String newPassword,
            Authentication auth,
            RedirectAttributes redirect) {
        try {
            String oldEmail = auth.getName();
            AppUser updatedUser;
            if (rolesSubmitted) {
                if (selectedRoles == null || selectedRoles.isEmpty()) {
                    throw new IllegalArgumentException("Please select at least one role (Donor and/or Patient).");
                }
                Set<Role> rolesSet = new HashSet<>(selectedRoles);
                updatedUser = users.updateProfile(oldEmail, profileData, rolesSet, newPassword);
            } else if (selectedRoles != null && !selectedRoles.isEmpty()) {
                Set<Role> rolesSet = new HashSet<>(selectedRoles);
                updatedUser = users.updateProfile(oldEmail, profileData, rolesSet, newPassword);
            } else {
                updatedUser = users.updateProfile(oldEmail, profileData, newPassword);
            }

            refreshSecurityContext(updatedUser, auth);

            redirect.addFlashAttribute("success", "Profile updated successfully.");
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/roles/add")
    public String addRole(@RequestParam Role role,
                          @RequestParam(required = false) String redirectUrl,
                          Authentication auth,
                          RedirectAttributes redirect) {
        try {
            AppUser updated = users.addRoleToUser(auth.getName(), role);
            refreshSecurityContext(updated, auth);

            redirect.addFlashAttribute("success", role.name() + " role activated successfully! You now have access to " + role.name() + " features.");
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                return "redirect:" + redirectUrl.trim();
            }
            return "redirect:/dashboard?tab=" + role.name().toLowerCase();
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return (redirectUrl != null && !redirectUrl.trim().isEmpty())
                    ? "redirect:" + redirectUrl.trim()
                    : "redirect:/dashboard";
        }
    }

    @PostMapping("/roles/remove")
    public String removeRole(@RequestParam Role role,
                             @RequestParam(required = false) String redirectUrl,
                             Authentication auth,
                             RedirectAttributes redirect) {
        try {
            AppUser updated = users.removeRoleFromUser(auth.getName(), role);
            refreshSecurityContext(updated, auth);

            redirect.addFlashAttribute("success", role.name() + " role removed.");
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                return "redirect:" + redirectUrl.trim();
            }
            return "redirect:/dashboard";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return (redirectUrl != null && !redirectUrl.trim().isEmpty())
                    ? "redirect:" + redirectUrl.trim()
                    : "redirect:/dashboard";
        }
    }

    private void refreshSecurityContext(AppUser updatedUser, Authentication currentAuth) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role r : updatedUser.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name()));
        }
        if (updatedUser.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + updatedUser.getRole().name()));
        }

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser.getEmail(),
                currentAuth != null ? currentAuth.getCredentials() : null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
