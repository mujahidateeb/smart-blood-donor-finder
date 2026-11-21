package com.smartblood.controller;

import com.smartblood.config.SecurityConfig;
import com.smartblood.model.AppUser;
import com.smartblood.model.Role;
import com.smartblood.security.CustomUserDetailsService;
import com.smartblood.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void getRegister_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    void postRegister_withMultipleRoles_success() throws Exception {
        when(userService.register(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Multi User")
                        .param("email", "multi@example.com")
                        .param("password", "password123")
                        .param("bloodGroup", "O_POSITIVE")
                        .param("city", "Bengaluru")
                        .param("selectedRoles", "DONOR", "PATIENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).register(any(AppUser.class));
    }
}
