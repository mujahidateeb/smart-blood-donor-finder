package com.smartblood.repository;

import com.smartblood.model.AppUser;
import com.smartblood.model.BloodGroup;
import com.smartblood.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByRoleAndBloodGroupAndCityIgnoreCaseAndAvailableToDonateTrue(
            Role role, BloodGroup bloodGroup, String city);
    List<AppUser> findByRoleAndBloodGroupAndCityIgnoreCaseAndAreaContainingIgnoreCaseAndAvailableToDonateTrue(
            Role role, BloodGroup bloodGroup, String city, String area);
}
