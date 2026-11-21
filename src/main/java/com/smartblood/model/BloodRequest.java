package com.smartblood.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser patient;

    @ManyToOne
    private AppUser acceptedDonor;

    @Enumerated(EnumType.STRING) @NotNull
    private BloodGroup bloodGroup;

    @NotBlank
    private String city;
    private String area;
    @Min(1)
    private Integer unitsRequired = 1;
    @NotBlank
    @Column(length = 1000)
    private String hospitalAddress;
    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime neededBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getPatient() { return patient; }
    public void setPatient(AppUser patient) { this.patient = patient; }
    public AppUser getAcceptedDonor() { return acceptedDonor; }
    public void setAcceptedDonor(AppUser acceptedDonor) { this.acceptedDonor = acceptedDonor; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public Integer getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(Integer unitsRequired) { this.unitsRequired = unitsRequired; }
    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getNeededBy() { return neededBy; }
    public void setNeededBy(LocalDateTime neededBy) { this.neededBy = neededBy; }
}
