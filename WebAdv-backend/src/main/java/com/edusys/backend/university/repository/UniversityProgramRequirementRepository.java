package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityProgramRequirement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityProgramRequirementRepository extends JpaRepository<UniversityProgramRequirement, Long> {
    List<UniversityProgramRequirement> findByProgramNameIgnoreCaseAndActiveTrueOrderByIdAsc(String programName);
    List<UniversityProgramRequirement> findAllByOrderByProgramNameAscIdAsc();
}
