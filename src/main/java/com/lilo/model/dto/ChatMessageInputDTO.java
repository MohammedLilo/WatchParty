package com.lilo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageInputDTO {
    @NotBlank(message = "content is required")
    private String content;
}
