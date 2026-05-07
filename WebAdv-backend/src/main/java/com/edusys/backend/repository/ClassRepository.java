package com.edusys.backend.repository;

import com.edusys.backend.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {

	@Query("SELECT DISTINCT ta.classEntity " +
			"FROM TeachingAssignment ta " +
			"WHERE ta.teacher.id = :teacherId " +
			"AND ta.isActive = true " +
			"AND ta.classEntity.isActive = true")
	List<Class> findActiveClassesTaughtByTeacher(@Param("teacherId") Long teacherId);
}
