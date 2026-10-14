package com.saicharan.courseregistration.service;

import com.saicharan.courseregistration.domain.Role;
import com.saicharan.courseregistration.domain.User;
import com.saicharan.courseregistration.dto.AuthDtos.AuthResponse;
import com.saicharan.courseregistration.dto.AuthDtos.LoginRequest;
import com.saicharan.courseregistration.dto.AuthDtos.RegisterRequest;
import com.saicharan.courseregistration.exception.ConflictException;
import com.saicharan.courseregistration.exception.NotFoundException;
import com.saicharan.courseregistration.repository.UserRepository;
import com.saicharan.courseregistration.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** New accounts are always students; admins are created by the seed data or by an existing admin. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = users.save(new User(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
