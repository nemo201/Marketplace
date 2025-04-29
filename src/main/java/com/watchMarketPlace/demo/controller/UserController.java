package com.watchMarketPlace.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.security.JwtTokenProvider;
import com.watchMarketPlace.demo.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
public class UserController { 
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData) {
        User user = userService.authenticateUser(loginData.get("email"), loginData.get("password"));
        if (user != null) {
            String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return response;
        } else {
            throw new RuntimeException("Invalid login credentials");
        }
    }
    // @GetMapping("/{id}")
    // public User getUserById(@PathVariable Long id) {
    //     return userService.getUserById(id);
    // }
    // @GetMapping("/all")
    // public List<User> getAllUsers() {
    //     return userService.getAllUsers();
    // }   
}
