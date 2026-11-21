package com.smartblood.repository;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM AppUser u LEFT JOIN u.roles r " +
            "WHERE (u.role = :role OR r = :role) " +
            "AND u.bloodGroup = :bloodGroup " +
            "AND LOWER(u.city) = LOWER(:city) " +
            "AND u.availableToDonate = true")
    List<AppUser> findByRoleAndBloodGroupAndCityIgnoreCaseAndAvailableToDonateTrue(
            @Param("role") Role role,
            @Param("bloodGroup") BloodGroup bloodGroup,
            @Param("city") String city);

    @Query("SELECT DISTINCT u FROM AppUser u LEFT JOIN u.roles r " +
            "WHERE (u.role = :role OR r = :role) " +
            "AND u.bloodGroup = :bloodGroup " +
            "AND LOWER(u.city) = LOWER(:city) " +
            "AND LOWER(u.area) LIKE LOWER(CONCAT('%', :area, '%')) " +
            "AND u.availableToDonate = true")
    List<AppUser> findByRoleAndBloodGroupAndCityIgnoreCaseAndAreaContainingIgnoreCaseAndAvailableToDonateTrue(
            @Param("role") Role role,
            @Param("bloodGroup") BloodGroup bloodGroup,
            @Param("city") String city,
            @Param("area") String area);
}
