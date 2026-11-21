package com.smartblood.controller;

import com.smartblood.model.*;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DonorController {
    private final UserService users;
    public DonorController(UserService users) { this.users = users; }

    @GetMapping("/donors")
    public String searchForm(Model model) {
        model.addAttribute("groups", BloodGroup.values());
        return "donors";
    }

    @GetMapping("/donors/search")
    public String search(@RequestParam BloodGroup bloodGroup, @RequestParam String city,
                         @RequestParam(required = false) String area, Model model) {
        model.addAttribute("groups", BloodGroup.values());
        model.addAttribute("donors", users.searchDonors(bloodGroup, city, area));
        model.addAttribute("searched", true);
        return "donors";
    }

    @PostMapping("/profile/availability")
    public String availability(@RequestParam Boolean available, Authentication auth) {
        AppUser user = users.byEmail(auth.getName());
        if (user.hasRole(Role.DONOR)) {
            user.setAvailableToDonate(available);
            users.saveProfile(user);
        }
        return "redirect:/dashboard";
    }
}
