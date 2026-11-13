package com.table4us.service;

import com.table4us.entity.Reservation;
import com.table4us.entity.ReservationSlot;
import com.table4us.repository.ReservationRepository;
import com.table4us.repository.ReservationSlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private final ReservationSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationSlotRepository slotRepository, ReservationRepository reservationRepository) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation book(Long slotId, String guestName, String guestEmail, short partySize, String specialRequest) {
        ReservationSlot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation time was not found"));
        slot.reserve(partySize);
        return reservationRepository.save(new Reservation(slot, guestName, guestEmail, partySize, specialRequest));
    }

    @Transactional
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation was not found"));
        if (reservation.getStatus() == Reservation.Status.CANCELLED) return reservation;
        ReservationSlot slot = slotRepository.findByIdForUpdate(reservation.getSlot().getId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation time was not found"));
        slot.release(reservation.getPartySize());
        reservation.cancel();
        return reservation;
    }
}
