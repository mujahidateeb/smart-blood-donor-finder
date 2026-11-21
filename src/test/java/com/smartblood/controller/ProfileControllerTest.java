package com.smartblood.controller;

import com.smartblood.config.SecurityConfig;
import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.Role;
import com.smartblood.security.CustomUserDetailsService;
import com.smartblood.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void getProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void getProfile_authenticated_returnsProfileView() throws Exception {
        AppUser user = new AppUser();
        user.setEmail("donor@example.com");
        user.setFullName("Donor User");
        user.setRole(Role.DONOR);
        user.setBloodGroup(BloodGroup.O_POSITIVE);
        user.setCity("Bengaluru");

        when(userService.byEmail("donor@example.com")).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("bloodGroups"))
                .andExpect(model().attributeExists("allRoles"));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void postProfile_success_redirectsWithFlashAttribute() throws Exception {
        AppUser savedUser = new AppUser();
        savedUser.setEmail("donor@example.com");
        savedUser.setFullName("Updated Name");

        when(userService.updateProfile(eq("donor@example.com"), any(AppUser.class), eq(null)))
                .thenReturn(savedUser);

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("fullName", "Updated Name")
                        .param("email", "donor@example.com")
                        .param("bloodGroup", "A_POSITIVE")
                        .param("city", "Bengaluru")
                        .param("area", "Koramangala")
                        .param("phone", "9876543210")
                        .param("availableToDonate", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Profile updated successfully."));

        verify(userService).updateProfile(eq("donor@example.com"), any(AppUser.class), eq(null));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void postProfile_withUpdatedRoles_success() throws Exception {
        AppUser savedUser = new AppUser();
        savedUser.setEmail("donor@example.com");
        savedUser.setFullName("Donor and Patient");
        savedUser.setRoles(Set.of(Role.DONOR, Role.PATIENT));

        when(userService.updateProfile(eq("donor@example.com"), any(AppUser.class), eq(Set.of(Role.DONOR, Role.PATIENT)), eq(null)))
                .thenReturn(savedUser);

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("fullName", "Donor and Patient")
                        .param("email", "donor@example.com")
                        .param("bloodGroup", "O_POSITIVE")
                        .param("city", "Bengaluru")
                        .param("rolesSubmitted", "true")
                        .param("selectedRoles", "DONOR", "PATIENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Profile updated successfully."));

        verify(userService).updateProfile(eq("donor@example.com"), any(AppUser.class), eq(Set.of(Role.DONOR, Role.PATIENT)), eq(null));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void postProfile_withNoRolesSelected_redirectsWithError() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("fullName", "Donor User")
                        .param("email", "donor@example.com")
                        .param("city", "Bengaluru")
                        .param("rolesSubmitted", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("error", "Please select at least one role (Donor and/or Patient)."));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void postProfile_validationFailure_redirectsWithError() throws Exception {
        when(userService.updateProfile(eq("donor@example.com"), any(AppUser.class), eq(null)))
                .thenThrow(new IllegalArgumentException("City cannot be empty."));

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("fullName", "Donor User")
                        .param("email", "donor@example.com")
                        .param("city", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("error", "City cannot be empty."));
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void addRole_activatesRoleAndRedirectsToDashboardTab() throws Exception {
        AppUser updated = new AppUser();
        updated.setEmail("donor@example.com");
        updated.setRoles(Set.of(Role.DONOR, Role.PATIENT));

        when(userService.addRoleToUser("donor@example.com", Role.PATIENT)).thenReturn(updated);

        mockMvc.perform(post("/profile/roles/add")
                        .with(csrf())
                        .param("role", "PATIENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?tab=patient"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).addRoleToUser("donor@example.com", Role.PATIENT);
    }

    @Test
    @WithMockUser(username = "donor@example.com", roles = "DONOR")
    void addRole_withRedirectUrl_redirectsToSpecifiedUrl() throws Exception {
        AppUser updated = new AppUser();
        updated.setEmail("donor@example.com");
        updated.setRoles(Set.of(Role.DONOR, Role.PATIENT));

        when(userService.addRoleToUser("donor@example.com", Role.PATIENT)).thenReturn(updated);

        mockMvc.perform(post("/profile/roles/add")
                        .with(csrf())
                        .param("role", "PATIENT")
                        .param("redirectUrl", "/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).addRoleToUser("donor@example.com", Role.PATIENT);
    }

    @Test
    @WithMockUser(username = "dual@example.com", roles = {"DONOR", "PATIENT"})
    void removeRole_removesRoleAndRedirectsToDashboard() throws Exception {
        AppUser updated = new AppUser();
        updated.setEmail("dual@example.com");
        updated.setRoles(Set.of(Role.DONOR));

        when(userService.removeRoleFromUser("dual@example.com", Role.PATIENT)).thenReturn(updated);

        mockMvc.perform(post("/profile/roles/remove")
                        .with(csrf())
                        .param("role", "PATIENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).removeRoleFromUser("dual@example.com", Role.PATIENT);
    }
}
