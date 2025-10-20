package com.samjhadoo.service;

import com.samjhadoo.dto.schedule.BookingRequest;
import com.samjhadoo.dto.schedule.BookingResponse;
import com.samjhadoo.exception.OperationNotAllowedException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.schedule.Booking;
import com.samjhadoo.model.schedule.BookingStatus;
import com.samjhadoo.model.schedule.MentorAvailability;
import com.samjhadoo.repository.BookingRepository;
import com.samjhadoo.repository.MentorAvailabilityRepository;
import com.samjhadoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingServiceImpl implements SchedulingService {

    private final MentorAvailabilityRepository availabilityRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MentorAvailability> getMentorAvailability(Long mentorId) {
        return availabilityRepository.findByMentorId(mentorId);
    }

    @Override
    @Transactional
    public void setMentorAvailability(User mentor, List<MentorAvailability> availability) {
        // Clear existing availability
        availabilityRepository.deleteAll(availabilityRepository.findByMentorId(mentor.getId()));

        // Save new availability
        availability.forEach(a -> a.setMentor(mentor));
        availabilityRepository.saveAll(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> getAvailableSlots(Long mentorId, LocalDateTime forDate) {
        DayOfWeek dayOfWeek = forDate.getDayOfWeek();
        List<MentorAvailability> availabilities = availabilityRepository.findByMentorIdAndDayOfWeek(mentorId, dayOfWeek);
        List<Booking> bookings = bookingRepository.findByMentorIdAndStartTimeBetween(mentorId, forDate.with(LocalTime.MIN), forDate.with(LocalTime.MAX));

        List<LocalDateTime> availableSlots = new ArrayList<>();
        for (MentorAvailability availability : availabilities) {
            LocalTime slot = availability.getStartTime();
            while (slot.isBefore(availability.getEndTime())) {
                LocalDateTime currentSlot = forDate.with(slot);
                boolean isBooked = bookings.stream().anyMatch(b -> b.getStartTime().equals(currentSlot));
                if (!isBooked) {
                    availableSlots.add(currentSlot);
                }
                slot = slot.plusMinutes(30); // Assuming 30-minute slots
            }
        }
        return availableSlots;
    }

    @Override
    @Transactional
    public BookingResponse requestBooking(User mentee, BookingRequest bookingRequest) {
        User mentor = userRepository.findById(bookingRequest.getMentorId()).orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        Booking booking = new Booking();
        booking.setMentee(mentee);
        booking.setMentor(mentor);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getStartTime().plusMinutes(bookingRequest.getDurationMinutes()));
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.REQUESTED);

        booking = bookingRepository.save(booking);

        // TODO: Send notification to mentor

        return toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId, User mentor) {
        Booking booking = findBookingByIdAndMentor(bookingId, mentor.getId());
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new OperationNotAllowedException("Only requested bookings can be confirmed.");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        // TODO: Send notification to mentee

        return toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, User user, String reason) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getMentee().equals(user) && !booking.getMentor().equals(user)) {
            throw new OperationNotAllowedException("You are not authorized to cancel this booking.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking = bookingRepository.save(booking);

        // TODO: Send notification to other party

        return toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse rescheduleBooking(Long bookingId, User user, LocalDateTime newStartTime) {
        // Implementation for rescheduling
        return null;
    }

    private Booking findBookingByIdAndMentor(Long bookingId, Long mentorId) {
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getMentor().getId().equals(mentorId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or you are not the mentor for this booking."));
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .menteeId(booking.getMentee().getId())
                .mentorId(booking.getMentor().getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .build();
    }
}
