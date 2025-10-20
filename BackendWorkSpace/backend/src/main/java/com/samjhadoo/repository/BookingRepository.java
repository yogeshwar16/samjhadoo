package com.samjhadoo.repository;

import com.samjhadoo.model.schedule.Booking;
import com.samjhadoo.model.schedule.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByMentorIdAndStatus(Long mentorId, BookingStatus status);

    List<Booking> findByMenteeIdAndStatus(Long menteeId, BookingStatus status);

    List<Booking> findByMentorIdAndStartTimeBetween(Long mentorId, LocalDateTime start, LocalDateTime end);
}
