package com.table4us.service;
import com.table4us.entity.*; import com.table4us.repository.*; import org.junit.jupiter.api.Test; import java.util.Optional; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class ReservationServiceTest {
 @Test void bookingLocksSlotAndSavesReservation(){var slots=mock(ReservationSlotRepository.class);var reservations=mock(ReservationRepository.class);var slot=mock(ReservationSlot.class);when(slots.findByIdForUpdate(9L)).thenReturn(Optional.of(slot));when(reservations.save(any(Reservation.class))).thenAnswer(i->i.getArgument(0));var result=new ReservationService(slots,reservations).book(9L,"Asha","asha@example.test",(short)4,"window seat");assertEquals(Reservation.Status.CONFIRMED,result.getStatus());verify(slot).reserve(4);verify(reservations).save(any(Reservation.class));}
 @Test void missingSlotFails(){var slots=mock(ReservationSlotRepository.class);when(slots.findByIdForUpdate(7L)).thenReturn(Optional.empty());assertThrows(IllegalArgumentException.class,()->new ReservationService(slots,mock(ReservationRepository.class)).book(7L,"A","a@example.test",(short)2,null));}
}
