package com.smartFood.donation.service;

import com.smartFood.donation.models.User;
import com.smartFood.donation.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail().trim());
        if (existingUser.isPresent()) {
            return false;
        }
        user.setEmail(user.getEmail().trim().toLowerCase());
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("DONOR");
        }
        userRepository.save(user);
        return true;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailAndPassword(email.trim().toLowerCase(), password);
    }
}