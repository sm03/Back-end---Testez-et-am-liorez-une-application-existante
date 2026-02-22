package com.openclassrooms.etudiant.dto;

import org.junit.jupiter.api.Test;

public class RegisterDTOTest {

    @Test
    public void testGettersAndSetters() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setLastName("Doe");
        registerDTO.setLogin("johndoe");
        registerDTO.setPassword("password");

        assert registerDTO.getFirstName().equals("John");
        assert registerDTO.getLastName().equals("Doe");
        assert registerDTO.getLogin().equals("johndoe");
        assert registerDTO.getPassword().equals("password");
    }
}
