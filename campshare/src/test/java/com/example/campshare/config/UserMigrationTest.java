package com.example.campshare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "file:./application-local.properties")
class UserMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usersTableHasColumnsAndRoleForeignKey() {
        Integer columns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'users'
                  AND column_name IN ('id', 'display_name', 'email', 'password_hash', 'role_id', 'created_at')
                """, Integer.class);
        Integer foreignKeys = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE table_schema = 'public' AND table_name = 'users'
                  AND constraint_type = 'FOREIGN KEY'
                """, Integer.class);

        assertThat(columns).isEqualTo(6);
        assertThat(foreignKeys).isEqualTo(1);
    }
}
