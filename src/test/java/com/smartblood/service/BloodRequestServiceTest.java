package com.smartblood.service;

import com.smartblood.model.*;
import com.smartblood.repository.BloodRequestRepository;
import com.smartblood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceTest {

    @Mock
    private BloodRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    private AppUser dualRoleUser;
    private AppUser anotherDonor;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        dualRoleUser = new AppUser();
        dualRoleUser.setId(10L);
        dualRoleUser.setEmail("dual@example.com");
        dualRoleUser.setFullName("Dual Role User");
        dualRoleUser.setRoles(Set.of(Role.DONOR, Role.PATIENT));
        dualRoleUser.setBloodGroup(BloodGroup.O_POSITIVE);
        dualRoleUser.setCity("Bengaluru");

        anotherDonor = new AppUser();
        anotherDonor.setId(20L);
        anotherDonor.setEmail("donor2@example.com");
        anotherDonor.setFullName("Donor Two");
        anotherDonor.setRole(Role.DONOR);
        anotherDonor.setBloodGroup(BloodGroup.O_POSITIVE);
        anotherDonor.setCity("Bengaluru");

        bloodRequest = new BloodRequest();
        bloodRequest.setId(100L);
        bloodRequest.setPatient(dualRoleUser);
        bloodRequest.setBloodGroup(BloodGroup.O_POSITIVE);
        bloodRequest.setCity("Bengaluru");
        bloodRequest.setHospitalAddress("City Hospital");
        bloodRequest.setStatus(RequestStatus.OPEN);
    }

    @Test
    void createRequest_success() {
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest newReq = new BloodRequest();
        newReq.setBloodGroup(BloodGroup.O_POSITIVE);
        newReq.setCity("Bengaluru");

        BloodRequest created = bloodRequestService.create(newReq, dualRoleUser);
        assertEquals(dualRoleUser, created.getPatient());
        assertEquals(RequestStatus.OPEN, created.getStatus());
        verify(requestRepository).save(newReq);
    }

    @Test
    void acceptRequest_byAnotherDonor_success() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest accepted = bloodRequestService.accept(100L, anotherDonor);
        assertEquals(RequestStatus.ACCEPTED, accepted.getStatus());
        assertEquals(anotherDonor, accepted.getAcceptedDonor());
    }

    @Test
    void acceptRequest_rejectSelfAcceptance() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));

        // Dual-role user created the request; they cannot accept their own request
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bloodRequestService.accept(100L, dualRoleUser)
        );
        assertTrue(ex.getMessage().contains("cannot accept your own blood request"));
    }

    @Test
    void acceptRequest_rejectNonDonor() {
        AppUser patientOnly = new AppUser();
        patientOnly.setId(30L);
        patientOnly.setRole(Role.PATIENT);
        patientOnly.setBloodGroup(BloodGroup.O_POSITIVE);
        patientOnly.setCity("Bengaluru");

        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bloodRequestService.accept(100L, patientOnly)
        );
        assertTrue(ex.getMessage().contains("Only donors can accept"));
    }
}
