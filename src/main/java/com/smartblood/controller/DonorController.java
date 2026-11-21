package com.smartblood.controller;

import com.smartblood.model.*;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DonorController {
    private final UserService users;
    private final BloodRequestService requests;

    public DonorController(UserService users, BloodRequestService requests) {
        this.users = users;
        this.requests = requests;
    }

    @GetMapping("/donors")
    public String searchForm(Authentication auth, Model model) {
        model.addAttribute("groups", BloodGroup.values());
        if (auth != null) {
            model.addAttribute("currentUser", users.byEmail(auth.getName()));
        }
        return "donors";
    }

    @GetMapping("/donors/search")
    public String search(@RequestParam BloodGroup bloodGroup, @RequestParam String city,
                         @RequestParam(required = false) String area, Authentication auth, Model model) {
        model.addAttribute("groups", BloodGroup.values());
        model.addAttribute("donors", users.searchDonors(bloodGroup, city, area));
        model.addAttribute("searched", true);
        model.addAttribute("selectedGroup", bloodGroup);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedArea", area);
        if (auth != null) {
            model.addAttribute("currentUser", users.byEmail(auth.getName()));
        }
        return "donors";
    }

    @PostMapping({"/donors/{donorId}/request", "/donors/{donorId}/accept"})
    public String requestDonor(@PathVariable Long donorId,
                               @RequestParam(required = false) Long requestId,
                               Authentication auth,
                               RedirectAttributes redirect) {
        if (auth == null) {
            return "redirect:/login";
        }
        AppUser actor = users.byEmail(auth.getName());
        AppUser donor = users.byId(donorId);

        if (actor.getId().equals(donor.getId())) {
            redirect.addFlashAttribute("error", "You cannot send a blood request to yourself.");
            return "redirect:/donors";
        }

        if (!actor.hasRole(Role.PATIENT)) {
            actor.addRole(Role.PATIENT);
            users.saveProfile(actor);
        }

        List<BloodRequest> openRequests = requests.forPatient(actor.getId()).stream()
                .filter(r -> r.getStatus() == RequestStatus.OPEN)
                .collect(Collectors.toList());

        BloodRequest targetRequest = null;
        if (requestId != null) {
            targetRequest = requests.getAuthorized(requestId, actor);
        } else if (!openRequests.isEmpty()) {
            targetRequest = openRequests.stream()
                    .filter(r -> r.getBloodGroup() == donor.getBloodGroup())
                    .findFirst()
                    .orElse(null);
        }

        if (targetRequest != null) {
            try {
                requests.sendRequestToDonor(targetRequest.getId(), donor, actor);
                redirect.addFlashAttribute("success", "Blood request sent to donor " + donor.getFullName() + ". They will review and can accept or reject your request.");
                return "redirect:/requests/" + targetRequest.getId();
            } catch (RuntimeException ex) {
                redirect.addFlashAttribute("error", ex.getMessage());
                return "redirect:/donors";
            }
        } else {
            redirect.addFlashAttribute("info", "Please enter hospital details to send a request to donor " + donor.getFullName() + ".");
            return "redirect:/requests/new?donorId=" + donor.getId();
        }
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
