package com.example.practice.task;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
        @NotBlank(message = "Status must not be blank")
        String status
) {
}
