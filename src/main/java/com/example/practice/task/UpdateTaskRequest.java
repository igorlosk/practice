package com.example.practice.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateTaskRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @NotBlank
        String status,

        @NotBlank
        String priority,

        Long assigneeId,
        Instant dueDate
) {
}
