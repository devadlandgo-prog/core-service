package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * A published FAQ entry.
 *
 * <p>FAQs have no draft state: a saved row is live. Removal is a soft delete
 * ({@link BaseEntity#isDeleted()}) so the public endpoint can stop serving an entry without
 * losing the answer an admin may want back.
 */
@Entity
@Table(name = "faqs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Faq extends BaseEntity {

    @Column(name = "question", nullable = false, length = 200)
    private String question;

    /** Plain-text answer. Always populated, even when {@link #answerHtml} is also supplied. */
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    /** Optional rich-text answer, sanitized before persisting. */
    @Column(name = "answer_html", columnDefinition = "TEXT")
    private String answerHtml;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
