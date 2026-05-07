package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.ServiceRequestStatus;
import com.edusys.backend.university.model.UniversityServiceRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UniversityServiceRequestRepository extends JpaRepository<UniversityServiceRequest, Long>, JpaSpecificationExecutor<UniversityServiceRequest> {
    List<UniversityServiceRequest> findAllByOrderByRequestedAtDescIdDesc();
    List<UniversityServiceRequest> findByStudent_IdOrderByRequestedAtDescIdDesc(Long studentId);
    List<UniversityServiceRequest> findByStatusOrderByRequestedAtDescIdDesc(ServiceRequestStatus status);
    List<UniversityServiceRequest> findByStudent_IdAndStatusOrderByRequestedAtDescIdDesc(Long studentId, ServiceRequestStatus status);
    long countByRequestNumberStartingWith(String prefix);
    long countByStatus(ServiceRequestStatus status);
    long countByAssignedOfficeAndStatusNotIn(String assignedOffice, List<ServiceRequestStatus> statuses);
    long countByAssignedOfficeAndAssignedUserIsNullAndStatusNotIn(String assignedOffice, List<ServiceRequestStatus> statuses);
    long countByAssignedOfficeAndDueAtBeforeAndStatusNotIn(String assignedOffice, java.time.LocalDateTime dueAt, List<ServiceRequestStatus> statuses);
    long countByAssignedOfficeAndDueAtBetweenAndStatusNotIn(String assignedOffice, java.time.LocalDateTime start, java.time.LocalDateTime end, List<ServiceRequestStatus> statuses);
}
