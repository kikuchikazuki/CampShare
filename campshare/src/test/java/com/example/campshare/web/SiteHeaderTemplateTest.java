package com.example.campshare.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteHeaderTemplateTest {

    private static final Path TEMPLATES = Path.of("src/main/resources/templates");
    private static final List<String> SITE_TEMPLATES = List.of(
            "gears.html", "gear-detail.html", "rental-form.html", "rentals.html",
            "admin/dashboard.html", "admin/gears.html", "admin/gear-form.html");

    @Test
    void sharedHeaderContainsAllConditionalActions() throws IOException {
        String header = Files.readString(TEMPLATES.resolve("fragments/site-header.html"));

        assertTrue(header.contains("th:fragment=\"siteHeader\""));
        assertTrue(header.contains("@{/gears}"));
        assertTrue(header.contains("@{/login}"));
        assertTrue(header.contains("@{/signup}"));
        assertTrue(header.contains("@{/mypage/rentals}"));
        assertTrue(header.contains("@{/admin/gears}"));
        assertTrue(header.contains("@{/logout}"));
        assertTrue(header.contains("method=\"post\""));
        assertTrue(header.contains("th:if=\"${navAuthenticated}\""));
        assertTrue(header.contains("th:if=\"${navAdmin}\""));
    }

    @Test
    void everySiteTemplateIncludesTheSharedHeader() throws IOException {
        for (String template : SITE_TEMPLATES) {
            String html = Files.readString(TEMPLATES.resolve(template));
            assertTrue(
                    html.contains("th:replace=\"~{fragments/site-header :: siteHeader}\""),
                    template + " must include the shared header");
        }
    }

    @Test
    void standaloneHomeTemplateIsRemoved() {
        assertFalse(Files.exists(TEMPLATES.resolve("home.html")));
    }
}
