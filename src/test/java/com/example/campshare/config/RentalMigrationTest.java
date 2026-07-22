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
class RentalMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rentalsTableHasRequiredColumns() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rentals", Integer.class);

        assertThat(count).isNotNull();
        assertThat(columnExists("user_id")).isTrue();
        assertThat(columnExists("gear_id")).isTrue();
        assertThat(columnExists("start_date")).isTrue();
        assertThat(columnExists("end_date")).isTrue();
        assertThat(columnExists("total_price")).isTrue();
        assertThat(columnExists("status")).isTrue();
        assertThat(columnExists("created_at")).isTrue();
    }

    private boolean columnExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_name = 'rentals' AND column_name = ?",
                Integer.class,
                columnName);
        return count != null && count == 1;
    }
}
