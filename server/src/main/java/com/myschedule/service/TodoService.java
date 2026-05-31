package com.myschedule.service;

import com.myschedule.dto.TodoRequest;
import com.myschedule.entity.Schedule;
import com.myschedule.entity.Todo;
import com.myschedule.repository.ScheduleRepository;
import com.myschedule.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final ScheduleRepository scheduleRepository;

    public TodoService(TodoRepository todoRepository, ScheduleRepository scheduleRepository) {
        this.todoRepository = todoRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<Todo> listByUser(Long userId) {
        return todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Todo> listByStatus(Long userId, String status) {
        if (status != null && !status.isEmpty()) {
            return todoRepository.findByUserIdAndStatusOrderByDueTimeAsc(userId, status);
        }
        return todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Todo> listUnarranged(Long userId) {
        return todoRepository.findByUserIdAndStatusOrderByDueTimeAsc(userId, "UNARRANGED");
    }

    public Todo create(Long userId, TodoRequest request) {
        Todo todo = Todo.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : 2)
                .dueTime(request.getDueTime())
                .reminderBeforeMinutes(request.getReminderBeforeMinutes() != null ? request.getReminderBeforeMinutes() : 30)
                .build();

        return todoRepository.save(todo);
    }

    public Todo update(Long userId, Long todoId, TodoRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }

        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        todo.setDueTime(request.getDueTime());
        todo.setReminderBeforeMinutes(request.getReminderBeforeMinutes() != null ? request.getReminderBeforeMinutes() : 30);

        return todoRepository.save(todo);
    }

    public void delete(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }

        todoRepository.delete(todo);
    }

    public Todo markDone(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }

        todo.setStatus("DONE");
        todo.setCompletedAt(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public Todo getById(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }

        return todo;
    }

    @Transactional
    public List<Schedule> arrange(Long userId, List<Long> ids) {
        List<Schedule> created = new ArrayList<>();

        for (Long id : ids) {
            Todo todo = todoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("待办不存在: " + id));

            if (!todo.getUserId().equals(userId)) {
                throw new RuntimeException("无权操作此待办: " + id);
            }

            Schedule schedule = Schedule.builder()
                    .userId(userId)
                    .title(todo.getTitle())
                    .description(todo.getDescription())
                    .startTime(todo.getDueTime() != null ? todo.getDueTime() : LocalDateTime.now().plusHours(1))
                    .endTime(todo.getDueTime() != null ? todo.getDueTime().plusHours(1) : null)
                    .reminderBeforeMinutes(todo.getReminderBeforeMinutes())
                    .build();

            created.add(scheduleRepository.save(schedule));

            todo.setStatus("ARRANGED");
            todoRepository.save(todo);
        }

        return created;
    }
}
