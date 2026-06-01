package com.myschedule.repository;

import com.myschedule.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Todo> findByUserIdAndStatusOrderByDueTimeAsc(Long userId, String status);

    List<Todo> findByStatusAndReminderSentFalse(String status);

    @Query("SELECT t FROM Todo t WHERE t.reminderSent = false AND t.status = 'UNARRANGED' " +
           "AND t.dueTime IS NOT NULL AND t.dueTime <= :now")
    List<Todo> findPendingReminders(@Param("now") LocalDateTime now);

    List<Todo> findByStatusAndExpiredNotifiedFalse(String status);

    @Query("SELECT t FROM Todo t WHERE t.status = 'ACTIVE' AND t.dueTime IS NOT NULL ORDER BY t.dueTime ASC")
    List<Todo> findActiveWithDueTime();

    @Query("SELECT t FROM Todo t WHERE t.status = 'ACTIVE' AND t.expiredNotified = false " +
           "AND t.dueTime IS NOT NULL AND t.dueTime <= :now")
    List<Todo> findExpiredNotNotified(@Param("now") LocalDateTime now);
}
