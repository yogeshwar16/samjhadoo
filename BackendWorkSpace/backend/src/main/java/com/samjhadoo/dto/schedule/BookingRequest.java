package com.samjhadoo.dto.schedule;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {

    @NotNull
    private Long mentorId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    private Integer durationMinutes;

    private String message;
}
