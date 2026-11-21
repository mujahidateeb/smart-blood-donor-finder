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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonorController.class)
@Import(SecurityConfig.class)
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
}
