package com.samjhadoo.model.pricing;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Surge pricing rules based on demand
 */
@Data
@Document(collection = "surge_rules")
public class SurgeRule {

    @Id
    private String id;

    private Long skillId; // Which skill triggers surge

    private String regionCode; // Optional: region-specific surge

    private Integer demandThreshold; // Number of requests before surge kicks in

    private BigDecimal multiplier; // 1.5x, 2.0x

    private BigDecimal capMultiplier; // Maximum surge (e.g., 3.0x)

    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
