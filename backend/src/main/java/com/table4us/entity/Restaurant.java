package com.table4us.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name = "restaurant")
public class Restaurant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 160) private String name;
    @Column(columnDefinition = "text") private String description;
    @Column(nullable = false, length = 80) private String cuisine;
    @Column(nullable = false, length = 300) private String address;
    @Column(nullable = false, length = 100) private String city;
    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 9, scale = 6) private BigDecimal longitude;
    @Column(name = "price_level", nullable = false) private short priceLevel;
    @Column(name = "average_rating", nullable = false, precision = 2, scale = 1) private BigDecimal averageRating;
    @Column(name = "booking_url", length = 500) private String bookingUrl;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;
    protected Restaurant() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCuisine() { return cuisine; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public short getPriceLevel() { return priceLevel; }
    public BigDecimal getAverageRating() { return averageRating; }
    public String getBookingUrl() { return bookingUrl; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
}
