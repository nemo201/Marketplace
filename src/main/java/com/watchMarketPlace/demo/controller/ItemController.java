package com.watchMarketPlace.demo.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.watchMarketPlace.demo.model.Item;
import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.model.Bid;
import com.watchMarketPlace.demo.repository.UserRepository;
import com.watchMarketPlace.demo.security.JwtTokenProvider;
import com.watchMarketPlace.demo.service.ItemService;

/**
 * REST controller for managing watch marketplace items.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String AUTH_HEADER_PREFIX = "Bearer ";

    @Autowired
    private ItemService itemService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        try {
            return ResponseEntity.ok("Database connection is working!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Database connection error: " + e.getMessage());
        }
    }

    /**
     * Create a new item listing.
     *
     * @param item The item details
     * @param request The HTTP request containing the authentication token
     * @return The created item
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        item.setUser(user);
        Item createdItem = itemService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    /**
     * Get all active items.
     *
     * @return List of all items
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems(HttpServletRequest request) {
        getAuthenticatedUser(request); // This will validate the token
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Place a bid on an item.
     *
     * @param itemId The ID of the item
     * @param amount The bid amount
     * @param request The HTTP request containing the authentication token
     * @return The created bid
     */
    @PostMapping("/{itemId}/bid")
    public ResponseEntity<Bid> placeBid(
            @PathVariable Long itemId,
            @RequestParam double amount,
            HttpServletRequest request) {
        validateBidAmount(amount);
        User user = getAuthenticatedUser(request);
        Bid bid = itemService.placeBid(itemId, user, amount);
        return ResponseEntity.ok(bid);
    }

    /**
     * Buy an item immediately at the buy-now price.
     *
     * @param itemId The ID of the item
     * @param request The HTTP request containing the authentication token
     * @return The purchased item
     */
    @PostMapping("/{itemId}/buy-now")
    public ResponseEntity<Item> buyNow(
            @PathVariable Long itemId,
            HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Item item = itemService.buyNow(itemId, user);
        return ResponseEntity.ok(item);
    }

    /**
     * Get all bids for an item.
     *
     * @param itemId The ID of the item
     * @return List of bids for the item
     */
    @GetMapping("/{itemId}/bids")
    public ResponseEntity<List<Bid>> getItemBids(@PathVariable Long itemId) {
        List<Bid> bids = itemService.getItemBids(itemId);
        return ResponseEntity.ok(bids);
    }

    /**
     * Update an existing item.
     *
     * @param itemId The ID of the item to update
     * @param updatedItem The updated item details
     * @param request The HTTP request containing the authentication token
     * @return The updated item
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long itemId,
            @RequestBody Item updatedItem,
            HttpServletRequest request) {
        validateItemUpdate(updatedItem);
        User user = getAuthenticatedUser(request);
        Item item = itemService.updateItem(itemId, updatedItem, user);
        return ResponseEntity.ok(item);
    }

    /**
     * Delete an item.
     *
     * @param itemId The ID of the item to delete
     * @param request The HTTP request containing the authentication token
     * @return No content response
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        itemService.deleteItem(itemId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the authenticated user from the request.
     *
     * @param request The HTTP request containing the authentication token
     * @return The authenticated user
     * @throws ResponseStatusException if authentication fails
     */
    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing Authorization header");
        }

        String token = authHeader.substring(AUTH_HEADER_PREFIX.length());
        
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        return user;
    }

    /**
     * Validate the bid amount.
     *
     * @param amount The bid amount to validate
     * @throws ResponseStatusException if the bid amount is invalid
     */
    private void validateBidAmount(double amount) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bid amount must be greater than zero");
        }
    }

    /**
     * Validate the item update request.
     *
     * @param item The item to validate
     * @throws ResponseStatusException if the item update is invalid
     */
    private void validateItemUpdate(Item item) {
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item details cannot be null");
        }
        if (item.getTitle() != null && item.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }
        if (item.getDescription() != null && item.getDescription().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot be empty");
        }
        if (item.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative");
        }
    }
}
