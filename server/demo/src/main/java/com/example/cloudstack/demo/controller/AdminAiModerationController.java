package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.moderation.AiModerationRequest;
import com.example.cloudstack.demo.dto.moderation.AiModerationResult;
import com.example.cloudstack.demo.service.AiModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiModerationController {
    private final AiModerationService aiModerationService;

    @PostMapping("/posts/{id}/review")
    public ResponseEntity<ApiResponse<AiModerationResult>> reviewPost(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean apply) {
        AiModerationResult result = aiModerationService.reviewPost(id, apply);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/comments/{id}/review")
    public ResponseEntity<ApiResponse<AiModerationResult>> reviewComment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean apply) {
        AiModerationResult result = aiModerationService.reviewComment(id, apply);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/review")
    public ResponseEntity<ApiResponse<AiModerationResult>> reviewText(
            @Valid @RequestBody AiModerationRequest request) {
        AiModerationResult result = aiModerationService.reviewText(
                request.getType(),
                request.getTitle(),
                request.getContent());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
