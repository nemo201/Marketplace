package com.watchMarketPlace.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;  // Regular price for non-bidding items

    @Column(nullable = false)
    private boolean biddingEnabled = false;

    @Column
    private Double startingPrice;  // Starting price for bidding items

    @Column
    private Double buyNowPrice;  // Buy now price for bidding items

    @Column
    private Double currentPrice;  // Current highest bid for bidding items

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean isActive = true;

    @JsonIgnore
    @OneToMany(mappedBy = "item")
    private List<Bid> bids;

    private LocalDateTime createdAt = LocalDateTime.now();
}
