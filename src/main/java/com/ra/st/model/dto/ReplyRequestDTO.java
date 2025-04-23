package com.ra.st.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequestDTO {
    @NotBlank(message = "Phản hồi không được để trống!")
    private String reply;
}