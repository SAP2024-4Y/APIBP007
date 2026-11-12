package com.table4us.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class JwtServiceTest {
 @Test void issuedTokenContainsDemoIdentityAndExpires(){var jwt=new JwtService();var claims=jwt.parse(jwt.issue("student@example.test"));assertEquals("student@example.test",claims.getSubject());assertEquals("USER",claims.get("role"));assertNotNull(claims.getExpiration());}
 @Test void invalidSignatureIsRejected(){var jwt=new JwtService();assertThrows(Exception.class,()->jwt.parse("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0."));}
}
