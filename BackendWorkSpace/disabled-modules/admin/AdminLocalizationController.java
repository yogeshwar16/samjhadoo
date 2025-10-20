package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.localization.TranslationDTO;
import com.samjhadoo.service.localization.LocalizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/i18n")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Localization", description = "Admin endpoints for managing translations")
public class AdminLocalizationController {

    private final LocalizationService localizationService;

    @PostMapping("/translations")
    @Operation(summary = "Save translation", description = "Creates or updates a translation")
    public ResponseEntity<TranslationDTO> saveTranslation(
            @RequestParam @NotNull String key,
            @RequestParam @NotNull String languageCode,
            @RequestParam @NotNull String value,
            @RequestParam(required = false) String category) {
        try {
            TranslationDTO translation = localizationService.saveTranslation(key, languageCode, value, category);
            return ResponseEntity.ok(translation);
        } catch (Exception e) {
            log.error("Error saving translation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/translations/bulk-import")
    @Operation(summary = "Bulk import translations", description = "Imports multiple translations at once")
    public ResponseEntity<Map<String, Integer>> bulkImport(
            @RequestParam @NotNull String languageCode,
            @RequestBody Map<String, String> translations) {
        try {
            int count = localizationService.bulkImportTranslations(languageCode, translations);
            return ResponseEntity.ok(Map.of("imported", count));
        } catch (Exception e) {
            log.error("Error bulk importing translations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/translations/{languageCode}/export")
    @Operation(summary = "Export translations", description = "Exports all translations to JSON")
    public ResponseEntity<String> exportTranslations(@PathVariable String languageCode) {
        try {
            String json = localizationService.exportTranslations(languageCode);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            log.error("Error exporting translations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/translations/auto-translate")
    @Operation(summary = "Auto-translate", description = "Auto-translates missing translations using AI")
    public ResponseEntity<Map<String, Integer>> autoTranslate(
            @RequestParam @NotNull String sourceLanguage,
            @RequestParam @NotNull String targetLanguage) {
        try {
            int count = localizationService.autoTranslate(sourceLanguage, targetLanguage);
            return ResponseEntity.ok(Map.of("translated", count));
        } catch (Exception e) {
            log.error("Error auto-translating: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
