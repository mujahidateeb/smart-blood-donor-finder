package com.smartblood.controller;

import com.smartblood.config.SecurityConfig;
import com.smartblood.model.*;
import com.smartblood.security.CustomUserDetailsService;
import com.smartblood.service.BloodRequestService;
import com.smartblood.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@Import(SecurityConfig.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void editForm_ownerCanAccessForm() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        BloodRequest req = new BloodRequest();
        req.setId(10L);
        req.setPatient(patient);
        req.setBloodGroup(BloodGroup.O_POSITIVE);
        req.setCity("Bengaluru");
        req.setHospitalAddress("City Hospital");
        req.setStatus(RequestStatus.OPEN);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(bloodRequestService.getAuthorized(10L, patient)).thenReturn(req);

        mockMvc.perform(get("/requests/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("request-edit"))
                .andExpect(model().attribute("request", req))
                .andExpect(model().attributeExists("groups"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void updateRequest_successRedirectsToDetail() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);

        mockMvc.perform(post("/requests/10/edit")
                        .with(csrf())
                        .param("bloodGroup", "A_POSITIVE")
                        .param("city", "Mysuru")
                        .param("unitsRequired", "2")
                        .param("hospitalAddress", "Apollo Hospital"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/10"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).updateByUser(eq(10L), eq(patient), any(BloodRequest.class));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void closeRequest_fromDetail_redirectsToDetail() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);

        mockMvc.perform(post("/requests/10/close")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/10"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).closeRequest(10L, patient);
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void closeRequest_fromDashboard_redirectsToDashboard() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);

        mockMvc.perform(post("/requests/10/close")
                        .with(csrf())
                        .param("from", "dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?tab=patient"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).closeRequest(10L, patient);
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void createRequest_withDonorId_sendsRequestToDonorAndRedirects() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        AppUser donor = new AppUser();
        donor.setId(5L);
        donor.setFullName("John Donor");
        donor.setRole(Role.DONOR);

        BloodRequest created = new BloodRequest();
        created.setId(25L);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(userService.byId(5L)).thenReturn(donor);
        when(bloodRequestService.createWithTargetDonor(any(BloodRequest.class), eq(patient), eq(donor))).thenReturn(created);

        mockMvc.perform(post("/requests")
                        .with(csrf())
                        .param("donorId", "5")
                        .param("bloodGroup", "O_POSITIVE")
                        .param("city", "Bengaluru")
                        .param("unitsRequired", "1")
                        .param("hospitalAddress", "Manipal Hospital"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/25"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).createWithTargetDonor(any(BloodRequest.class), eq(patient), eq(donor));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void rejectRequest_donorCanRejectRequest() throws Exception {
        AppUser donor = new AppUser();
        donor.setId(5L);
        donor.setEmail("donor@example.com");
        donor.setRole(Role.DONOR);

        when(userService.byEmail("donor@example.com")).thenReturn(donor);

        mockMvc.perform(post("/requests/15/reject")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?tab=donor"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).reject(15L, donor);
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void status_updateStatusSuccess() throws Exception {
        AppUser donor = new AppUser();
        donor.setId(5L);
        donor.setEmail("donor@example.com");
        donor.setRole(Role.DONOR);

        when(userService.byEmail("donor@example.com")).thenReturn(donor);

        mockMvc.perform(post("/requests/15/status")
                        .with(csrf())
                        .param("status", "COMPLETED")
                        .param("from", "donor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/15?from=donor"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).updateStatus(15L, donor, RequestStatus.COMPLETED);
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void detail_withFromQueryParam_setsBackTab() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        BloodRequest req = new BloodRequest();
        req.setId(10L);
        req.setPatient(patient);
        req.setStatus(RequestStatus.ACCEPTED);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(bloodRequestService.getAuthorized(10L, patient)).thenReturn(req);

        mockMvc.perform(get("/requests/10").param("from", "donor"))
                .andExpect(status().isOk())
                .andExpect(view().name("request-detail"))
                .andExpect(model().attribute("backTab", "donor"));

        mockMvc.perform(get("/requests/10").param("from", "patient"))
                .andExpect(status().isOk())
                .andExpect(view().name("request-detail"))
                .andExpect(model().attribute("backTab", "patient"));
    }
}
