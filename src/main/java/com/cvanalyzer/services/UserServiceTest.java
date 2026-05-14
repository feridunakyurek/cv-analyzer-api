package com.cvanalyzer.services;

import com.cvanalyzer.dtos.JwtResponse;
import com.cvanalyzer.dtos.UserRegistrationRequest;
import com.cvanalyzer.entities.Role;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.UserAlreadyExistsException;
import com.cvanalyzer.repos.UserRepository;
import com.cvanalyzer.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    // ✅ Kayıt başarılı
    @Test
    void registerUser_WhenEmailNotExists_ShouldReturnToken() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Feridun");
        request.setSurname("Akyürek");
        request.setEmail("test@test.com");
        request.setPassword("1234");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(any(), any())).thenReturn("mockToken");

        JwtResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ❌ Kayıt — email zaten var
    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@test.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any());
    }

    // ✅ Email ile kullanıcı bulundu
    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    // ❌ Email ile kullanıcı bulunamadı
    @Test
    void getUserByEmail_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findByEmail("yok@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByEmail("yok@test.com"));
    }

    // ✅ loadUserByUsername başarılı
    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getUsername());
    }

    // ✅ Şifre değiştirme başarılı
    @Test
    void changePassword_WhenCurrentPasswordCorrect_ShouldUpdatePassword() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedOldPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");

        userService.changePassword("test@test.com", "oldPassword", "newPassword");

        assertEquals("hashedNewPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    // ❌ Şifre değiştirme — mevcut şifre yanlış
    @Test
    void changePassword_WhenCurrentPasswordWrong_ShouldThrowException() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedOldPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedOldPassword")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> userService.changePassword("test@test.com", "wrongPassword", "newPassword"));

        verify(userRepository, never()).save(any());
    }
}