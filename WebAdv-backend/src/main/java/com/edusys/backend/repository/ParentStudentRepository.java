package com.edusys.backend.repository;

import com.edusys.backend.model.ParentStudent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long>, JpaSpecificationExecutor<ParentStudent> {
    @Override
    @EntityGraph(attributePaths = {"parent", "student"})
    Page<ParentStudent> findAll(org.springframework.data.jpa.domain.Specification<ParentStudent> spec, Pageable pageable);

    boolean existsByParent_IdAndStudent_Id(Long parentId, Long studentId);
    List<ParentStudent> findByParent_Id(Long parentId);
    List<ParentStudent> findByStudent_Id(Long studentId);

    @EntityGraph(attributePaths = {"parent", "student"})
    List<ParentStudent> findByStudent_IdIn(List<Long> studentIds);

    @Query("select ps.student.id from ParentStudent ps where ps.parent.id = :parentId")
    List<Long> findStudentIdsByParentId(@Param("parentId") Long parentId);
}
