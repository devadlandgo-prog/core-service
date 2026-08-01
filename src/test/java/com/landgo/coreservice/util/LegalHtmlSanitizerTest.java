package com.landgo.coreservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalHtmlSanitizerTest {

    @Test
    @DisplayName("keeps the structure and utility classes the web app renders")
    void preservesDocumentStructure() {
        String html = "<div class=\"space-y-5\"><section><h2>1. Information We Collect</h2>"
                + "<p>We collect <strong>account</strong> details.</p></section></div>";

        String cleaned = LegalHtmlSanitizer.sanitize(html);

        assertTrue(cleaned.contains("class=\"space-y-5\""));
        assertTrue(cleaned.contains("<section>"));
        assertTrue(cleaned.contains("<h2>"));
        assertTrue(cleaned.contains("<strong>account</strong>"));
    }

    @Test
    @DisplayName("strips script tags")
    void stripsScripts() {
        String cleaned = LegalHtmlSanitizer.sanitize("<p>Policy</p><script>alert('xss')</script>");

        assertTrue(cleaned.contains("<p>Policy</p>"));
        assertFalse(cleaned.contains("<script"));
        assertFalse(cleaned.contains("alert"));
    }

    @Test
    @DisplayName("strips inline event handlers")
    void stripsEventHandlers() {
        String cleaned = LegalHtmlSanitizer.sanitize("<p onclick=\"steal()\">Terms</p>");

        assertTrue(cleaned.contains("Terms"));
        assertFalse(cleaned.contains("onclick"));
    }

    @Test
    @DisplayName("strips javascript: URLs while keeping ordinary links")
    void stripsJavascriptUrls() {
        String cleaned = LegalHtmlSanitizer.sanitize(
                "<a href=\"javascript:alert(1)\">bad</a><a href=\"https://landgo.ca\">good</a>");

        assertFalse(cleaned.contains("javascript:"));
        assertTrue(cleaned.contains("https://landgo.ca"));
    }

    @Test
    @DisplayName("handles a null document body")
    void handlesNull() {
        assertNull(LegalHtmlSanitizer.sanitize(null));
    }
}
