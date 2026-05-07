package com.edusys.backend.university.dto;

import java.util.List;

public record ServiceRequestDetailResponse(
        ServiceRequestResponse request,
        List<ServiceRequestCommentResponse> comments,
        List<ServiceRequestHistoryResponse> history,
        List<ServiceRequestAttachmentResponse> attachments
) {}
