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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    @Test
    void register_withMultipleRoles() {
        when(userRepository.existsByEmail("multi@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = new AppUser();
        user.setEmail("multi@example.com");
        user.setPassword("password123");
        user.setRoles(Set.of(Role.DONOR, Role.PATIENT));

        AppUser registered = userService.register(user);

        assertTrue(registered.hasRole(Role.DONOR));
        assertTrue(registered.hasRole(Role.PATIENT));
        assertTrue(registered.isDonor());
        assertTrue(registered.isPatient());
        assertTrue(registered.getAvailableToDonate());
        verify(userRepository).save(user);
    }

    @Test
    void register_rejectAdmin() {
        AppUser adminUser = new AppUser();
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(Role.ADMIN));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.register(adminUser)
        );

        assertTrue(ex.getMessage().contains("Admin registration is not allowed"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addRoleToUser_donorBecomesPatient() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updated = userService.addRoleToUser("john@example.com", Role.PATIENT);

        assertTrue(updated.hasRole(Role.DONOR));
        assertTrue(updated.hasRole(Role.PATIENT));
        assertTrue(updated.isDonor());
        assertTrue(updated.isPatient());
        verify(userRepository).save(existingUser);
    }

    @Test
    void removeRoleFromUser_successAndRejectLastRole() {
        existingUser.addRole(Role.PATIENT);
        assertTrue(existingUser.isDonor());
        assertTrue(existingUser.isPatient());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updated = userService.removeRoleFromUser("john@example.com", Role.PATIENT);
        assertFalse(updated.hasRole(Role.PATIENT));
        assertTrue(updated.hasRole(Role.DONOR));

        // Attempting to remove the last role must fail
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.removeRoleFromUser("john@example.com", Role.DONOR)
        );
        assertTrue(ex.getMessage().contains("at least one role"));
    }

    @Test
    void updateProfile_withRoles_updatesRolesAndSanitizesAdmin() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser updatedData = new AppUser();
        updatedData.setFullName("John Multi");
        updatedData.setCity("Bengaluru");

        Set<Role> requestedRoles = new HashSet<>();
        requestedRoles.add(Role.DONOR);
        requestedRoles.add(Role.PATIENT);
        requestedRoles.add(Role.ADMIN); // Attempt to self-assign ADMIN

        AppUser result = userService.updateProfile("john@example.com", updatedData, requestedRoles, null);

        assertTrue(result.hasRole(Role.DONOR));
        assertTrue(result.hasRole(Role.PATIENT));
        assertFalse(result.hasRole(Role.ADMIN)); // ADMIN must be filtered out
    }
}
