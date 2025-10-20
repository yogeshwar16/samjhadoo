package com.samjhadoo.dto.schedule;

import com.samjhadoo.model.schedule.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long menteeId;
    private Long mentorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
}
