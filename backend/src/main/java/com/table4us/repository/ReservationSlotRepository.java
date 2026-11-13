package com.table4us.repository;

import com.table4us.entity.ReservationSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
    List<ReservationSlot> findByRestaurantIdAndStartsAtBetweenOrderByStartsAt(Long restaurantId, OffsetDateTime from, OffsetDateTime to);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ReservationSlot s where s.id = :id")
    Optional<ReservationSlot> findByIdForUpdate(@Param("id") Long id);
}
