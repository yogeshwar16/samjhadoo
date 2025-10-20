package com.samjhadoo.dto.localization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationDTO {
    private Long id;
    private String key;
    private String languageCode;
    private String value;
    private String category;
    private String context;
    private boolean reviewed;
    private boolean verified;
}
