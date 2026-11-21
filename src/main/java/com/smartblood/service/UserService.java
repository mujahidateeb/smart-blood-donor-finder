package com.smartblood.service;

import com.smartblood.model.*;
import com.smartblood.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public AppUser register(AppUser user) {
        if (users.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (user.hasRole(Role.ADMIN)) {
            throw new IllegalArgumentException("Admin registration is not allowed.");
        }
        if (user.getRoles().isEmpty() && user.getRole() == null) {
            throw new IllegalArgumentException("Please select at least one role.");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.hasRole(Role.DONOR) && user.getAvailableToDonate() == null) {
            user.setAvailableToDonate(true);
        }
        return users.save(user);
    }

    public AppUser byEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public List<AppUser> allUsers() {
        return users.findAll();
    }

    public AppUser byId(Long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public AppUser updateByAdmin(Long id, AppUser data) {
        return updateByAdmin(id, data, null);
    }

    public AppUser updateByAdmin(Long id, AppUser data, Set<Role> roles) {
        AppUser user = byId(id);
        if (data.getFullName() == null || data.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (data.getEmail() == null || data.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        String email = data.getEmail().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered by another account.");
        }
        user.setFullName(data.getFullName().trim());
        user.setEmail(email);
        user.setPhone(data.getPhone() != null ? data.getPhone().trim() : null);
        user.setCity(data.getCity() != null ? data.getCity().trim() : null);
        user.setArea(data.getArea() != null ? data.getArea().trim() : null);
        user.setBloodGroup(data.getBloodGroup());
        user.setAvailableToDonate(data.getAvailableToDonate());

        if (roles != null && !roles.isEmpty()) {
            user.setRoles(roles);
        }

        return users.save(user);
    }

    public AppUser saveProfile(AppUser user) {
        return users.save(user);
    }

    public AppUser updateProfile(String currentEmail, AppUser updatedData, String newPassword) {
        return updateProfile(currentEmail, updatedData, null, newPassword);
    }

    public AppUser updateProfile(String currentEmail, AppUser updatedData, Set<Role> updatedRoles, String newPassword) {
        AppUser user = byEmail(currentEmail);

        if (updatedData.getFullName() == null || updatedData.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        user.setFullName(updatedData.getFullName().trim());

        if (updatedData.getEmail() != null && !updatedData.getEmail().trim().equalsIgnoreCase(currentEmail)) {
            String newEmail = updatedData.getEmail().trim().toLowerCase();
            if (users.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email is already registered by another account.");
            }
            user.setEmail(newEmail);
        }

        if (updatedData.getCity() == null || updatedData.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be empty.");
        }
        user.setCity(updatedData.getCity().trim());

        user.setArea(updatedData.getArea() != null ? updatedData.getArea().trim() : null);
        user.setPhone(updatedData.getPhone() != null ? updatedData.getPhone().trim() : null);

        if (updatedData.getBloodGroup() != null) {
            user.setBloodGroup(updatedData.getBloodGroup());
        }

        if (updatedRoles != null && !updatedRoles.isEmpty()) {
            Set<Role> sanitizedRoles = new HashSet<>();
            for (Role r : updatedRoles) {
                if (r == Role.DONOR || r == Role.PATIENT) {
                    sanitizedRoles.add(r);
                }
            }
            if (user.hasRole(Role.ADMIN)) {
                sanitizedRoles.add(Role.ADMIN);
            }
            if (!sanitizedRoles.isEmpty()) {
                user.setRoles(sanitizedRoles);
            }
        }

        if (user.hasRole(Role.DONOR)) {
            if (updatedData.getAvailableToDonate() != null) {
                user.setAvailableToDonate(Boolean.TRUE.equals(updatedData.getAvailableToDonate()));
            } else if (user.getAvailableToDonate() == null) {
                user.setAvailableToDonate(true);
            }
        } else {
            user.setAvailableToDonate(false);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.trim().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters long.");
            }
            user.setPassword(encoder.encode(newPassword.trim()));
        }

        return users.save(user);
    }

    public AppUser addRoleToUser(String email, Role role) {
        AppUser user = byEmail(email);
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot self-assign ADMIN role.");
        }
        user.addRole(role);
        if (role == Role.DONOR && user.getAvailableToDonate() == null) {
            user.setAvailableToDonate(true);
        }
        return users.save(user);
    }

    public AppUser removeRoleFromUser(String email, Role role) {
        AppUser user = byEmail(email);
        if (user.getRoles().size() <= 1 && user.hasRole(role)) {
            throw new IllegalArgumentException("You must keep at least one role.");
        }
        user.removeRole(role);
        return users.save(user);
    }

    public List<AppUser> searchDonors(BloodGroup group, String city, String area) {
        if (area != null && !area.trim().isEmpty()) {
            return users.findByRoleAndBloodGroupAndCityIgnoreCaseAndAreaContainingIgnoreCaseAndAvailableToDonateTrue(
                    Role.DONOR, group, city.trim(), area.trim());
        }
        return users.findByRoleAndBloodGroupAndCityIgnoreCaseAndAvailableToDonateTrue(Role.DONOR, group, city.trim());
    }
}
