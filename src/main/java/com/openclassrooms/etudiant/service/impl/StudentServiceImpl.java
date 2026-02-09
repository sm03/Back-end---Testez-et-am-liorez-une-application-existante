package com.openclassrooms.etudiant.service.impl;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    public StudentServiceImpl(StudentRepository repository) {
        this.repository = repository;
    }

    private StudentResponseDTO mapToDTO(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone()
        );
    }

    @Override
    public StudentResponseDTO createStudent(StudentRequestDTO dto) {
        Student student = new Student(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getPhone()
        );
        repository.save(student);
        return mapToDTO(student);
    }

    @Override
    public List<StudentResponseDTO> getAllStudents() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StudentResponseDTO getStudentById(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        return mapToDTO(student);
    }

    @Override
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO dto) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());

        repository.save(student);
        return mapToDTO(student);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        repository.delete(student);
    }
}
