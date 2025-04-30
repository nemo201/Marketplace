package com.watchMarketPlace.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.watchMarketPlace.demo.model.Item;
import com.watchMarketPlace.demo.model.Bid;
import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.repository.ItemRepository;
import com.watchMarketPlace.demo.repository.BidRepository;

/**
 * Service class for managing watch marketplace items.
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BidRepository bidRepository;

    /**
     * Create a new item listing.
     *
     * @param item The item to create
     * @return The created item
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public Item createItem(Item item) {
        validateItemCreation(item);
        
        if (item.isBiddingEnabled()) {
            item.setCurrentPrice(item.getStartingPrice());
        } else {
            item.setBuyNowPrice(item.getPrice());
        }
        
        return itemRepository.save(item);
    }
    
    /**
     * Get all items.
     *
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Place a bid on an item.
     *
     * @param itemId The ID of the item
     * @param user The user placing the bid
     * @param amount The bid amount
     * @return The created bid
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public Bid placeBid(Long itemId, User user, double amount) {
        Item item = findActiveItem(itemId);
        validateBid(item, amount);

        Bid bid = new Bid();
        bid.setItem(item);
        bid.setUser(user);
        bid.setAmount(amount);

        item.setCurrentPrice(amount);
        itemRepository.save(item);

        return bidRepository.save(bid);
    }

    /**
     * Buy an item immediately.
     *
     * @param itemId The ID of the item
     * @param user The user buying the item
     * @return The purchased item
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public Item buyNow(Long itemId, User user) {
        Item item = findActiveItem(itemId);
        validateBuyNow(item);

        item.setActive(false);
        return itemRepository.save(item);
    }

    /**
     * Get all bids for an item.
     *
     * @param itemId The ID of the item
     * @return List of bids for the item
     * @throws ResponseStatusException if validation fails
     */
    public List<Bid> getItemBids(Long itemId) {
        Item item = findActiveItem(itemId);
        
        if (!item.isBiddingEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bidding is not enabled for this item");
        }
        
        return bidRepository.findByItemOrderByAmountDesc(item);
    }

    /**
     * Update an existing item.
     *
     * @param itemId The ID of the item to update
     * @param updatedItem The updated item details
     * @param user The user updating the item
     * @return The updated item
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public Item updateItem(Long itemId, Item updatedItem, User user) {
        Item existingItem = findActiveItem(itemId);
        validateItemUpdate(existingItem, user);

        updateItemProperties(existingItem, updatedItem);
        return itemRepository.save(existingItem);
    }

    /**
     * Delete an item.
     *
     * @param itemId The ID of the item to delete
     * @param user The user deleting the item
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public void deleteItem(Long itemId, User user) {
        Item item = findActiveItem(itemId);
        validateItemDeletion(item, user);

        List<Bid> bids = bidRepository.findByItemOrderByAmountDesc(item);
        bidRepository.deleteAll(bids);
        itemRepository.delete(item);
    }

    private Item findActiveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (!item.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is no longer active");
        }

        return item;
    }

    private void validateItemCreation(Item item) {
        if (item.isBiddingEnabled()) {
            if (item.getStartingPrice() == null || item.getBuyNowPrice() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Starting price and buy now price are required for items with bidding enabled");
            }
            if (item.getEndTime() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "End time is required for items with bidding enabled");
            }
            if (item.getEndTime().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "End time must be in the future");
            }
            if (item.getStartingPrice() >= item.getBuyNowPrice()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Starting price must be less than buy now price");
            }
        }
    }

    private void validateBid(Item item, double amount) {
        if (!item.isBiddingEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bidding is not enabled for this item");
        }

        if (LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bidding has ended for this item");
        }

        if (amount <= item.getCurrentPrice()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bid amount must be higher than current price");
        }

        if (amount >= item.getBuyNowPrice()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bid amount must be less than buy now price");
        }
    }

    private void validateBuyNow(Item item) {
        if (item.isBiddingEnabled() && LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is no longer available for purchase");
        }
    }

    private void validateItemUpdate(Item item, User user) {
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this item");
        }
    }

    private void validateItemDeletion(Item item, User user) {
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this item");
        }
    }

    private void updateItemProperties(Item existingItem, Item updatedItem) {
        if (updatedItem.getTitle() != null) {
            existingItem.setTitle(updatedItem.getTitle());
        }
        if (updatedItem.getDescription() != null) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getPrice() > 0) {
            existingItem.setPrice(updatedItem.getPrice());
        }
        
        if (existingItem.isBiddingEnabled()) {
            if (updatedItem.getStartingPrice() != null) {
                existingItem.setStartingPrice(updatedItem.getStartingPrice());
            }
            if (updatedItem.getBuyNowPrice() != null) {
                existingItem.setBuyNowPrice(updatedItem.getBuyNowPrice());
            }
            if (updatedItem.getEndTime() != null) {
                existingItem.setEndTime(updatedItem.getEndTime());
            }
        }
    }
}