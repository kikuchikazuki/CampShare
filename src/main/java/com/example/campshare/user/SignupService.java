package com.example.campshare.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(SignupForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateEmailException();
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER is not configured"));
        String passwordHash = passwordEncoder.encode(form.getPassword());
        userRepository.save(new User(form.getDisplayName(), form.getEmail(), passwordHash, roleUser));
    }
}
