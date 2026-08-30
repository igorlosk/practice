package com.example.practice.task;

import com.example.practice.project.ProjectRepository;
import com.example.practice.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
public class TaskService {

    private static final Set VALID_STATUSES =
            Set.of("TODO", "IN_PROGRESS", "DONE", "CANCELLED");

    private static final Set VALID_PRIORITIES =
            Set.of("LOW", "MEDIUM", "HIGH");

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------

    @Transactional
    public TaskResponse createTask(Long creatorId, CreateTaskRequest request) {
        var project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + request.projectId()));

        var creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + creatorId));

        var task = new Task(request.title(), project, creator);
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assignee not found: " + request.assigneeId()));
            task.setAssignee(assignee);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    // --------------------------------------------------
    // READ
    // --------------------------------------------------

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(TaskResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getProjectTasks(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return taskRepository.findByProjectId(projectId, pageable)
                .map(TaskResponse::from);
    }

    // --------------------------------------------------
    // UPDATE
    // --------------------------------------------------

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        var task = getTaskEntity(taskId);

        validateStatus(request.status());
        validatePriority(request.priority());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assignee not found: " + request.assigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse changeStatus(Long taskId, ChangeStatusRequest request) {
        validateStatus(request.status());

        var task = getTaskEntity(taskId);
        var oldStatus = task.getStatus();

        // Проверяем допустимость перехода
        validateStatusTransition(oldStatus, request.status());

        task.setStatus(request.status());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        var task = getTaskEntity(taskId);

        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found: " + request.assigneeId()));
            task.setAssignee(assignee);
        } else {
            // null = снять исполнителя
            task.setAssignee(null);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    // --------------------------------------------------
    // Вспомогательные методы
    // --------------------------------------------------

    private Task getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Allowed: " + VALID_STATUSES);
        }
    }

    private void validatePriority(String priority) {
        if (!VALID_PRIORITIES.contains(priority)) {
            throw new IllegalArgumentException(
                    "Invalid priority: " + priority + ". Allowed: " + VALID_PRIORITIES);
        }
    }

    private void validateStatusTransition(String from, String to) {
        // CANCELLED -> любой статус не разрешён
        if ("CANCELLED".equals(from)) {
            throw new IllegalArgumentException(
                    "Cannot change status of cancelled task");
        }
        // DONE -> только CANCELLED разрешён (переоткрытие через IN_PROGRESS)
        if ("DONE".equals(from) && !"CANCELLED".equals(to) && !"IN_PROGRESS".equals(to)) {
            throw new IllegalArgumentException(
                    "Completed task can only be cancelled or reopened");
        }
    }
}
