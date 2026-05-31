package com.myschedule.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ArrangeRequest {
    @NotEmpty(message = "待办ID列表不能为空")
    private List<Long> ids;
}
