package com.samjhadoo.dto.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResult {
    private String text;
    private String language;
    private Double confidence;
    private Integer duration; // in seconds
    private String provider;
    private String[] alternatives;
    private String errorMessage;
}
