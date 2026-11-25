
package com.shawbin.user_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "riya@example.com";
    private static final long EXPIRATION = 3600000L;

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(new byte[32]);

    private static final String SECRET =
            Encoders.BASE64.encode(SECRET_KEY.getEncoded());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void generateTokenShouldReturnValidJwt() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateTokenShouldContainUserIdAsSubject() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("1", claims.getSubject());
    }

    @Test
    void generateTokenShouldContainEmailClaim() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(EMAIL, claims.get("email", String.class));
    }

    @Test
    void generateTokenShouldContainIssuedAtDate() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getIssuedAt().before(new Date())
                || claims.getIssuedAt().equals(new Date()));
    }


    @Test
    void generateTokenShouldHaveCorrectExpirationTime() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAtSeconds =
                claims.getIssuedAt().getTime() / 1000;

        long expirationSeconds =
                claims.getExpiration().getTime() / 1000;

        long expectedExpirationSeconds =
                issuedAtSeconds + (EXPIRATION / 1000);

        assertEquals(
                expectedExpirationSeconds,
                expirationSeconds
        );
    }


    @Test
    void generateTokenShouldCreateSignedToken() {

        String token = jwtService.generateToken(USER_ID, EMAIL);

        assertDoesNotThrow(() ->
                Jwts.parser()
                        .verifyWith(SECRET_KEY)
                        .build()
                        .parseSignedClaims(token)
        );
    }
}
