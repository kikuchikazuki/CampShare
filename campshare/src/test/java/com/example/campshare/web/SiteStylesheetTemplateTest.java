package com.example.campshare.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteStylesheetTemplateTest {

    @Test
    void allNonAuthTemplatesLoadTheSharedSiteStylesheet() throws IOException {
        List<String> templates = List.of(
                "gears.html", "gear-detail.html", "rental-form.html", "rentals.html",
                "admin/dashboard.html", "admin/gears.html", "admin/gear-form.html");

        for (String template : templates) {
            String html = Files.readString(Path.of("src/main/resources/templates", template));
            assertTrue(html.contains("@{/css/site.css}"), template + " must load site.css");
        }
    }

    @Test
    void gearListHasPaginationAtTopAndBottomWithBoundedPages() throws IOException {
        String html = Files.readString(Path.of("src/main/resources/templates/gears.html"));
        assertTrue(html.contains("gear-pagination"));
        assertTrue(html.contains("th:if=\"${totalPages > 1}\""));
        assertTrue(html.contains("#numbers.sequence(0, totalPages - 1)"));
        assertTrue(html.contains("gear-pagination-bottom"));
    }
}
