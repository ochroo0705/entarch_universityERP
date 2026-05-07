package com.edusys.backend.repository;

import com.edusys.backend.model.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    // Find all homework for a given teaching assignment
    List<Homework> findByTeachingAssignmentId(Long teachingAssignmentId);

    // All homework for a given student
    @Query("""
        SELECT h FROM Homework h\s
        JOIN h.teachingAssignment t
        JOIN t.classEntity c
        JOIN c.studentEnrollments se
        WHERE se.student.id = :studentId
   \s""")
    List<Homework> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT COUNT(h) FROM Homework h\s
        JOIN h.teachingAssignment t
        JOIN t.classEntity c
        JOIN c.studentEnrollments se
        WHERE se.student.id = :studentId
    """)
    long countAllByStudentId(@Param("studentId") Long studentId);

    // All homework for a teacher
    List<Homework> findByTeachingAssignment_Teacher_Id(Long teacherId);

    // All homework for a list of class IDs
    List<Homework> findByTeachingAssignment_ClassEntity_IdIn(List<Long> classIds);

    // Get all homeworks for a specific class
    List<Homework> findByTeachingAssignment_ClassEntity_Id(Long classId);

    // Check if a teacher owns any homework in a class
    boolean existsByTeachingAssignment_ClassEntity_IdAndTeachingAssignment_Teacher_Id(Long classId, Long teacherId);
}
