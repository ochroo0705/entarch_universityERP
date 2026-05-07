package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityFacultyLeaveRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityFacultyLeaveRequestRepository extends JpaRepository<UniversityFacultyLeaveRequest, Long> {
    List<UniversityFacultyLeaveRequest> findAllByOrderByRequestedAtDescIdDesc();
    List<UniversityFacultyLeaveRequest> findByFacultyProfile_IdOrderByRequestedAtDescIdDesc(Long facultyProfileId);
    List<UniversityFacultyLeaveRequest> findByStatusIgnoreCaseOrderByRequestedAtDescIdDesc(String status);
}
