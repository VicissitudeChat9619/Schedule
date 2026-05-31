package com.myschedule.service;

import com.myschedule.dto.ScheduleRequest;
import com.myschedule.entity.Schedule;
import com.myschedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<Schedule> listByUser(Long userId) {
        return scheduleRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    public Schedule create(Long userId, ScheduleRequest request) {
        Schedule schedule = Schedule.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .repeatType(request.getRepeatType() != null ? request.getRepeatType() : "NONE")
                .reminderBeforeMinutes(request.getReminderBeforeMinutes() != null ? request.getReminderBeforeMinutes() : 15)
                .build();

        return scheduleRepository.save(schedule);
    }

    public Schedule update(Long userId, Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        if (!schedule.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此日程");
        }

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setRepeatType(request.getRepeatType() != null ? request.getRepeatType() : "NONE");
        schedule.setReminderBeforeMinutes(request.getReminderBeforeMinutes() != null ? request.getReminderBeforeMinutes() : 15);

        return scheduleRepository.save(schedule);
    }

    public void delete(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        if (!schedule.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此日程");
        }

        scheduleRepository.delete(schedule);
    }

    public Schedule getById(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        if (!schedule.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此日程");
        }

        return schedule;
    }
}
