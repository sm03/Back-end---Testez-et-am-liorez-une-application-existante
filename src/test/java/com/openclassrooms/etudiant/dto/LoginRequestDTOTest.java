package com.openclassrooms.etudiant.dto;

import org.junit.jupiter.api.Test;

public class LoginRequestDTOTest {

    @Test
    public void testGettersAndSetters() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("johndoe");
        loginRequestDTO.setPassword("password");

        assert loginRequestDTO.getLogin().equals("johndoe");
        assert loginRequestDTO.getPassword().equals("password");
    }
}