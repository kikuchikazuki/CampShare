package com.example.campshare.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalConfigurationDocumentationTest {

    @Test
    void exampleContainsSafeDatabaseConfigurationPlaceholders() throws IOException {
        String example = Files.readString(Path.of("application-local.example.properties"));

        assertTrue(example.contains("spring.datasource.url=jdbc:postgresql://localhost:5432/campshare"));
        assertTrue(example.contains("spring.datasource.username=YOUR_USERNAME"));
        assertTrue(example.contains("spring.datasource.password=YOUR_PASSWORD"));
        assertTrue(example.contains("spring.datasource.driver-class-name=org.postgresql.Driver"));
        assertFalse(example.contains("campshare_app"));
        assertFalse(example.contains("Admin123!"));
    }

    @Test
    void readmeDocumentsCopyingTheExampleWithoutEditingIt() throws IOException {
        String readme = Files.readString(Path.of("README.md"));

        assertTrue(readme.contains(
                "Copy-Item application-local.example.properties application-local.properties"));
        assertTrue(readme.contains("application-local.example.properties"));
        assertTrue(readme.contains("application-local.properties"));
    }

    @Test
    void realLocalPropertiesRemainIgnored() throws IOException {
        String gitignore = Files.readString(Path.of(".gitignore"));

        assertTrue(gitignore.lines()
                .map(String::trim)
                .anyMatch(line -> line.equals("application-local.properties")));
    }
}
