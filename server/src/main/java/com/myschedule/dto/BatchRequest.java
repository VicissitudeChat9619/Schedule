package com.myschedule.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchRequest {
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
