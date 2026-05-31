package com.myschedule.repository;

import com.myschedule.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    List<ReminderLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ReminderLog> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
