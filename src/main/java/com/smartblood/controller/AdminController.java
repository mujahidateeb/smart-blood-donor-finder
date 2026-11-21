package com.smartblood.controller;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.BloodRequest;
import com.smartblood.model.RequestStatus;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService users;
    private final BloodRequestService requests;

    public AdminController(UserService users, BloodRequestService requests) {
        this.users = users;
        this.requests = requests;
    }

    @GetMapping
    public String index() { return "redirect:/admin/users"; }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", users.allUsers());
        return "admin-users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", users.byId(id));
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "admin-user-edit";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute AppUser data, RedirectAttributes redirect) {
        try {
            users.updateByAdmin(id, data);
            redirect.addFlashAttribute("success", "User details updated.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("requests", requests.allRequests());
        return "admin-requests";
    }

    @GetMapping("/requests/{id}/edit")
    public String editRequest(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("request", requests.getAuthorized(id, users.byEmail(auth.getName())));
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("statuses", RequestStatus.values());
        return "admin-request-edit";
    }

    @PostMapping("/requests/{id}")
    public String updateRequest(@PathVariable Long id, @ModelAttribute BloodRequest data, RedirectAttributes redirect) {
        try {
            requests.updateByAdmin(id, data);
            redirect.addFlashAttribute("success", "Blood request details updated.");
            return "redirect:/admin/requests";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/requests/" + id + "/edit";
        }
    }
}
