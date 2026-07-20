package com.example.campshare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "file:./application-local.properties")
class RoleMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rolesTableContainsDefaultRoles() {
        List<String> roleNames = jdbcTemplate.queryForList(
                "SELECT name FROM roles ORDER BY name",
                String.class
        );

        assertThat(roleNames).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }
}
