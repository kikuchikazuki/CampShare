package com.example.campshare.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private SignupService signupService;

    @Test
    void registerSavesRoleUserWithEncodedPassword() {
        SignupForm form = new SignupForm("Camper Taro", "taro@example.com", "password123");
        Role roleUser = new Role(1L, "ROLE_USER");
        when(userRepository.existsByEmail("taro@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        signupService.register(form);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getValue().getRole()).isSameAs(roleUser);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        SignupForm form = new SignupForm("Camper Taro", "taro@example.com", "password123");
        when(userRepository.existsByEmail("taro@example.com")).thenReturn(true);

        assertThatThrownBy(() -> signupService.register(form))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
