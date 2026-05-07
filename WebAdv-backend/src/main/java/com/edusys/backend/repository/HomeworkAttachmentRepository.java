package com.edusys.backend.repository;

import com.edusys.backend.model.HomeworkAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeworkAttachmentRepository extends JpaRepository<HomeworkAttachment, Long> {
    List<HomeworkAttachment> findByHomeworkIdOrderByUploadedAtAsc(Long homeworkId);
    Optional<HomeworkAttachment> findByIdAndHomeworkId(Long id, Long homeworkId);
}
