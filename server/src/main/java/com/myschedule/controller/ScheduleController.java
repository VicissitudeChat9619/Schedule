package com.myschedule.controller;

import com.myschedule.dto.ApiResponse;
import com.myschedule.dto.ScheduleRequest;
import com.myschedule.entity.Schedule;
import com.myschedule.entity.User;
import com.myschedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Schedule>>> list(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Schedule> schedules = scheduleService.listByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> create(Authentication authentication,
                                                         @Valid @RequestBody ScheduleRequest request) {
        User user = (User) authentication.getPrincipal();
        Schedule schedule = scheduleService.create(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> update(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody ScheduleRequest request) {
        User user = (User) authentication.getPrincipal();
        try {
            Schedule schedule = scheduleService.update(user.getId(), id, request);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication,
                                                     @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            scheduleService.delete(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getById(Authentication authentication,
                                                          @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            Schedule schedule = scheduleService.getById(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
