package com.edusys.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TranslationBulkDTO(
        @NotEmpty @Valid List<TranslationDTO> translations
) {}
