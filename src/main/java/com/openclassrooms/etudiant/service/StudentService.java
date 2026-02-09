package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;

import java.util.List;

public interface StudentService {

    StudentResponseDTO createStudent(StudentRequestDTO dto);

    List<StudentResponseDTO> getAllStudents();

    StudentResponseDTO getStudentById(Long id);

    StudentResponseDTO updateStudent(Long id, StudentRequestDTO dto);

    void deleteStudent(Long id);
}

