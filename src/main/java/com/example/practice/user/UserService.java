package com.example.practice.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    @Transactional
    public User createUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        var user = new User(username, email);
        return userRepository.save(user);
    }

    // READ
    @Transactional(readOnly = true)
    public Optional findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    // UPDATE
    @Transactional
    public User updateDisplayName(Long id, String displayName) {
        var user = getById(id);
        user.setDisplayName(displayName);
        return userRepository.save(user);
    }

    // DELETE
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
