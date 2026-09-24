package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.FaqReorderRequest;
import com.landgo.coreservice.dto.request.FaqRequest;
import com.landgo.coreservice.dto.response.FaqResponse;
import com.landgo.coreservice.entity.Faq;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.repository.FaqRepository;
import com.landgo.coreservice.util.LegalHtmlSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * FAQ management.
 *
 * <p>There is deliberately no draft state: saving a valid FAQ publishes it, and editing one
 * changes the live entry immediately. Deletion is soft so the public endpoint stops serving the
 * row while an administrator can still recover the wording.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private static final int MAX_QUESTION_LENGTH = 200;
    private static final int MAX_ANSWER_LENGTH = 10_000;

    private final FaqRepository faqRepository;

    // ── Public ──────────────────────────────────────────────────────────────

    /**
     * Live FAQs for the public {@code /faq} page, ordered by {@code sortOrder} then question.
     *
     * <p>Not paginated and not filtered by anything but category: the whole set is rendered as one
     * accordion, and the page's search box filters client-side.
     */
    @Transactional(readOnly = true)
    public List<FaqResponse> listPublicFaqs(String category) {
        List<Faq> faqs = (category == null || category.isBlank())
                ? faqRepository.findAllLive()
                : faqRepository.findLiveByCategory(category.trim());
        return faqs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return faqRepository.findLiveCategories();
    }

    // ── Admin ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FaqResponse> listAdminFaqs(String category, String search) {
        List<Faq> faqs = (category == null || category.isBlank())
                ? faqRepository.findAllLive()
                : faqRepository.findLiveByCategory(category.trim());

        if (search == null || search.isBlank()) {
            return faqs.stream().map(this::toResponse).toList();
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return faqs.stream()
                .filter(faq -> faq.getQuestion().toLowerCase(Locale.ROOT).contains(needle)
                        || (faq.getAnswer() != null && faq.getAnswer().toLowerCase(Locale.ROOT).contains(needle)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FaqResponse getFaq(UUID id) {
        return toResponse(requireFaq(id));
    }

    @Transactional
    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = Faq.builder().build();
        apply(request, faq, true);
        Faq saved = faqRepository.save(faq);
        log.info("FAQ created and published: id={} question='{}'", saved.getId(), saved.getQuestion());
        return toResponse(saved);
    }

    @Transactional
    public FaqResponse updateFaq(UUID id, FaqRequest request, boolean partial) {
        Faq faq = requireFaq(id);
        apply(request, faq, !partial);
        Faq saved = faqRepository.save(faq);
        log.info("FAQ updated: id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Persists reordered positions in one transaction so the list never renders half-reordered.
     */
    @Transactional
    public List<FaqResponse> reorder(FaqReorderRequest request) {
        Map<UUID, Integer> positions = new java.util.LinkedHashMap<>();
        for (FaqReorderRequest.Item item : request.getItems()) {
            positions.put(item.getId(), item.getSortOrder());
        }

        List<Faq> faqs = faqRepository.findAllById(positions.keySet()).stream()
                .filter(faq -> !faq.isDeleted())
                .toList();
        if (faqs.size() != positions.size()) {
            throw new BadRequestException("One or more FAQs in the reorder request do not exist", "VALIDATION_ERROR");
        }

        faqs.forEach(faq -> faq.setSortOrder(positions.get(faq.getId())));
        faqRepository.saveAll(faqs);
        log.info("Reordered {} FAQs", faqs.size());
        return faqRepository.findAllLive().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteFaq(UUID id) {
        Faq faq = requireFaq(id);
        faq.setDeleted(true);
        faqRepository.save(faq);
        log.info("FAQ soft-deleted: id={}", id);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Faq requireFaq(UUID id) {
        return faqRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ", "id", id));
    }

    /**
     * Copies a request onto an entity.
     *
     * @param requireAll when true every required field must be present (create and PUT); when
     *                   false absent fields keep their current value (PATCH)
     */
    private void apply(FaqRequest request, Faq faq, boolean requireAll) {
        if (request.getQuestion() != null) {
            String question = request.getQuestion().trim();
            if (question.isEmpty()) {
                throw new BadRequestException("Question is required", "VALIDATION_ERROR");
            }
            if (question.length() > MAX_QUESTION_LENGTH) {
                throw new BadRequestException("Question must be at most " + MAX_QUESTION_LENGTH + " characters",
                        "VALIDATION_ERROR");
            }
            faq.setQuestion(question);
        } else if (requireAll) {
            throw new BadRequestException("Question is required", "VALIDATION_ERROR");
        }

        boolean answerSupplied = request.getAnswer() != null || request.getAnswerHtml() != null;
        if (answerSupplied) {
            String sanitizedHtml = null;
            if (request.getAnswerHtml() != null && !request.getAnswerHtml().isBlank()) {
                sanitizedHtml = LegalHtmlSanitizer.sanitize(request.getAnswerHtml());
            }

            String plain = request.getAnswer() != null && !request.getAnswer().isBlank()
                    ? request.getAnswer().trim()
                    : (sanitizedHtml != null ? Jsoup.parse(sanitizedHtml).text().trim() : "");

            if (plain.isEmpty()) {
                throw new BadRequestException("Answer is required", "VALIDATION_ERROR");
            }
            if (plain.length() > MAX_ANSWER_LENGTH) {
                throw new BadRequestException("Answer must be at most " + MAX_ANSWER_LENGTH + " characters",
                        "VALIDATION_ERROR");
            }
            faq.setAnswer(plain);
            faq.setAnswerHtml(sanitizedHtml);
        } else if (requireAll) {
            throw new BadRequestException("Answer is required", "VALIDATION_ERROR");
        }

        if (request.getCategory() != null) {
            String category = request.getCategory().trim();
            faq.setCategory(category.isEmpty() ? null : category);
        }

        if (request.getSortOrder() != null) {
            if (request.getSortOrder() < 0) {
                throw new BadRequestException("sortOrder must be zero or greater", "VALIDATION_ERROR");
            }
            faq.setSortOrder(request.getSortOrder());
        } else if (faq.getSortOrder() == null) {
            faq.setSortOrder(0);
        }
    }

    private FaqResponse toResponse(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .answerHtml(faq.getAnswerHtml())
                .category(faq.getCategory())
                .sortOrder(faq.getSortOrder())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
