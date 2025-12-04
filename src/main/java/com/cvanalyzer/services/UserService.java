package com.cvanalyzer.services;

import com.cvanalyzer.dtos.JwtResponse;
import com.cvanalyzer.dtos.UserRegistrationRequest;
import com.cvanalyzer.entities.Role;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.UserAlreadyExistsException;
import com.cvanalyzer.repos.UserRepository;
import com.cvanalyzer.security.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public JwtResponse registerUser(@Valid UserRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists!");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER);

        userRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole().name());

        return new JwtResponse(token);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassowrd, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(!passwordEncoder.matches(currentPassowrd, user.getPassword())) {
            throw new RuntimeException("Mevcut şifreniz hatalı!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    public User registerAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }
}
