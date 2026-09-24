package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.FaqResponse;
import com.landgo.coreservice.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public FAQ read API backing the unauthenticated {@code /faq} web route.
 *
 * <p>Responses are marked {@code no-store}: an admin edit is expected to show on the next refresh,
 * and a cached page that still shows a deleted answer is the failure this avoids.
 */
@RestController
@RequestMapping("/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQ", description = "Public, unauthenticated FAQ content")
public class FaqController {

    private final FaqService faqService;

    @GetMapping({"", "/"})
    @Operation(summary = "List published FAQs",
            description = "Returns every published, non-deleted FAQ ordered by sortOrder then question. "
                    + "Public endpoint — never returns deleted rows or admin-only fields.")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> listFaqs(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(faqService.listPublicFaqs(category)));
    }

    @GetMapping("/categories")
    @Operation(summary = "List FAQ categories",
            description = "Distinct categories across published FAQs, for the category filter on /faq.")
    public ResponseEntity<ApiResponse<List<String>>> listCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(faqService.listCategories()));
    }
}
