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

    @Test
    void updateByUser_success() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest updatedData = new BloodRequest();
        updatedData.setBloodGroup(BloodGroup.A_POSITIVE);
        updatedData.setCity("Mysuru");
        updatedData.setArea("Vijayanagar");
        updatedData.setUnitsRequired(3);
        updatedData.setHospitalAddress("Apollo BGS Hospital");
        updatedData.setMessage("Need 3 units urgently");

        BloodRequest result = bloodRequestService.updateByUser(100L, dualRoleUser, updatedData);
        assertEquals(BloodGroup.A_POSITIVE, result.getBloodGroup());
        assertEquals("Mysuru", result.getCity());
        assertEquals("Vijayanagar", result.getArea());
        assertEquals(3, result.getUnitsRequired());
        assertEquals("Apollo BGS Hospital", result.getHospitalAddress());
        assertEquals("Need 3 units urgently", result.getMessage());
        verify(requestRepository).save(bloodRequest);
    }

    @Test
    void updateByUser_unauthorized() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));

        AppUser stranger = new AppUser();
        stranger.setId(99L);
        stranger.setEmail("stranger@example.com");

        BloodRequest updatedData = new BloodRequest();
        updatedData.setBloodGroup(BloodGroup.A_POSITIVE);
        updatedData.setCity("Mysuru");
        updatedData.setHospitalAddress("Apollo Hospital");
        updatedData.setUnitsRequired(2);

        assertThrows(SecurityException.class, () ->
                bloodRequestService.updateByUser(100L, stranger, updatedData)
        );
    }

    @Test
    void closeRequest_success() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest closed = bloodRequestService.closeRequest(100L, dualRoleUser);
        assertEquals(RequestStatus.CLOSED, closed.getStatus());
        verify(requestRepository).save(bloodRequest);
    }

    @Test
    void closeRequest_unauthorized() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));

        AppUser stranger = new AppUser();
        stranger.setId(99L);
        stranger.setEmail("stranger@example.com");

        assertThrows(SecurityException.class, () ->
                bloodRequestService.closeRequest(100L, stranger)
        );
    }

    @Test
    void sendRequestToDonor_success() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest sent = bloodRequestService.sendRequestToDonor(100L, anotherDonor, dualRoleUser);
        assertEquals(RequestStatus.OPEN, sent.getStatus());
        assertEquals(anotherDonor, sent.getTargetDonor());
        verify(requestRepository).save(bloodRequest);
    }

    @Test
    void sendRequestToDonor_rejectNonDonor() {
        AppUser patientOnly = new AppUser();
        patientOnly.setId(30L);
        patientOnly.setRole(Role.PATIENT);
        patientOnly.setBloodGroup(BloodGroup.O_POSITIVE);

        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bloodRequestService.sendRequestToDonor(100L, patientOnly, dualRoleUser)
        );
        assertTrue(ex.getMessage().contains("not registered as a donor"));
    }

    @Test
    void createWithTargetDonor_success() {
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest req = new BloodRequest();
        req.setBloodGroup(BloodGroup.O_POSITIVE);
        req.setCity("Bengaluru");
        req.setHospitalAddress("City Hospital");
        req.setUnitsRequired(1);

        BloodRequest created = bloodRequestService.createWithTargetDonor(req, dualRoleUser, anotherDonor);
        assertEquals(dualRoleUser, created.getPatient());
        assertEquals(anotherDonor, created.getTargetDonor());
        assertEquals(RequestStatus.OPEN, created.getStatus());
        verify(requestRepository).save(req);
    }

    @Test
    void rejectRequest_success() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(bloodRequest));
        when(requestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequest rejected = bloodRequestService.reject(100L, anotherDonor);
        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
        verify(requestRepository).save(bloodRequest);
    }

    @Test
    void matchedRequestsForDonor_returnsOnlyMatchingBloodAndCity() {
        BloodRequest matchedReq = new BloodRequest();
        matchedReq.setId(101L);
        matchedReq.setPatient(dualRoleUser);
        matchedReq.setBloodGroup(BloodGroup.O_POSITIVE);
        matchedReq.setCity("Bengaluru");
        matchedReq.setStatus(RequestStatus.OPEN);

        BloodRequest mismatchedGroup = new BloodRequest();
        mismatchedGroup.setId(102L);
        mismatchedGroup.setPatient(dualRoleUser);
        mismatchedGroup.setBloodGroup(BloodGroup.B_POSITIVE);
        mismatchedGroup.setCity("Bengaluru");
        mismatchedGroup.setStatus(RequestStatus.OPEN);

        BloodRequest mismatchedCity = new BloodRequest();
        mismatchedCity.setId(103L);
        mismatchedCity.setPatient(dualRoleUser);
        mismatchedCity.setBloodGroup(BloodGroup.O_POSITIVE);
        mismatchedCity.setCity("Mysuru");
        mismatchedCity.setStatus(RequestStatus.OPEN);

        when(requestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN))
                .thenReturn(java.util.List.of(matchedReq, mismatchedGroup, mismatchedCity));

        java.util.List<BloodRequest> result = bloodRequestService.matchedRequestsForDonor(anotherDonor);
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getId());
    }
}
