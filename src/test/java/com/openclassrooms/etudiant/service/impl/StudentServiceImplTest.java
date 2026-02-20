package com.openclassrooms.etudiant.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;

import jakarta.persistence.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
public class StudentServiceImplTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentServiceImpl studentService;

/*    
    private StudentRequestDTO buildRequestDTO(
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
*/

    // =====================================================
    // CREATE
    // =====================================================

    @Test
    void createStudent_shouldSaveAndReturnDTO() {
        StudentRequestDTO dto = StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"
        );

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);

        StudentResponseDTO result = studentService.createStudent(dto);

        verify(repository).save(captor.capture());

        Student saved = captor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(saved.getPhone()).isEqualTo("0612345678");

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(result.getPhone()).isEqualTo("0612345678");
    }    

    // =====================================================
    // GET ALL
    // =====================================================

    @Test
    void getAllStudents_shouldReturnList() {
        Student johnStudent = new Student("John", "Doe", "john.doe@gmail.com", "0612345678", 1L);
        Student janeStudent = new Student("Jane", "Smith", "jane.smith@gmail.com", "0698765432", 2L);
        Long johnId = johnStudent.getId();
        Long janeId = janeStudent.getId();
        
        when(repository.findAll()).thenReturn(List.of(johnStudent, janeStudent));

        List<StudentResponseDTO> result = studentService.getAllStudents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(johnId);
        assertThat(result.get(1).getId()).isEqualTo(janeId);

        verify(repository).findAll();
    }

    @Test
    void getAllStudents_shouldReturnEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<StudentResponseDTO> result = studentService.getAllStudents();

        assertThat(result).isEmpty();
    }    

     // =====================================================
    // GET BY ID
    // =====================================================

    @Test
    void getStudentById_shouldReturnStudent() {
        Student johnStudent = new Student("John", "Doe", "john.doe@gmail.com", "0612345678", 1L);
        Student janeStudent = new Student("Jane", "Smith", "jane.smith@gmail.com", "0698765432", 2L);
        Long johnId = johnStudent.getId();
        Long janeId = janeStudent.getId();

        when(repository.findById(johnId)).thenReturn(Optional.of(johnStudent));
        when(repository.findById(janeId)).thenReturn(Optional.of(janeStudent));

        StudentResponseDTO result = studentService.getStudentById(janeId);
        assertThat(result.getId()).isEqualTo(janeId);
        assertThat(result.getEmail()).isEqualTo("jane.smith@gmail.com");
        verify(repository).findById(janeId);

        result = studentService.getStudentById(johnId);
        assertThat(result.getId()).isEqualTo(johnId);
        assertThat(result.getEmail()).isEqualTo("john.doe@gmail.com");
        verify(repository).findById(johnId);
    }

    @Test
    void getStudentById_shouldThrow_whenNotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found");

        verify(repository).findById(2L);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Test
    void updateStudent_shouldUpdateAndReturnDTO() {
        Student janeStudent = new Student("Jane", "Smith", "jane.smith@gmail.com", "0698765432", 2L);
        when(repository.findById(janeStudent.getId())).thenReturn(Optional.of(janeStudent));

        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName("Jane Lou");
        dto.setLastName("Smith");
        dto.setEmail("jane@smith.com");

        StudentResponseDTO result = studentService.updateStudent(janeStudent.getId(), dto);

        verify(repository).save(janeStudent);

        assertThat(janeStudent.getFirstName()).isEqualTo("Jane Lou");
        assertThat(janeStudent.getLastName()).isEqualTo("Smith");
        assertThat(janeStudent.getEmail()).isEqualTo("jane@smith.com");

        assertThat(result.getFirstName()).isEqualTo("Jane Lou");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane@smith.com");
    }

    @Test
    void updateStudent_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateStudent(1L, 
            StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"
        )))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found");

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }   
    
    // =====================================================
    // DELETE
    // =====================================================

    @Test
    void deleteStudent_shouldDelete_whenExists() {
        Student janeStudent = new Student("Jane", "Smith", "jane.smith@gmail.com", "0698765432", 2L);
        when(repository.findById(janeStudent.getId())).thenReturn(Optional.of(janeStudent));

        studentService.deleteStudent(janeStudent.getId());

        verify(repository).delete(janeStudent);
    }

    @Test
    void deleteStudent_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found");

        verify(repository, never()).delete(any());
    }    
    
}
