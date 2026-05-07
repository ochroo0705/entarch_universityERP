package com.edusys.backend.controller;

import com.edusys.backend.dto.HomeworkGradeBulkDto;
import com.edusys.backend.dto.HomeworkGradeDto;
import com.edusys.backend.dto.HomeworkSubmissionResponseDto;
import com.edusys.backend.model.User;
import com.edusys.backend.service.HomeworkGradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homework-grading")
@Tag(name = "Homework Grading", description = "APIs for grading homework submissions")
@SecurityRequirement(name = "bearerAuth")
public class HomeworkGradingController {

    private final HomeworkGradingService gradingService;

    public HomeworkGradingController(HomeworkGradingService gradingService) {
        this.gradingService = gradingService;
    }

    @PatchMapping("/submissions/{submissionId}")
    @Operation(summary = "Grade submission", description = "Teacher grades or updates a single homework submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission graded successfully"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public HomeworkSubmissionResponseDto gradeSubmission(
            @Parameter(description = "Submission ID") @PathVariable Long submissionId,
            @RequestBody HomeworkGradeDto gradeDto
    ) {
        return gradingService.gradeSubmission(submissionId, gradeDto);
    }

    @PatchMapping("/submissions/{submissionId}/update")
    @Operation(summary = "Update grade", description = "Update an existing homework grade")
    public HomeworkSubmissionResponseDto updateGrade(
            @Parameter(description = "Submission ID") @PathVariable Long submissionId,
            @RequestBody HomeworkGradeDto gradeDto,
            @AuthenticationPrincipal User authUser
    ) {
        return gradingService.updateGrade(submissionId, gradeDto, authUser);
    }

    @PatchMapping("/homework/{homeworkId}/bulk")
    @Operation(summary = "Bulk grade submissions", description = "Grade multiple homework submissions at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submissions graded successfully"),
            @ApiResponse(responseCode = "404", description = "Homework not found")
    })
    public List<HomeworkSubmissionResponseDto> gradeSubmissionsBulk(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId,
            @RequestBody List<HomeworkGradeBulkDto> grades
    ) {
        return gradingService.gradeSubmissionsBulk(homeworkId, grades);
    }
}
