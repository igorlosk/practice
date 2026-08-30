package com.example.practice.task;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    List<Task> findByProjectIdAndStatus(Long projectId, String status);

    List<Task> findByAssigneeId(Long assigneeId);

    long countByProjectIdAndStatus(Long projectId, String status);
}
