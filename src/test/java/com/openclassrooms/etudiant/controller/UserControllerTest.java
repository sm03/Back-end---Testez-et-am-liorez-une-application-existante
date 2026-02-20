package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.sql.Connection;
import java.sql.ResultSet;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerTest {

    private static final String REGISTER_URL = "/api/register";
    private static final String LOGIN_URL = "/api/login";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
    private static final String JWT_TOKEN = "JWT_TOKEN";

    @Container // mysql:latest (9.0) non compatible avec testcontainers 2.0.3 (erreur innodb_log_file_size=5M)
    @ServiceConnection // à la place de @DynamicPropertySource pour injecter les propriétés de connexion à la base de données
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

/*  Non nécessaire avec @ServiceConnection qui injecte automatiquement les propriétés de connexion à la base de données
    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

    }
*/

    @BeforeAll
    public static void beforeAll() throws Exception {
        log.info("MySQLContainer started ({}) at URL: {}",
            mySQLContainer.isRunning(),
            mySQLContainer.getJdbcUrl()); 

        String userName = mySQLContainer.getUsername();
        String password = mySQLContainer.getPassword();
        log.info("MySQLContainer credentials - username: {}, password: {}", userName, password); 
        
        Connection connection = mySQLContainer.createConnection("");
        log.info("Successfully connected to MySQLContainer with connection: {}", connection);
        ResultSet resultSet = connection.createStatement().executeQuery("SHOW DATABASES;");
        while (resultSet.next()) {
            log.info("- {}", resultSet.getString(1));
        } // information_schema, performance_schema, test

        // Il n'existe aucune table dans la base de données à ce stade
        // et curieusement cette requête déclenche une exception (la base de données test n'existe pas)
//        resultSet = connection.createStatement().executeQuery("SHOW TABLES IN TEST;");
//        while (resultSet.next()) {
//            log.info("- {}", resultSet.getString(1));
//        }
        
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    public void registerUserWithoutRequiredData() throws Exception {
        // GIVEN
        RegisterDTO registerDTO = new RegisterDTO();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(registerDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void registerAlreadyExistUser() throws Exception {
        // GIVEN
      User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        // Enregistrer l'utilisateur pour qu'il existe déjà dans la base de données
        userService.register(user); 

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName(FIRST_NAME);
        registerDTO.setLastName(LAST_NAME);
        registerDTO.setLogin(LOGIN);
        registerDTO.setPassword(PASSWORD);

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(registerDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
    }

    @Test
    public void registerUserSuccessful() throws Exception {
        // GIVEN
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName(FIRST_NAME);
        registerDTO.setLastName(LAST_NAME);
        registerDTO.setLogin(LOGIN);
        registerDTO.setPassword(PASSWORD);

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(registerDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void login_shouldReturnJwtToken_whenCredentialsValid() throws Exception {
        // GIVEN
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin(LOGIN);
        loginRequestDTO.setPassword(PASSWORD);
  
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        // Nécessaire pour enregistrer l'utilisateur dans la base de données et permettre le login
        userService.register(user);

        // THEN
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
            .param("login", loginRequestDTO.getLogin())
            .param("password", loginRequestDTO.getPassword())
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.PRAGMA, "no-cache"))
            .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.token_type").value("Bearer"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.expires_in").exists());         
    }

    @Test
    public void login_shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        // GIVEN
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin(LOGIN);
        loginRequestDTO.setPassword(WRONG_PASSWORD);
  
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        // Nécessaire pour enregistrer l'utilisateur dans la base de données et permettre le login
        userService.register(user);

        // THEN
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
            .param("login", loginRequestDTO.getLogin())
            .param("password", loginRequestDTO.getPassword())
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials"));
    }

}
