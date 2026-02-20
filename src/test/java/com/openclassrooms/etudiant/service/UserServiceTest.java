package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.configuration.security.JwtService;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String ENCODED_PASSWORD = "ENCODED_PASSWORD";
    private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
    private static final String JWT_TOKEN = "JWT_TOKEN";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @Test
    public void test_create_null_user_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(null));
    }

    @Test
    public void test_create_already_exist_user_throws_IllegalArgumentException() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(user));
    }

    @Test
    public void test_create_user() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());

        // WHEN
        userService.register(user);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(user);
    }

    @Test
    void register_shouldEncodePasswordBeforeSaving() {
        // GIVEN
        User user = new User();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

        // WHEN
        userService.register(user);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(userCaptor.getValue().getLogin()).isEqualTo(LOGIN);
    }

    @Test
    void login_shouldReturnJwt_whenCredentialsAreValid() {
        // GIVEN
        User user = new User();
        user.setLogin(LOGIN);
        user.setPassword(ENCODED_PASSWORD);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(JWT_TOKEN);

        // WHEN
        String result = userService.login(LOGIN, PASSWORD);

        // THEN
        assertThat(result).isEqualTo(JWT_TOKEN);

        verify(userRepository).findByLogin(LOGIN);
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        // GIVEN
        User user = new User();
        user.setLogin(LOGIN);
        user.setPassword(ENCODED_PASSWORD);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.login(LOGIN, WRONG_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        // GIVEN
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThatThrownBy(() -> userService.login(LOGIN, PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowException_whenLoginNull() {
        assertThatThrownBy(() -> userService.login(null, PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login must not be null");
    }

    @Test
    void login_shouldThrowException_whenPasswordNull() {
        assertThatThrownBy(() -> userService.login(LOGIN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must not be null");
    }
}
