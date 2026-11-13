package com.table4us.api;
import com.table4us.entity.*; import com.table4us.repository.*; import com.table4us.service.ReservationService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*; import org.springframework.http.HttpStatus; import org.springframework.web.server.ResponseStatusException;
import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/v1")
public class ReservationController {
 private final ReservationSlotRepository slots; private final ReservationService service;
 public ReservationController(ReservationSlotRepository slots,ReservationService service){this.slots=slots;this.service=service;}
 @GetMapping("/restaurants/{restaurantId}/slots") public List<Map<String,Object>> slots(@PathVariable Long restaurantId,@RequestParam LocalDate date){
  var from=date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();var to=date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
  return slots.findByRestaurantIdAndStartsAtBetweenOrderByStartsAt(restaurantId,from,to).stream().map(s->Map.<String,Object>of("id",s.getId(),"startsAt",s.getStartsAt(),"availablePeople",s.getAvailablePeople(),"durationMinutes",s.getDurationMinutes())).toList();
 }
 @PostMapping("/reservations") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> book(@Valid @RequestBody Booking b){try{var r=service.book(b.slotId(),b.guestName(),b.guestEmail(),b.partySize().shortValue(),b.specialRequest());return Map.of("id",r.getId(),"status",r.getStatus(),"partySize",r.getPartySize(),"sampleBooking",true);}catch(IllegalStateException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"SLOT_CAPACITY_EXCEEDED");}catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"SLOT_NOT_FOUND");}}
 public record Booking(@NotNull Long slotId,@NotBlank @Size(max=120) String guestName,@NotBlank @Email String guestEmail,@NotNull @Min(1) @Max(30) Integer partySize,@Size(max=500) String specialRequest){}
}
