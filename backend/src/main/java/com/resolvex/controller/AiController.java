package com.resolvex.controller;

import com.resolvex.dto.AiDtos;
import com.resolvex.dto.ApiResponse;
import com.resolvex.service.AiClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Assistance", description = "AI-assisted ticket classification and knowledge article recommendation")
public class AiController {

    private final AiClassificationService aiClassificationService;

    public AiController(AiClassificationService aiClassificationService) {
        this.aiClassificationService = aiClassificationService;
    }

    @PostMapping("/analyze-ticket")
    @Operation(summary = "Analyze ticket details to recommend category, priority, and knowledge articles with user confirmation")
    public ResponseEntity<ApiResponse<AiDtos.AiTicketAnalysisResponse>> analyzeTicket(
            @Valid @RequestBody AiDtos.AiTicketAnalysisRequest request) {
        AiDtos.AiTicketAnalysisResponse response = aiClassificationService.analyzeTicket(request);
        return ResponseEntity.ok(ApiResponse.success("Analysis generated successfully", response));
    }
}
