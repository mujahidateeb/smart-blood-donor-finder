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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DonorController.class)
@Import(SecurityConfig.class)
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void availabilityToggle_updatesAvailabilityAndRedirects() throws Exception {
        AppUser donor = new AppUser();
        donor.setEmail("donor@example.com");
        donor.setRole(Role.DONOR);
        donor.setAvailableToDonate(true);

        when(userService.byEmail("donor@example.com")).thenReturn(donor);

        mockMvc.perform(post("/profile/availability")
                        .with(csrf())
                        .param("available", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        org.junit.jupiter.api.Assertions.assertFalse(donor.getAvailableToDonate());
        verify(userService).saveProfile(donor);
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void searchDonors_rendersResultsWithAcceptButton() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        AppUser donor = new AppUser();
        donor.setId(2L);
        donor.setFullName("Donor Jane");
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setCity("Bengaluru");
        donor.setRole(Role.DONOR);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(userService.searchDonors(BloodGroup.O_POSITIVE, "Bengaluru", null)).thenReturn(List.of(donor));

        mockMvc.perform(get("/donors/search")
                        .param("bloodGroup", "O_POSITIVE")
                        .param("city", "Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(view().name("donors"))
                .andExpect(model().attributeExists("donors"))
                .andExpect(model().attribute("searched", true));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void requestDonor_withMatchingOpenRequest_sendsRequestAndRedirectsToDetails() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        AppUser donor = new AppUser();
        donor.setId(2L);
        donor.setFullName("Donor Jane");
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setRole(Role.DONOR);

        BloodRequest openReq = new BloodRequest();
        openReq.setId(10L);
        openReq.setPatient(patient);
        openReq.setBloodGroup(BloodGroup.O_POSITIVE);
        openReq.setStatus(RequestStatus.OPEN);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(userService.byId(2L)).thenReturn(donor);
        when(bloodRequestService.forPatient(1L)).thenReturn(List.of(openReq));

        mockMvc.perform(post("/donors/2/request")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/10"))
                .andExpect(flash().attributeExists("success"));

        verify(bloodRequestService).sendRequestToDonor(10L, donor, patient);
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void requestDonor_withoutOpenRequest_redirectsToNewRequestForm() throws Exception {
        AppUser patient = new AppUser();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setRole(Role.PATIENT);

        AppUser donor = new AppUser();
        donor.setId(2L);
        donor.setFullName("Donor Jane");
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setRole(Role.DONOR);

        when(userService.byEmail("patient@example.com")).thenReturn(patient);
        when(userService.byId(2L)).thenReturn(donor);
        when(bloodRequestService.forPatient(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/donors/2/request")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/requests/new?donorId=2"))
                .andExpect(flash().attributeExists("info"));
    }
}
