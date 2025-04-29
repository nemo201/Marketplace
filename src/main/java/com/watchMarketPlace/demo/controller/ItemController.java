package com.watchMarketPlace.demo.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.watchMarketPlace.demo.model.Item;
import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.repository.UserRepository;
import com.watchMarketPlace.demo.security.JwtTokenProvider;
import com.watchMarketPlace.demo.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    // Earlier anyone could creat a sale post
    // @PostMapping
    // public Item createItem(@RequestBody Item item) {
    //     return itemService.createItem(item);
    // }

    @PostMapping
    public Item create(@RequestBody Item item, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid or expiredtoken");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email);
        
        item.setUser(user);
        return itemService.createItem(item);
    }
    
    @GetMapping
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }
}
