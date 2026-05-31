package com.myschedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String repeatType = "NONE";

    private Integer reminderBeforeMinutes = 15;

    private Boolean autoDelete = false;
}
