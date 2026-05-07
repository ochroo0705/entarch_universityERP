package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.CourseSelectionStatus;
import com.edusys.backend.university.model.UniversityCourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityCourseSelectionRepository extends JpaRepository<UniversityCourseSelection, Long> {
    List<UniversityCourseSelection> findByStudent_IdOrderBySelectedAtDescIdDesc(Long studentId);
    List<UniversityCourseSelection> findByAcademicYearAndSemesterOrderBySelectedAtDescIdDesc(String academicYear, Integer semester);
    List<UniversityCourseSelection> findByStudent_IdAndAcademicYearAndSemesterOrderBySelectedAtDescIdDesc(Long studentId, String academicYear, Integer semester);
    Optional<UniversityCourseSelection> findByStudent_IdAndSubject_IdAndAcademicYearAndSemester(Long studentId, Long subjectId, String academicYear, Integer semester);
    long countByStatus(CourseSelectionStatus status);
}
