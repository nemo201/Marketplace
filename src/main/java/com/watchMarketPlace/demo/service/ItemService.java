package com.watchMarketPlace.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.watchMarketPlace.demo.model.Item;
import com.watchMarketPlace.demo.model.Bid;
import com.watchMarketPlace.demo.model.User;
import com.watchMarketPlace.demo.repository.ItemRepository;
import com.watchMarketPlace.demo.repository.BidRepository;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BidRepository bidRepository;

    public Item createItem(Item item) {
        if (item.isBiddingEnabled()) {
            if (item.getStartingPrice() == null || item.getBuyNowPrice() == null) {
                throw new RuntimeException("Starting price and buy now price are required for items with bidding enabled");
            }
            if (item.getEndTime() == null) {
                throw new RuntimeException("End time is required for items with bidding enabled");
            }
            item.setCurrentPrice(item.getStartingPrice());
        } else {
            // For non-bidding items, set the price as the buy now price
            item.setBuyNowPrice(item.getPrice());
        }
        return itemRepository.save(item);
    }
    
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Bid placeBid(Long itemId, User user, double amount) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.isBiddingEnabled()) {
            throw new RuntimeException("Bidding is not enabled for this item");
        }

        if (!item.isActive()) {
            throw new RuntimeException("Item is no longer active");
        }

        if (LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new RuntimeException("Bidding has ended for this item");
        }

        if (amount <= item.getCurrentPrice()) {
            throw new RuntimeException("Bid amount must be higher than current price");
        }

        if (amount >= item.getBuyNowPrice()) {
            throw new RuntimeException("Bid amount must be less than buy now price");
        }

        Bid bid = new Bid();
        bid.setItem(item);
        bid.setUser(user);
        bid.setAmount(amount);

        item.setCurrentPrice(amount);
        itemRepository.save(item);

        return bidRepository.save(bid);
    }

    @Transactional
    public Item buyNow(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.isActive()) {
            throw new RuntimeException("Item is no longer active");
        }

        if (item.isBiddingEnabled() && LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new RuntimeException("Item is no longer available for purchase");
        }

        item.setActive(false);
        return itemRepository.save(item);
    }

    public List<Bid> getItemBids(Long itemId) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (!item.isBiddingEnabled()) {
            throw new RuntimeException("Bidding is not enabled for this item");
        }
        
        return bidRepository.findByItemOrderByAmountDesc(item);
    }
}