package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityFacultyWorkload;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityFacultyWorkloadRepository extends JpaRepository<UniversityFacultyWorkload, Long> {
    List<UniversityFacultyWorkload> findAllByOrderByAcademicYearDescSemesterDescIdDesc();
    List<UniversityFacultyWorkload> findByFacultyProfile_IdOrderByAcademicYearDescSemesterDescIdDesc(Long facultyProfileId);
}
