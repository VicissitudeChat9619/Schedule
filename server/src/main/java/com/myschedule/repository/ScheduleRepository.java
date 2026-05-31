package com.myschedule.repository;

import com.myschedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdOrderByStartTimeDesc(Long userId);

    List<Schedule> findByUserIdAndStatusTrueOrderByStartTimeAsc(Long userId);

    List<Schedule> findByStatusTrueAndReminderSentFalse();

    @Query("SELECT s FROM Schedule s WHERE s.reminderSent = false AND s.status = true " +
           "AND s.startTime <= :now")
    List<Schedule> findPendingReminders(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Schedule s WHERE s.repeatType != 'NONE' AND s.status = true")
    List<Schedule> findRecurringSchedules();

    List<Schedule> findByStatusTrueAndExpiredNotifiedFalse();
}
