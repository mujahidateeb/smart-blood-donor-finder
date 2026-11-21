package com.smartblood.controller;

import com.smartblood.config.SecurityConfig;
import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.BloodRequest;
import com.smartblood.model.Role;
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
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "dual@example.com", roles = {"DONOR", "PATIENT"})
    void dashboard_dualRoleUser_populatesBothPortalsAndDefaultsToDonorTab() throws Exception {
        AppUser dualUser = new AppUser();
        dualUser.setId(1L);
        dualUser.setEmail("dual@example.com");
        dualUser.setFullName("Dual User");
        dualUser.setRoles(Set.of(Role.DONOR, Role.PATIENT));
        dualUser.setBloodGroup(BloodGroup.O_POSITIVE);
        dualUser.setCity("Bengaluru");

        when(userService.byEmail("dual@example.com")).thenReturn(dualUser);
        when(bloodRequestService.forPatient(1L)).thenReturn(Collections.emptyList());
        when(bloodRequestService.matchedRequestsForDonor(dualUser)).thenReturn(Collections.emptyList());
        when(bloodRequestService.forDonor(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("user", dualUser))
                .andExpect(model().attribute("activeTab", "donor"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(model().attributeExists("openRequests"))
                .andExpect(model().attributeExists("myAcceptedRequests"));
    }

    @Test
    @WithMockUser(username = "dual@example.com", roles = {"DONOR", "PATIENT"})
    void dashboard_dualRoleUser_withPatientTabParam() throws Exception {
        AppUser dualUser = new AppUser();
        dualUser.setId(1L);
        dualUser.setEmail("dual@example.com");
        dualUser.setFullName("Dual User");
        dualUser.setRoles(Set.of(Role.DONOR, Role.PATIENT));
        dualUser.setBloodGroup(BloodGroup.O_POSITIVE);
        dualUser.setCity("Bengaluru");

        when(userService.byEmail("dual@example.com")).thenReturn(dualUser);

        mockMvc.perform(get("/dashboard").param("tab", "patient"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("activeTab", "patient"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    void dashboard_patientOnlyUser_defaultsToPatientTab() throws Exception {
        AppUser patientUser = new AppUser();
        patientUser.setId(2L);
        patientUser.setEmail("patient@example.com");
        patientUser.setFullName("Patient User");
        patientUser.setRoles(Set.of(Role.PATIENT));
        patientUser.setBloodGroup(BloodGroup.B_POSITIVE);
        patientUser.setCity("Bengaluru");

        when(userService.byEmail("patient@example.com")).thenReturn(patientUser);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("activeTab", "patient"));
    }
}
