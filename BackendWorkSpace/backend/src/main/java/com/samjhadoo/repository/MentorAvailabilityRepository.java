package com.samjhadoo.repository;

import com.samjhadoo.model.schedule.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {

    List<MentorAvailability> findByMentorId(Long mentorId);

    List<MentorAvailability> findByMentorIdAndDayOfWeek(Long mentorId, DayOfWeek dayOfWeek);
}
