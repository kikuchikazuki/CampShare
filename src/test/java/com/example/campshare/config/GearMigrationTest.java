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
class GearMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void gearsTableContainsSixDemoItems() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM gears", Integer.class);

        assertThat(count).isEqualTo(6);
    }
}
