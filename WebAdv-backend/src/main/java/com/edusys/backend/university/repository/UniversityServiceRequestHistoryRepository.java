package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityServiceRequestHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityServiceRequestHistoryRepository extends JpaRepository<UniversityServiceRequestHistory, Long> {
    List<UniversityServiceRequestHistory> findByRequest_IdOrderByCreatedAtAscIdAsc(Long requestId);
}
