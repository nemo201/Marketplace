package com.watchMarketPlace.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.repository.UserRepository;
// import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // public User registerUser(User user) {
    //     return userRepository.save(user);
    // }
    
    // public User getUserById(Long id) {
    //     return userRepository.findById(id).orElse(null);
    // }
    
    // public List<User> getAllUsers() {
    //     return userRepository.findAll();
    // }

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}