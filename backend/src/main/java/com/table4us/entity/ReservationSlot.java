package com.table4us.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "reservation_slot")
public class ReservationSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "restaurant_id", nullable = false) private Restaurant restaurant;
    @Column(name = "starts_at", nullable = false) private OffsetDateTime startsAt;
    @Column(name = "duration_minutes", nullable = false) private short durationMinutes;
    @Column(name = "capacity_people", nullable = false) private int capacityPeople;
    @Column(name = "reserved_people", nullable = false) private int reservedPeople;
    protected ReservationSlot() {}
    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public short getDurationMinutes() { return durationMinutes; }
    public int getCapacityPeople() { return capacityPeople; }
    public int getReservedPeople() { return reservedPeople; }
    public int getAvailablePeople() { return capacityPeople - reservedPeople; }
    public void reserve(int people) {
        if (people <= 0 || people > getAvailablePeople()) throw new IllegalStateException("Not enough seats are available");
        reservedPeople += people;
    }
    public void release(int people) {
        if (people <= 0 || people > reservedPeople) throw new IllegalStateException("Cannot release more seats than reserved");
        reservedPeople -= people;
    }
}
