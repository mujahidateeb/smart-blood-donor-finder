package com.smartblood.controller;

import com.smartblood.model.*;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {
    private final UserService users;
    private final BloodRequestService requests;
    public DashboardController(UserService users, BloodRequestService requests) {
        this.users = users; this.requests = requests;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String tab, Authentication auth, Model model) {
        AppUser user = users.byEmail(auth.getName());
        model.addAttribute("user", user);

        model.addAttribute("requests", requests.forPatient(user.getId()));
        model.addAttribute("openRequests", requests.openRequests());
        model.addAttribute("myAcceptedRequests", requests.forDonor(user.getId()));

        String activeTab = tab;
        if (activeTab == null || activeTab.trim().isEmpty()) {
            if (user.hasRole(Role.DONOR)) {
                activeTab = "donor";
            } else if (user.hasRole(Role.PATIENT)) {
                activeTab = "patient";
            } else {
                activeTab = "donor";
            }
        } else {
            activeTab = activeTab.trim().toLowerCase();
        }
        model.addAttribute("activeTab", activeTab);

        return "dashboard";
    }
}
