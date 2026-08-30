package com.example.practice.project;

import com.example.practice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);
    List<Project> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
