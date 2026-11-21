package com.smartblood.controller;

import com.smartblood.model.*;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestController {
    private final BloodRequestService requests;
    private final UserService users;
    public RequestController(BloodRequestService requests, UserService users) {
        this.requests = requests; this.users = users;
    }

    @GetMapping("/requests/new")
    public String newForm(Model model) {
        model.addAttribute("request", new BloodRequest());
        model.addAttribute("groups", BloodGroup.values());
        return "request-form";
    }

    @PostMapping("/requests")
    public String create(@ModelAttribute BloodRequest request, Authentication auth, RedirectAttributes redirect) {
        AppUser patient = users.byEmail(auth.getName());
        if (patient.getRole() != Role.PATIENT) return "redirect:/access-denied";
        requests.create(request, patient);
        redirect.addFlashAttribute("success", "Blood request created.");
        return "redirect:/dashboard";
    }

    @PostMapping("/requests/{id}/accept")
    public String accept(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            requests.accept(id, users.byEmail(auth.getName()));
            redirect.addFlashAttribute("success", "You accepted the request. Patient contact details are now available on the request page.");
            return "redirect:/requests/" + id;
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/requests/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        AppUser actor = users.byEmail(auth.getName());
        model.addAttribute("request", requests.getAuthorized(id, actor));
        return "request-detail";
    }

    @PostMapping("/requests/{id}/status")
    public String status(@PathVariable Long id, @RequestParam RequestStatus status,
                         Authentication auth, RedirectAttributes redirect) {
        try {
            requests.updateStatus(id, users.byEmail(auth.getName()), status);
            redirect.addFlashAttribute("success", "Request status updated.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/requests/" + id;
    }
}
