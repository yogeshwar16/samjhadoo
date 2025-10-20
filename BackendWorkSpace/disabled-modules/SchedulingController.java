package com.samjhadoo.controller.api;

import com.samjhadoo.dto.schedule.BookingRequest;
import com.samjhadoo.dto.schedule.BookingResponse;
import com.samjhadoo.model.User;
import com.samjhadoo.model.schedule.MentorAvailability;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.SchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @GetMapping("/availability/{mentorId}")
    public ResponseEntity<List<MentorAvailability>> getMentorAvailability(@PathVariable Long mentorId) {
        return ResponseEntity.ok(schedulingService.getMentorAvailability(mentorId));
    }

    @PostMapping("/availability")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Void> setMentorAvailability(@CurrentUser User mentor, @RequestBody List<MentorAvailability> availability) {
        schedulingService.setMentorAvailability(mentor, availability);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slots/{mentorId}")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(@PathVariable Long mentorId, @RequestParam String forDate) {
        LocalDateTime date = LocalDateTime.parse(forDate);
        return ResponseEntity.ok(schedulingService.getAvailableSlots(mentorId, date));
    }

    @PostMapping("/bookings/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> requestBooking(@CurrentUser User mentee, @Valid @RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(schedulingService.requestBooking(mentee, bookingRequest));
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId, @CurrentUser User mentor) {
        return ResponseEntity.ok(schedulingService.confirmBooking(bookingId, mentor));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId, @CurrentUser User user, @RequestBody String reason) {
        return ResponseEntity.ok(schedulingService.cancelBooking(bookingId, user, reason));
    }
}
