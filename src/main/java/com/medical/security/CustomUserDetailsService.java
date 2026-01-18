package com.medical.security;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        boolean enabled = "ACTIVE".equalsIgnoreCase(user.getStatus());
        var roles = user.getRoles()
                .stream()
                .map(role -> role.getName().toUpperCase())
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getPatientId(),
                user.getUsername(),
                user.getPasswordHash(),
                enabled,
                roles
        );
    }
}
