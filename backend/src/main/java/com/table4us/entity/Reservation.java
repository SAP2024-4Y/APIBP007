package com.table4us.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "reservation")
public class Reservation {
    public enum Status { CONFIRMED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "slot_id", nullable = false) private ReservationSlot slot;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private AppUser user;
    @Column(name = "guest_name", nullable = false, length = 120) private String guestName;
    @Column(name = "guest_email", nullable = false) private String guestEmail;
    @Column(name = "party_size", nullable = false) private short partySize;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "special_request", length = 500) private String specialRequest;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;
    protected Reservation() {}
    public Reservation(ReservationSlot slot, String guestName, String guestEmail, short partySize, String specialRequest) {
        this.slot = slot; this.guestName = guestName; this.guestEmail = guestEmail;
        this.partySize = partySize; this.specialRequest = specialRequest; this.status = Status.CONFIRMED;
    }
    public Long getId() { return id; }
    public ReservationSlot getSlot() { return slot; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public short getPartySize() { return partySize; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void cancel() { this.status = Status.CANCELLED; }
}
