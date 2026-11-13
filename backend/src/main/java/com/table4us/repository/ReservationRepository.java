package com.table4us.repository;

import com.table4us.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(String guestEmail);
    List<Reservation> findBySlotRestaurantIdAndStatus(Long restaurantId, Reservation.Status status);
}
