package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityServiceRequestComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityServiceRequestCommentRepository extends JpaRepository<UniversityServiceRequestComment, Long> {
    List<UniversityServiceRequestComment> findByRequest_IdOrderByCreatedAtAscIdAsc(Long requestId);
}
