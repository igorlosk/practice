package com.example.practice.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /api/tasks
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        // TODO: заменить на реального пользователя из SecurityContext
        Long creatorId = 1L;
        var task = taskService.createTask(creatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    // GET /api/projects/{projectId}/tasks?page=0&size=20&sort=createdAt,desc
    @GetMapping("/by-project/{projectId}")
    public Page<TaskResponse> getProjectTasks(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return taskService.getProjectTasks(projectId, pageable);
    }

    // PUT /api/tasks/{id}
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    // PATCH /api/tasks/{id}/status
    @PatchMapping("/{id}/status")
    public TaskResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return taskService.changeStatus(id, request);
    }

    // PATCH /api/tasks/{id}/assignee
    @PatchMapping("/{id}/assignee")
    public TaskResponse assignTask(
            @PathVariable Long id,
            @RequestBody AssignTaskRequest request) {
        return taskService.assignTask(id, request);
    }

    // DELETE /api/tasks/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
