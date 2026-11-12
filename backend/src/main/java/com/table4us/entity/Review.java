package com.table4us.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "review")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "restaurant_id", nullable = false) private Restaurant restaurant;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private AppUser user;
    @Column(nullable = false) private short rating;
    @Column(columnDefinition = "text") private String comment;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;
    protected Review() {}
    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public AppUser getUser() { return user; }
    public short getRating() { return rating; }
    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
