package com.table4us.service;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.Date;
@Service public class JwtService {
 private final SecretKey key=Keys.hmacShaKeyFor(System.getenv().getOrDefault("JWT_SECRET","local-demo-secret-change-this-before-sharing-123456").getBytes(StandardCharsets.UTF_8));
 public String issue(String email){Instant now=Instant.now();return Jwts.builder().subject(email).claim("role","USER").issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600))).signWith(key).compact();}
 public Claims parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
}
