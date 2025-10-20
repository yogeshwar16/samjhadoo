package com.samjhadoo.controller.api;

import com.samjhadoo.dto.ai.AIAnalyticsDTO;
import com.samjhadoo.model.ai.AIConfig;
import com.samjhadoo.repository.ai.AIConfigRepository;
import com.samjhadoo.service.ai.AIGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin API for AI configuration and analytics
 */
@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIController {

    private final AIGatewayService aiGatewayService;
    private final AIConfigRepository aiConfigRepository;

    /**
     * Get AI analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<AIAnalyticsDTO> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        AIAnalyticsDTO analytics = aiGatewayService.getAnalytics(start, end);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get all AI configurations
     */
    @GetMapping("/config")
    public ResponseEntity<List<AIConfig>> getAllConfigs() {
        return ResponseEntity.ok(aiConfigRepository.findAll());
    }

    /**
     * Update AI configuration
     */
    @PutMapping("/config/{id}")
    public ResponseEntity<AIConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody AIConfig config) {
        AIConfig existing = aiConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config not found"));

        existing.setModelName(config.getModelName());
        existing.setMaxTokens(config.getMaxTokens());
        existing.setRequestLimitPerHour(config.getRequestLimitPerHour());
        existing.setEnabled(config.isEnabled());
        existing.setSystemPrompt(config.getSystemPrompt());

        return ResponseEntity.ok(aiConfigRepository.save(existing));
    }

    /**
     * Enable/disable AI tier
     */
    @PostMapping("/config/{id}/toggle")
    public ResponseEntity<Void> toggleConfig(@PathVariable Long id) {
        AIConfig config = aiConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config not found"));
        
        config.setEnabled(!config.isEnabled());
        aiConfigRepository.save(config);

        return ResponseEntity.ok().build();
    }
}
