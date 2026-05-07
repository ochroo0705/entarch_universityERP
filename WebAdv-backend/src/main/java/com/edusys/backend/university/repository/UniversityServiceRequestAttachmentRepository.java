package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityServiceRequestAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityServiceRequestAttachmentRepository extends JpaRepository<UniversityServiceRequestAttachment, Long> {
    List<UniversityServiceRequestAttachment> findByRequest_IdOrderByUploadedAtAscIdAsc(Long requestId);
    boolean existsByRequest_Id(Long requestId);
}
