package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StudentRequestDTO { // Création ou mise à jour d'un étudiant

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    private String phone;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    static public StudentRequestDTO buildRequestDTO(
        String firstName,
        String lastName,
        String email,
        String phone
    ) {
        var studentRequestDTO = new StudentRequestDTO();
        studentRequestDTO.setFirstName(firstName);
        studentRequestDTO.setLastName(lastName);
        studentRequestDTO.setEmail(email);
        studentRequestDTO.setPhone(phone);
        return studentRequestDTO;
    }
}

