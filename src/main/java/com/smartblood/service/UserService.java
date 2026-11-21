package com.smartblood.service;

import com.smartblood.model.*;
import com.smartblood.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    public AppUser register(AppUser user) {
        if (users.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email is already registered.");
        if (user.getRole() == Role.ADMIN) throw new IllegalArgumentException("Admin registration is not allowed.");
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRole() == Role.DONOR && user.getAvailableToDonate() == null) user.setAvailableToDonate(true);
        return users.save(user);
    }

    public AppUser byEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public List<AppUser> allUsers() { return users.findAll(); }

    public AppUser byId(Long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public AppUser updateByAdmin(Long id, AppUser data) {
        AppUser user = byId(id);
        if (data.getFullName() == null || data.getFullName().trim().isEmpty()) throw new IllegalArgumentException("Full name cannot be empty.");
        if (data.getEmail() == null || data.getEmail().trim().isEmpty()) throw new IllegalArgumentException("Email cannot be empty.");
        String email = data.getEmail().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmail(email)) throw new IllegalArgumentException("Email is already registered by another account.");
        user.setFullName(data.getFullName().trim());
        user.setEmail(email);
        user.setPhone(data.getPhone() != null ? data.getPhone().trim() : null);
        user.setCity(data.getCity() != null ? data.getCity().trim() : null);
        user.setArea(data.getArea() != null ? data.getArea().trim() : null);
        user.setBloodGroup(data.getBloodGroup());
        user.setAvailableToDonate(data.getAvailableToDonate());
        return users.save(user);
    }

    public AppUser saveProfile(AppUser user) { return users.save(user); }

    public AppUser updateProfile(String currentEmail, AppUser updatedData, String newPassword) {
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

        if (user.getRole() == Role.DONOR) {
            user.setAvailableToDonate(Boolean.TRUE.equals(updatedData.getAvailableToDonate()));
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.trim().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters long.");
            }
            user.setPassword(encoder.encode(newPassword.trim()));
        }

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
