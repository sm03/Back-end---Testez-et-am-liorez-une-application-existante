package com.openclassrooms.etudiant.entities;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class UserTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final LocalDateTime now = LocalDateTime.now();
    private static final LocalDateTime updatedAt = now.plusSeconds(3600);
    private static final User user = new User(
            1L,
            FIRST_NAME,
            LAST_NAME,
            LOGIN,
            PASSWORD,
            now,
            updatedAt
    );

    @Test
    public void testGetters() {
        assert user.getFirstName().equals(FIRST_NAME);
        assert user.getLastName().equals(LAST_NAME);
        assert user.getLogin().equals(LOGIN);
        assert user.getPassword().equals(PASSWORD);
        assert user.getId().equals(1L);
    }

    @Test
    public void testUserDetailsMethods() {
        assert user.getAuthorities().isEmpty();
        assert user.getUsername().equals(LOGIN);
        assert user.isAccountNonExpired();
        assert user.isAccountNonLocked();
        assert user.isCredentialsNonExpired();
        assert user.isEnabled();
        assert user.getCreated_at().equals(now);
        assert user.getUpdated_at().equals(updatedAt);
    }
}
