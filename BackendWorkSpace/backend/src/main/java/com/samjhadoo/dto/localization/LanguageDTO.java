package com.samjhadoo.dto.localization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    private Long id;
    private String code;
    private String name;
    private String nativeName;
    private boolean rtl;
    private boolean active;
    private boolean defaultLanguage;
    private String flagEmoji;
    private String localeCode;
    private Double completionPercentage;
}
