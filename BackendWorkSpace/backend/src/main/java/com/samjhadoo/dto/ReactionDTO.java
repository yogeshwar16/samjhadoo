package com.samjhadoo.dto.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    private String emoji;
    private Long userId;
    private String userName;
    private LocalDateTime reactedAt;
}
