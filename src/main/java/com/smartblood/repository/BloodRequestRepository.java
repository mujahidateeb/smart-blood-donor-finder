package com.smartblood.repository;

import com.smartblood.model.BloodRequest;
import com.smartblood.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<BloodRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<BloodRequest> findByAcceptedDonorIdOrderByCreatedAtDesc(Long donorId);
}
