package com.cvanalyzer.controllers;

import com.cvanalyzer.dtos.AuthRequest;
import com.cvanalyzer.dtos.JwtResponse;
import com.cvanalyzer.dtos.ResetPasswordRequest;
import com.cvanalyzer.dtos.UserRegistrationRequest;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.UserAlreadyExistsException;
import com.cvanalyzer.security.JwtUtil;
import com.cvanalyzer.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody @Valid UserRegistrationRequest request) {

        JwtResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        User user = userService.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String incomingPassword = authRequest.getPassword();
        if (incomingPassword == null || incomingPassword.isEmpty()) {
            throw new RuntimeException("Şifre boş olamaz");
        }

        String storedPassword = user.getPassword();

        boolean isAuthenticated = passwordEncoder.matches(incomingPassword, storedPassword);

        if (!isAuthenticated) {
            throw new RuntimeException("Geçersiz şifre");
        }

        String token = jwtUtil.generateToken(user.getEmail(), String.valueOf(user.getRole()));
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Şifreniz başarıyla Değiştirildi.");
    }
}
