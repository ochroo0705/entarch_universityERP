package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityFacultyProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityFacultyProfileRepository extends JpaRepository<UniversityFacultyProfile, Long> {
    List<UniversityFacultyProfile> findAllByOrderByDepartmentAscFacultyUser_LastNameAscFacultyUser_FirstNameAscIdAsc();
    Optional<UniversityFacultyProfile> findByFacultyUser_Id(Long facultyUserId);
}
