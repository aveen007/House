package com.medical.controller;

import com.medical.dto.AuthResponse;
import com.medical.dto.LoginRequest;
import com.medical.dto.RegisterRequest;
import com.medical.entity.User;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.PatientRepository;
import com.medical.repository.RoleRepository;
import com.medical.repository.UserRepository;
import com.medical.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        AuthResponse response = new AuthResponse();
        response.setUserId(principal.getUserId());
        response.setUsername(principal.getUsername());
        response.setPatientId(principal.getPatientId());
        response.setRoles(principal.getRoles());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getPatientId() != null && !patientRepository.existsById(request.getPatientId())) {
            throw new ResourceNotFoundException("Patient not found: " + request.getPatientId());
        }

        String roleName = request.getRole() == null || request.getRole().isBlank()
                ? "PATIENT"
                : request.getRole().trim().toUpperCase();

        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setStatus("ACTIVE");
        user.setPatientId(request.getPatientId());
        user.getRoles().add(role);

        User saved = userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setUserId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setPatientId(saved.getPatientId());
        response.setRoles(Set.of(roleName));
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(401).build();
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse response = new AuthResponse();
        response.setUserId(principal.getUserId());
        response.setUsername(principal.getUsername());
        response.setPatientId(principal.getPatientId());
        response.setRoles(Set.copyOf(principal.getRoles()));
        return ResponseEntity.ok(response);
    }
}
