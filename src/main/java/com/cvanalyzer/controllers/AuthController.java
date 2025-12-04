package com.cvanalyzer.controllers;

import com.cvanalyzer.dtos.*;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.security.JwtUtil;
import com.cvanalyzer.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userService.getUserByEmail(email);

        String name = (user.getName() != null) ? user.getName() : "";
        String surname = (user.getSurname() != null) ? user.getSurname() : "";
        String fullName = (name + " " + surname).trim();

        if(fullName.isEmpty()){
            fullName = "Kullanıcı";
        }

        UserResponse userResponse = new UserResponse(fullName, user.getEmail());

        return ResponseEntity.ok(userResponse);
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

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String email = authentication.getName();

        userService.deleteUser(email);

        return  ResponseEntity.ok("Hesabınız ve tüm verileriniz başarıyla silindi.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Authentication authentication) {
        String email = authentication.getName();

        try{
            userService.changePassword(email, changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword());
            return ResponseEntity.ok("Şifreniz değiştirildi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
