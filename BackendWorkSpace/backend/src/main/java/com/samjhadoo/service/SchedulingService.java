package com.samjhadoo.service;

import com.samjhadoo.dto.schedule.BookingRequest;
import com.samjhadoo.dto.schedule.BookingResponse;
import com.samjhadoo.model.User;
import com.samjhadoo.model.schedule.MentorAvailability;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedulingService {

    List<MentorAvailability> getMentorAvailability(Long mentorId);

    void setMentorAvailability(User mentor, List<MentorAvailability> availability);

    List<LocalDateTime> getAvailableSlots(Long mentorId, LocalDateTime forDate);

    BookingResponse requestBooking(User mentee, BookingRequest bookingRequest);

    BookingResponse confirmBooking(Long bookingId, User mentor);

    BookingResponse cancelBooking(Long bookingId, User user, String reason);

    BookingResponse rescheduleBooking(Long bookingId, User user, LocalDateTime newStartTime);
}
