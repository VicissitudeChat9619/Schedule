package com.myschedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindQQRequest {
    @NotBlank(message = "QQ号不能为空")
    private String qqNumber;
}
