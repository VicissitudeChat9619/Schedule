package com.myschedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private Integer priority = 2;

    private LocalDateTime dueTime;

    private Integer reminderBeforeMinutes = 30;

    private Boolean autoDelete = false;
}
