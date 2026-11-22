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
    public String newForm(@RequestParam(required = false) Long donorId,
                          Authentication auth, Model model, RedirectAttributes redirect) {
        if (auth != null) {
            AppUser user = users.byEmail(auth.getName());
            if (!user.hasRole(Role.PATIENT)) {
                user.addRole(Role.PATIENT);
                users.saveProfile(user);
            }
        }
        BloodRequest request = new BloodRequest();
        if (donorId != null) {
            try {
                AppUser donor = users.byId(donorId);
                request.setBloodGroup(donor.getBloodGroup());
                request.setCity(donor.getCity());
                request.setArea(donor.getArea());
                model.addAttribute("targetDonor", donor);
            } catch (Exception ignored) {}
        }
        model.addAttribute("request", request);
        model.addAttribute("groups", BloodGroup.values());
        return "request-form";
    }

    @PostMapping("/requests")
    public String create(@ModelAttribute BloodRequest request,
                         @RequestParam(required = false) Long donorId,
                         Authentication auth, RedirectAttributes redirect) {
        AppUser patient = users.byEmail(auth.getName());
        if (!patient.hasRole(Role.PATIENT)) {
            patient.addRole(Role.PATIENT);
            users.saveProfile(patient);
        }
        if (donorId != null) {
            try {
                AppUser donor = users.byId(donorId);
                BloodRequest saved = requests.createWithTargetDonor(request, patient, donor);
                redirect.addFlashAttribute("success", "Blood request sent to donor " + donor.getFullName() + ". They will review and can accept or reject your request.");
                return "redirect:/requests/" + saved.getId();
            } catch (RuntimeException ex) {
                redirect.addFlashAttribute("error", ex.getMessage());
            }
        }
        requests.create(request, patient);
        redirect.addFlashAttribute("success", "Blood request created.");
        return "redirect:/dashboard?tab=patient";
    }

    @PostMapping("/requests/{id}/accept")
    public String accept(@PathVariable Long id,
                         @RequestParam(required = false) String from,
                         Authentication auth, RedirectAttributes redirect) {
        try {
            requests.accept(id, users.byEmail(auth.getName()));
            redirect.addFlashAttribute("success", "You accepted the request. Patient contact details are now available on the request page.");
            String targetFrom = (from != null && !from.isBlank()) ? from : "donor";
            return "redirect:/requests/" + id + "?from=" + targetFrom;
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/dashboard?tab=donor";
        }
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String from,
                         Authentication auth, RedirectAttributes redirect) {
        try {
            requests.reject(id, users.byEmail(auth.getName()));
            redirect.addFlashAttribute("success", "You rejected the blood request.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        String targetTab = "patient".equalsIgnoreCase(from) ? "patient" : "donor";
        return "redirect:/dashboard?tab=" + targetTab;
    }

    @GetMapping("/requests/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String from,
                         Authentication auth, Model model) {
        AppUser actor = users.byEmail(auth.getName());
        BloodRequest req = requests.getAuthorized(id, actor);
        boolean isOwner = req.getPatient().getId().equals(actor.getId());
        boolean isDonor = req.getAcceptedDonor() != null && req.getAcceptedDonor().getId().equals(actor.getId());
        boolean isTargetDonor = req.getTargetDonor() != null && req.getTargetDonor().getId().equals(actor.getId());
        boolean isAdmin = actor.hasRole(Role.ADMIN);
        model.addAttribute("request", req);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isDonor", isDonor);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", actor);

        String backTab = "patient";
        if ("donor".equalsIgnoreCase(from)) {
            backTab = "donor";
        } else if ("patient".equalsIgnoreCase(from)) {
            backTab = "patient";
        } else if (isDonor || isTargetDonor) {
            backTab = "donor";
        } else if (isOwner) {
            backTab = "patient";
        } else if (actor.hasRole(Role.DONOR) && !actor.hasRole(Role.PATIENT)) {
            backTab = "donor";
        }
        model.addAttribute("backTab", backTab);
        model.addAttribute("from", from);
        return "request-detail";
    }

    @GetMapping("/requests/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(required = false) String from,
                           Authentication auth, Model model, RedirectAttributes redirect) {
        AppUser actor = users.byEmail(auth.getName());
        try {
            BloodRequest request = requests.getAuthorized(id, actor);
            boolean isOwner = request.getPatient().getId().equals(actor.getId());
            if (!isOwner && !actor.hasRole(Role.ADMIN)) {
                redirect.addFlashAttribute("error", "You can only edit your own blood requests.");
                return "redirect:/dashboard?tab=patient";
            }
            model.addAttribute("request", request);
            model.addAttribute("groups", BloodGroup.values());
            model.addAttribute("statuses", RequestStatus.values());
            model.addAttribute("from", from);
            return "request-edit";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/dashboard?tab=patient";
        }
    }

    @PostMapping("/requests/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String from,
                         @ModelAttribute BloodRequest form, Authentication auth, RedirectAttributes redirect) {
        try {
            AppUser actor = users.byEmail(auth.getName());
            requests.updateByUser(id, actor, form);
            redirect.addFlashAttribute("success", "Blood request updated successfully.");
            String redirectUrl = "redirect:/requests/" + id;
            if (from != null && !from.isBlank()) {
                redirectUrl += "?from=" + from;
            }
            return redirectUrl;
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            String redirectUrl = "redirect:/requests/" + id + "/edit";
            if (from != null && !from.isBlank()) {
                redirectUrl += "?from=" + from;
            }
            return redirectUrl;
        }
    }

    @PostMapping("/requests/{id}/close")
    public String close(@PathVariable Long id,
                        @RequestParam(required = false) String from,
                        Authentication auth, RedirectAttributes redirect) {
        try {
            requests.closeRequest(id, users.byEmail(auth.getName()));
            redirect.addFlashAttribute("success", "Blood request closed successfully.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        if ("dashboard".equalsIgnoreCase(from)) {
            return "redirect:/dashboard?tab=patient";
        }
        String redirectUrl = "redirect:/requests/" + id;
        if (from != null && !from.isBlank()) {
            redirectUrl += "?from=" + from;
        }
        return redirectUrl;
    }

    @PostMapping("/requests/{id}/status")
    public String status(@PathVariable Long id,
                         @RequestParam RequestStatus status,
                         @RequestParam(required = false) String from,
                         Authentication auth, RedirectAttributes redirect) {
        try {
            requests.updateStatus(id, users.byEmail(auth.getName()), status);
            redirect.addFlashAttribute("success", "Request status updated.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        String redirectUrl = "redirect:/requests/" + id;
        if (from != null && !from.isBlank()) {
            redirectUrl += "?from=" + from;
        }
        return redirectUrl;
    }
}
