package com.example.campshare.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DatabaseUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsEmailHashAndRoleAuthority() {
        Role role = new Role(1L, "ROLE_USER");
        User user = new User("Camper Taro", "taro@example.com", "stored-hash", role);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("taro@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("taro@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("stored-hash");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsernameRejectsUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
