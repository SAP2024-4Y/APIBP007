package com.table4us.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "menu_item")
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "restaurant_id", nullable = false) private Restaurant restaurant;
    @Column(nullable = false, length = 160) private String name;
    @Column(columnDefinition = "text") private String description;
    @Column(length = 80) private String category;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(nullable = false) private boolean vegetarian;
    @Column(nullable = false) private boolean available;
    protected MenuItem() {}
    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isAvailable() { return available; }
}
