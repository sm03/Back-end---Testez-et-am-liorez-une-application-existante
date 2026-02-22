package com.openclassrooms.etudiant.configuration.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    private static final long EXPIRATION = 60 * 60;

    @Test
    public void testExpirationTimes() {
        assertThat(JwtService.getExpirationMs()).isEqualTo(EXPIRATION * 1000);
        assertThat(JwtService.getExpirationSeconds()).isEqualTo(EXPIRATION);
    }
}