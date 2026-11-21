package com.smartblood.controller;

import com.smartblood.model.*;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final UserService users;
    private final BloodRequestService requests;
    public DashboardController(UserService users, BloodRequestService requests) {
        this.users = users; this.requests = requests;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        AppUser user = users.byEmail(auth.getName());
        model.addAttribute("user", user);
        if (user.getRole() == Role.PATIENT) model.addAttribute("requests", requests.forPatient(user.getId()));
        if (user.getRole() == Role.DONOR) {
            model.addAttribute("openRequests", requests.openRequests());
            model.addAttribute("myAcceptedRequests", requests.forDonor(user.getId()));
        }
        return "dashboard";
    }
}
