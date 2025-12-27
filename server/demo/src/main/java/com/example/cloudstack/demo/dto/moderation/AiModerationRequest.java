package com.example.cloudstack.demo.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiModerationRequest {
    private String type;
    private String title;

    @NotBlank
    private String content;
}
