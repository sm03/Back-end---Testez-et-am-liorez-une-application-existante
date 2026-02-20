package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.UserService;
import com.openclassrooms.etudiant.service.impl.StudentServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentControllerTest {

    private static final String STUDENTS_URL = "/api/students";
    private static final String USER_RAW_PASSWORD = "orangutan";
    
    @Container // mysql:latest (9.0) non compatible avec testcontainers 2.0.3 (erreur innodb_log_file_size=5M)
    @ServiceConnection // à la place de @DynamicPropertySource pour injecter les propriétés de connexion à la base de données
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired // Nécessaire pour les appels authentifiés
    private UserService userService;
    @Autowired // Nécessaire pour voir les données dans la base de données
    private StudentRepository studentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private User user;
    private String token;
    

    @BeforeAll
    void setUpAll() {
        user = new User();
        user.setFirstName("Horace");
        user.setLastName("WorbleHat");
        user.setLogin("hworblehat");
        user.setPassword(USER_RAW_PASSWORD);
        userService.register(user);
    }

    @BeforeEach
    void setUp() {
        token = userService.login(user.getLogin(), USER_RAW_PASSWORD);
        //log.info("Token: {}", token);
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
    }

    // ==========================================
    // CREATE
    // ==========================================

    @Test
    void createStudent_shouldReturnCreatedStudent() throws Exception {

        // Given
        StudentRequestDTO requestDTO = StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"
        );

        var john = studentRepository.findByEmail("john.doe@gmail.com");
        assertThat(john.isEmpty()).isTrue();

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@gmail.com"));

        john = studentRepository.findByEmail("john.doe@gmail.com");
        assertThat(john.isEmpty()).isFalse();
        
        Student student = john.get();
        assertThat(student.getFirstName()).isEqualTo("John");
        assertThat(student.getLastName()).isEqualTo("Doe");
        assertThat(student.getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(student.getPhone()).isEqualTo("0612345678");        
    }

    @Test
    void createStudent_withoutToken_shouldBeUnauthorized() throws Exception {

        // Given
        StudentRequestDTO requestDTO = StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
        //                .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        //        .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Unauthorized"));
        assertThat(studentRepository.findAll().isEmpty());
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @Test
    void getAllStudents_shouldReturnList() throws Exception {

        // Given
        Set<StudentRequestDTO> students = new HashSet<>(Arrays.asList(
            StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"),
            StudentRequestDTO.buildRequestDTO(
            "Jane",
            "Smith",
            "jane.smith@gmail.com",
            "0687654321"),
            StudentRequestDTO.buildRequestDTO(
            "Jack",
            "Johnson",
            "jack.johnson@gmail.com",
            "0698765432"))
        );
        students.forEach(student -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(student))
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(students.size()))
                .andExpect(jsonPath("$[0].email").isNotEmpty())
                .andExpect(jsonPath("$[1].email").isNotEmpty())
                .andExpect(jsonPath("$[2].email").isNotEmpty());
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @Test
    void getStudent_shouldReturnStudent() throws Exception {
        // Given
        List<StudentRequestDTO> students = new ArrayList<>(Arrays.asList(
            StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"),
            StudentRequestDTO.buildRequestDTO(
            "Jane",
            "Smith",
            "jane.smith@gmail.com",
            "0687654321"),
            StudentRequestDTO.buildRequestDTO(
            "Jack",
            "Johnson",
            "jack.johnson@gmail.com",
            "0698765432"))
        );
        students.forEach(student -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(student))
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // When
        var jane = studentRepository.findByEmail("jane.smith@gmail.com");
        var janeId = jane.get().getId();
        
        // Then
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL + "/" + janeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(janeId))
                .andExpect(jsonPath("$.email").value("jane.smith@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phone").value("0687654321"));
    }

    @Test
    void getStudent_shouldReturn500_whenNotFound() throws Exception {

        Long fakeId = 999L;

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL + "/" + fakeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Test
    void updateStudent_shouldReturnUpdatedStudent() throws Exception {

        // Given
        StudentRequestDTO requestDTO = StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"
        );

        // When
        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
                
        requestDTO.setPhone("0798765432");
        requestDTO.setEmail("jdoe99@hotmail.fr");
        var john = studentRepository.findByEmail("john.doe@gmail.com");
        var johnId = john.get().getId();        

        // Then
        mockMvc.perform(MockMvcRequestBuilders.put(STUDENTS_URL + "/" + johnId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(johnId))
                .andExpect(jsonPath("$.email").value("jdoe99@hotmail.fr"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("0798765432"));
                        
        // Verify updated student details
        var updatedStudent = studentRepository.findById(johnId);
        assertThat(updatedStudent.get().getEmail()).isEqualTo("jdoe99@hotmail.fr");
        assertThat(updatedStudent.get().getPhone()).isEqualTo("0798765432");
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Test
    void deleteStudent_shouldReturnNoContent() throws Exception {
        // Given
        List<StudentRequestDTO> students = new ArrayList<>(Arrays.asList(
            StudentRequestDTO.buildRequestDTO(
            "John",
            "Doe",
            "john.doe@gmail.com",
            "0612345678"),
            StudentRequestDTO.buildRequestDTO(
            "Jane",
            "Smith",
            "jane.smith@gmail.com",
            "0687654321"),
            StudentRequestDTO.buildRequestDTO(
            "Jack",
            "Johnson",
            "jack.johnson@gmail.com",
            "0698765432"))
        );
        students.forEach(student -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(student))
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // When
        var jane = studentRepository.findByEmail("jane.smith@gmail.com");
        var janeId = jane.get().getId();
        
        // Then
        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENTS_URL + "/" + janeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify student is deleted
        var deletedStudent = studentRepository.findById(janeId);
        assertThat(deletedStudent.isEmpty()).isTrue();

        // Verify other students are not deleted
        var john = studentRepository.findByEmail("john.doe@gmail.com");
        assertThat(john.isPresent()).isTrue();
        var jack = studentRepository.findByEmail("jack.johnson@gmail.com");
        assertThat(jack.isPresent()).isTrue();
    }

    @Test
    void deleteStudent_shouldReturn500_whenNotFound() throws Exception {

        // Given
        var fakeId = 999L;
        
        // When : verify student does not exist
        var noStudent = studentRepository.findById(fakeId);
        assertThat(noStudent.isEmpty()).isTrue();        

        // Then
        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENTS_URL + "/" + fakeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
