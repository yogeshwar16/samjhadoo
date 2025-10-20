package com.samjhadoo.controller.api.localization;

import com.samjhadoo.dto.localization.LanguageDTO;
import com.samjhadoo.service.localization.LocalizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Localization", description = "Internationalization and localization endpoints")
public class LocalizationController {

    private final LocalizationService localizationService;

    @GetMapping("/languages")
    @Operation(summary = "Get languages", description = "Gets all active languages")
    public ResponseEntity<List<LanguageDTO>> getLanguages() {
        try {
            List<LanguageDTO> languages = localizationService.getActiveLanguages();
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            log.error("Error getting languages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/languages/{code}")
    @Operation(summary = "Get language", description = "Gets a language by code")
    public ResponseEntity<LanguageDTO> getLanguage(@PathVariable String code) {
        try {
            LanguageDTO language = localizationService.getLanguageByCode(code);
            return ResponseEntity.ok(language);
        } catch (Exception e) {
            log.error("Error getting language: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/translations/{languageCode}")
    @Operation(summary = "Get translations", description = "Gets all translations for a language")
    public ResponseEntity<Map<String, String>> getTranslations(@PathVariable String languageCode) {
        try {
            Map<String, String> translations = localizationService.getAllTranslations(languageCode);
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            log.error("Error getting translations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/translations/{languageCode}/category/{category}")
    @Operation(summary = "Get translations by category", description = "Gets translations for a specific category")
    public ResponseEntity<Map<String, String>> getTranslationsByCategory(
            @PathVariable String languageCode,
            @PathVariable String category) {
        try {
            Map<String, String> translations = localizationService.getTranslationsByCategory(languageCode, category);
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            log.error("Error getting translations by category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/translate")
    @Operation(summary = "Get translation", description = "Gets a single translation")
    public ResponseEntity<Map<String, String>> getTranslation(
            @RequestParam String key,
            @RequestParam String language,
            @RequestParam(required = false) Map<String, Object> params) {
        try {
            String translation = params != null && !params.isEmpty()
                    ? localizationService.getTranslation(key, language, params)
                    : localizationService.getTranslation(key, language);
            return ResponseEntity.ok(Map.of("translation", translation));
        } catch (Exception e) {
            log.error("Error getting translation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/detect-language")
    @Operation(summary = "Detect language", description = "Detects language from text")
    public ResponseEntity<Map<String, String>> detectLanguage(@RequestParam String text) {
        try {
            String languageCode = localizationService.detectLanguage(text);
            return ResponseEntity.ok(Map.of("languageCode", languageCode));
        } catch (Exception e) {
            log.error("Error detecting language: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/progress/{languageCode}")
    @Operation(summary = "Get translation progress", description = "Gets translation completion status")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable String languageCode) {
        try {
            Map<String, Object> progress = localizationService.getTranslationProgress(languageCode);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("Error getting translation progress: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
