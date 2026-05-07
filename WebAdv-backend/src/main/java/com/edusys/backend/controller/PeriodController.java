package com.edusys.backend.controller;

import com.edusys.backend.model.Period;
import com.edusys.backend.service.PeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periods")
@Tag(name = "Periods", description = "APIs for managing class periods")
@SecurityRequirement(name = "bearerAuth")
public class PeriodController {

    private final PeriodService service;

    public PeriodController(PeriodService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all periods", description = "Get list of all class periods")
    public List<Period> getAll() {
        return service.findAll();
    }

    @PostMapping
    @Operation(summary = "Create period", description = "Create a new class period")
    public Period create(@RequestBody Period period) {
        return service.save(period);
    }
}
