package com.smartblood.service;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.Role;
import com.smartblood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setFullName("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("encodedOldPassword");
        existingUser.setRole(Role.DONOR);
        existingUser.setBloodGroup(BloodGroup.O_POSITIVE);
        existingUser.setCity("Bengaluru");
        existingUser.setArea("Indiranagar");
        existingUser.setPhone("9876543210");
        existingUser.setAvailableToDonate(true);
    }

    @Test
    void updateProfile_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("Johnathan Doe");
        updatedData.setEmail("john@example.com");
        updatedData.setBloodGroup(BloodGroup.A_POSITIVE);
        updatedData.setCity("Mysuru");
        updatedData.setArea("Gokulam");
        updatedData.setPhone("9123456780");
        updatedData.setAvailableToDonate(false);

        AppUser result = userService.updateProfile("john@example.com", updatedData, null);

        assertEquals("Johnathan Doe", result.getFullName());
        assertEquals(BloodGroup.A_POSITIVE, result.getBloodGroup());
        assertEquals("Mysuru", result.getCity());
        assertEquals("Gokulam", result.getArea());
        assertEquals("9123456780", result.getPhone());
        assertFalse(result.getAvailableToDonate());
        assertEquals("encodedOldPassword", result.getPassword());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateProfile_withNewPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newSecretPass")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("John Doe");
        updatedData.setCity("Bengaluru");

        AppUser result = userService.updateProfile("john@example.com", updatedData, "newSecretPass");

        assertEquals("encodedNewPassword", result.getPassword());
        verify(passwordEncoder).encode("newSecretPass");
    }

    @Test
    void updateProfile_rejectShortPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("John Doe");
        updatedData.setCity("Bengaluru");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.updateProfile("john@example.com", updatedData, "short")
        );

        assertTrue(ex.getMessage().contains("at least 8 characters"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_rejectEmptyFullName() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("   ");
        updatedData.setCity("Bengaluru");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.updateProfile("john@example.com", updatedData, null)
        );

        assertTrue(ex.getMessage().contains("Full name cannot be empty"));
    }

    @Test
    void updateProfile_rejectEmptyCity() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("John Doe");
        updatedData.setCity("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.updateProfile("john@example.com", updatedData, null)
        );

        assertTrue(ex.getMessage().contains("City cannot be empty"));
    }

    @Test
    void updateProfile_rejectDuplicateEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        AppUser updatedData = new AppUser();
        updatedData.setFullName("John Doe");
        updatedData.setEmail("taken@example.com");
        updatedData.setCity("Bengaluru");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.updateProfile("john@example.com", updatedData, null)
        );

        assertTrue(ex.getMessage().contains("already registered"));
    }
}
