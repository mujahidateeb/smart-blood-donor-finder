package com.smartblood.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @Email @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private String phone;
    private String city;
    private String area;
    private Boolean availableToDonate = Boolean.TRUE;
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PostLoad
    public void onPostLoad() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        if (roles.isEmpty() && role != null) {
            roles.add(role);
        }
    }

    @PrePersist
    @PreUpdate
    public void onPrePersistOrUpdate() {
        if (roles != null && !roles.isEmpty()) {
            if (role == null || !roles.contains(role)) {
                role = roles.iterator().next();
            }
        } else if (role != null) {
            if (roles == null) {
                roles = new HashSet<>();
            }
            roles.add(role);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() {
        if (role != null) return role;
        if (roles != null && !roles.isEmpty()) {
            if (roles.contains(Role.ADMIN)) return Role.ADMIN;
            if (roles.contains(Role.DONOR)) return Role.DONOR;
            return roles.iterator().next();
        }
        return null;
    }

    public void setRole(Role role) {
        this.role = role;
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
        }
    }

    public Set<Role> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        if (roles.isEmpty() && role != null) {
            roles.add(role);
        }
        return roles;
    }

    public void setRoles(Set<Role> newRoles) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        } else {
            this.roles.clear();
        }
        if (newRoles != null) {
            this.roles.addAll(newRoles);
        }
        if (!this.roles.isEmpty()) {
            if (this.role == null || !this.roles.contains(this.role)) {
                this.role = this.roles.iterator().next();
            }
        } else {
            this.role = null;
        }
    }

    public boolean hasRole(Role targetRole) {
        if (targetRole == null) return false;
        if (this.role == targetRole) return true;
        return this.roles != null && this.roles.contains(targetRole);
    }

    public boolean isDonor() {
        return hasRole(Role.DONOR);
    }

    public boolean isPatient() {
        return hasRole(Role.PATIENT);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public void addRole(Role roleToAdd) {
        if (roleToAdd == null) return;
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(roleToAdd);
        if (this.role == null) {
            this.role = roleToAdd;
        }
    }

    public void removeRole(Role roleToRemove) {
        if (roleToRemove == null || this.roles == null) return;
        this.roles.remove(roleToRemove);
        if (this.role == roleToRemove) {
            this.role = this.roles.isEmpty() ? null : this.roles.iterator().next();
        }
    }

    public String getRolesDisplay() {
        Set<Role> currentRoles = getRoles();
        if (currentRoles.isEmpty()) {
            return role != null ? role.name() : "";
        }
        return currentRoles.stream()
                .map(Role::name)
                .collect(Collectors.joining(", "));
    }

    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public Boolean getAvailableToDonate() { return availableToDonate; }
    public void setAvailableToDonate(Boolean availableToDonate) { this.availableToDonate = availableToDonate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
