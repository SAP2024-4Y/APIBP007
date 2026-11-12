package com.table4us.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "full_name", nullable = false, length = 120) private String fullName;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;
    protected AppUser() {}
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
