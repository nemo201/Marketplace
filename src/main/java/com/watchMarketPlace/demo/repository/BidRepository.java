package com.watchMarketPlace.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.watchMarketPlace.demo.model.Bid;
import com.watchMarketPlace.demo.model.Item;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByItemOrderByAmountDesc(Item item);
    Bid findTopByItemOrderByAmountDesc(Item item);
} 