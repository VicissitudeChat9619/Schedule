package com.myschedule.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myschedule.entity.ReminderLog;
import com.myschedule.entity.Schedule;
import com.myschedule.entity.Todo;
import com.myschedule.entity.User;
import com.myschedule.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final UserRepository userRepository;
    private final RestTemplate napcatRestTemplate;
    private final String napcatApiUrl;
    private final ObjectMapper objectMapper;

    public ReminderService(ScheduleRepository scheduleRepository,
                           TodoRepository todoRepository,
                           ReminderLogRepository reminderLogRepository,
                           UserRepository userRepository,
                           @Qualifier("napcatRestTemplate") RestTemplate napcatRestTemplate,
                           @Qualifier("napcatApiUrl") String napcatApiUrl) {
        this.scheduleRepository = scheduleRepository;
        this.todoRepository = todoRepository;
        this.reminderLogRepository = reminderLogRepository;
        this.userRepository = userRepository;
        this.napcatRestTemplate = napcatRestTemplate;
        this.napcatApiUrl = napcatApiUrl;
        this.objectMapper = new ObjectMapper();
    }

    @Scheduled(cron = "${reminder.cron}")
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        checkScheduleReminders(now);
        checkTodoReminders(now);
        checkExpiredSchedules(now);
    }

    private void checkScheduleReminders(LocalDateTime now) {
        List<Schedule> schedules = scheduleRepository.findByStatusTrueAndReminderSentFalse();
        for (Schedule schedule : schedules) {
            LocalDateTime remindTime = schedule.getStartTime().minusMinutes(schedule.getReminderBeforeMinutes());
            if (now.isAfter(remindTime) || now.isEqual(remindTime)) {
                sendScheduleReminder(schedule);
            }
        }
    }

    private void checkTodoReminders(LocalDateTime now) {
        List<Todo> todos = todoRepository.findByStatusAndReminderSentFalse("UNARRANGED");
        for (Todo todo : todos) {
            if (todo.getDueTime() == null) {
                continue;
            }

            LocalDateTime remindTime = todo.getDueTime().minusMinutes(todo.getReminderBeforeMinutes());
            if (now.isAfter(remindTime) || now.isEqual(remindTime)) {
                sendTodoReminder(todo);
            }
        }
    }

    private void sendScheduleReminder(Schedule schedule) {
        User user = userRepository.findById(schedule.getUserId()).orElse(null);
        if (user == null || user.getQqNumber() == null) {
            return;
        }

        String message = buildScheduleMessage(schedule);
        String result = sendToQQ(user.getNapcatUserId(), message);

        ReminderLog log = ReminderLog.builder()
                .userId(user.getId())
                .targetType("SCHEDULE")
                .targetId(schedule.getId())
                .content(message)
                .sendStatus(result != null ? "SUCCESS" : "FAILED")
                .response(result)
                .sendTime(LocalDateTime.now())
                .build();
        reminderLogRepository.save(log);

        if (result != null) {
            schedule.setReminderSent(true);
            scheduleRepository.save(schedule);
        }
    }

    private void sendTodoReminder(Todo todo) {
        User user = userRepository.findById(todo.getUserId()).orElse(null);
        if (user == null || user.getQqNumber() == null) {
            return;
        }

        String message = buildTodoMessage(todo);
        String result = sendToQQ(user.getNapcatUserId(), message);

        ReminderLog log = ReminderLog.builder()
                .userId(user.getId())
                .targetType("TODO")
                .targetId(todo.getId())
                .content(message)
                .sendStatus(result != null ? "SUCCESS" : "FAILED")
                .response(result)
                .sendTime(LocalDateTime.now())
                .build();
        reminderLogRepository.save(log);

        if (result != null) {
            todo.setReminderSent(true);
            todoRepository.save(todo);
        }
    }

    private String buildScheduleMessage(Schedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("【日程提醒】\n");
        sb.append("标题: ").append(schedule.getTitle()).append("\n");
        if (schedule.getDescription() != null && !schedule.getDescription().isEmpty()) {
            sb.append("内容: ").append(schedule.getDescription()).append("\n");
        }
        sb.append("时间: ").append(schedule.getStartTime()).append("\n");
        if (schedule.getEndTime() != null) {
            sb.append("结束: ").append(schedule.getEndTime()).append("\n");
        }
        return sb.toString();
    }

    private String buildTodoMessage(Todo todo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【待办提醒】\n");
        sb.append("事项: ").append(todo.getTitle()).append("\n");
        if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
            sb.append("详情: ").append(todo.getDescription()).append("\n");
        }
        sb.append("优先级: ").append(getPriorityLabel(todo.getPriority())).append("\n");
        if (todo.getDueTime() != null) {
            sb.append("截止: ").append(todo.getDueTime()).append("\n");
        }
        return sb.toString();
    }

    private String getPriorityLabel(Integer priority) {
        if (priority == null) return "普通";
        return switch (priority) {
            case 1 -> "高";
            case 3 -> "低";
            default -> "普通";
        };
    }

    private String sendToQQ(String targetUserId, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("user_id", targetUserId != null ? targetUserId : "");
            body.put("message", message);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            String response = napcatRestTemplate.postForObject(
                    napcatApiUrl + "/send_private_msg", request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            if ("ok".equals(jsonNode.get("status").asText())) {
                log.info("Reminder sent to QQ {}: {}", targetUserId, message.substring(0, Math.min(50, message.length())));
                return response;
            }
            log.warn("NapCat returned non-ok status: {}", response);
            return null;
        } catch (Exception e) {
            log.error("Failed to send QQ message to {}: {}", targetUserId, e.getMessage());
            return null;
        }
    }

    private void checkExpiredSchedules(LocalDateTime now) {
        List<Schedule> schedules = scheduleRepository.findByStatusTrueAndExpiredNotifiedFalse();
        for (Schedule schedule : schedules) {
            LocalDateTime expireTime = schedule.getEndTime() != null ? schedule.getEndTime() : schedule.getStartTime();
            if (now.isAfter(expireTime)) {
                if (schedule.getAutoDelete() != null && schedule.getAutoDelete()) {
                    log.info("Auto-deleting expired schedule: {}", schedule.getTitle());
                    scheduleRepository.delete(schedule);
                } else {
                    sendExpiredNotification(schedule);
                }
            }
        }
    }

    private void sendExpiredNotification(Schedule schedule) {
        User user = userRepository.findById(schedule.getUserId()).orElse(null);
        if (user == null || user.getQqNumber() == null) {
            return;
        }

        String message = "【日程过期提醒】\n您的日程「" + schedule.getTitle() + "」";
        if (schedule.getEndTime() != null) {
            message += "已于 " + schedule.getEndTime().format(DT_FMT) + " 过期";
        } else {
            message += "已于 " + schedule.getStartTime().format(DT_FMT) + " 过期";
        }
        message += "，请及时处理。";

        String result = sendToQQ(user.getNapcatUserId(), message);

        ReminderLog log = ReminderLog.builder()
                .userId(user.getId())
                .targetType("SCHEDULE_EXPIRED")
                .targetId(schedule.getId())
                .content(message)
                .sendStatus(result != null ? "SUCCESS" : "FAILED")
                .response(result)
                .sendTime(LocalDateTime.now())
                .build();
        reminderLogRepository.save(log);

        if (result != null) {
            schedule.setExpiredNotified(true);
            scheduleRepository.save(schedule);
        }
    }
}
