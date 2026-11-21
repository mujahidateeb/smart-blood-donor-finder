package com.smartblood.service;

import com.smartblood.model.*;
import com.smartblood.repository.BloodRequestRepository;
import com.smartblood.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BloodRequestService {
    private final BloodRequestRepository requests;
    private final UserRepository users;
    public BloodRequestService(BloodRequestRepository requests, UserRepository users) {
        this.requests = requests; this.users = users;
    }

    public BloodRequest create(BloodRequest request, AppUser patient) {
        request.setPatient(patient);
        request.setStatus(RequestStatus.OPEN);
        return requests.save(request);
    }

    public List<BloodRequest> forPatient(Long id) { return requests.findByPatientIdOrderByCreatedAtDesc(id); }
    public List<BloodRequest> openRequests() { return requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN); }
    public List<BloodRequest> forDonor(Long id) { return requests.findByAcceptedDonorIdOrderByCreatedAtDesc(id); }
    public List<BloodRequest> allRequests() { return requests.findAll(); }

    @Transactional
    public BloodRequest updateByAdmin(Long id, BloodRequest data) {
        BloodRequest request = requests.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found."));
        if (data.getBloodGroup() == null) throw new IllegalArgumentException("Blood group is required.");
        if (data.getCity() == null || data.getCity().trim().isEmpty()) throw new IllegalArgumentException("City cannot be empty.");
        if (data.getHospitalAddress() == null || data.getHospitalAddress().trim().isEmpty()) throw new IllegalArgumentException("Hospital address cannot be empty.");
        if (data.getUnitsRequired() == null || data.getUnitsRequired() < 1) throw new IllegalArgumentException("Units required must be at least 1.");
        if (data.getStatus() == null) throw new IllegalArgumentException("Request status is required.");
        request.setBloodGroup(data.getBloodGroup());
        request.setCity(data.getCity().trim());
        request.setArea(data.getArea() != null ? data.getArea().trim() : null);
        request.setUnitsRequired(data.getUnitsRequired());
        request.setHospitalAddress(data.getHospitalAddress().trim());
        request.setMessage(data.getMessage() != null ? data.getMessage().trim() : null);
        request.setNeededBy(data.getNeededBy());
        request.setStatus(data.getStatus());
        return requests.save(request);
    }

    @Transactional
    public BloodRequest accept(Long requestId, AppUser donor) {
        BloodRequest request = requests.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Request not found."));
        if (!donor.hasRole(Role.DONOR)) throw new IllegalArgumentException("Only donors can accept requests.");
        if (request.getPatient().getId().equals(donor.getId())) throw new IllegalArgumentException("You cannot accept your own blood request.");
        if (request.getStatus() != RequestStatus.OPEN) throw new IllegalArgumentException("This request is no longer open.");
        if (donor.getBloodGroup() != request.getBloodGroup()) throw new IllegalArgumentException("Your blood group does not match this request.");
        if (!donor.getCity().equalsIgnoreCase(request.getCity())) throw new IllegalArgumentException("This request is outside your registered city.");
        request.setAcceptedDonor(donor);
        request.setStatus(RequestStatus.ACCEPTED);
        return requests.save(request);
    }

    @Transactional
    public BloodRequest updateStatus(Long requestId, AppUser actor, RequestStatus status) {
        BloodRequest request = requests.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Request not found."));
        boolean owner = request.getPatient().getId().equals(actor.getId());
        boolean assignedDonor = request.getAcceptedDonor() != null && request.getAcceptedDonor().getId().equals(actor.getId());
        if (!owner && !assignedDonor && !actor.hasRole(Role.ADMIN)) throw new SecurityException("Not authorized.");
        if (status == RequestStatus.ACCEPTED) throw new IllegalArgumentException("Use the accept action.");
        request.setStatus(status);
        return requests.save(request);
    }

    public BloodRequest getAuthorized(Long id, AppUser actor) {
        BloodRequest request = requests.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found."));
        boolean owner = request.getPatient().getId().equals(actor.getId());
        boolean assignedDonor = request.getAcceptedDonor() != null && request.getAcceptedDonor().getId().equals(actor.getId());
        if (!owner && !assignedDonor && !actor.hasRole(Role.ADMIN)) throw new SecurityException("Not authorized.");
        return request;
    }
}
