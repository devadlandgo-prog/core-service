package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.DraftStepRequest;
import com.landgo.coreservice.dto.response.DraftResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.entity.ListingDraft;
import com.landgo.coreservice.entity.User;
import com.landgo.coreservice.enums.DraftStatus;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.repository.ListingDraftRepository;
import com.landgo.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingDraftService {

    private final ListingDraftRepository draftRepository;
    private final UserRepository userRepository;

    @Transactional
    public DraftResponse createDraft(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ListingDraft draft = ListingDraft.builder()
                .owner(user)
                .status(DraftStatus.IN_PROGRESS)
                .currentStep(0)
                .build();

        ListingDraft saved = draftRepository.save(draft);
        log.info("Draft created: {} by user: {}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    public DraftResponse updateDraftStep(UUID userId, UUID draftId, DraftStepRequest request) {
        ListingDraft draft = draftRepository.findByIdAndOwnerId(draftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft", "id", draftId));

        if (draft.getStatus() == DraftStatus.PUBLISHED) {
            throw new BadRequestException("Cannot update a published draft");
        }

        int stepNum = parseStepNumber(request.getStep());
        Map<String, Object> data = request.getData();

        switch (stepNum) {
            case 1 -> draft.setStep1Data(data);
            case 2 -> draft.setStep2Data(data);
            case 3 -> draft.setStep3Data(data);
            case 4 -> draft.setStep4Data(data);
            case 5 -> draft.setStep5Data(data);
            default -> throw new BadRequestException("Invalid step: " + request.getStep());
        }

        if (stepNum > draft.getCurrentStep()) {
            draft.setCurrentStep(stepNum);
        }

        ListingDraft saved = draftRepository.save(draft);
        log.info("Draft step {} updated for draft: {}", stepNum, draftId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DraftResponse getDraft(UUID userId, UUID draftId) {
        ListingDraft draft = draftRepository.findByIdAndOwnerId(draftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft", "id", draftId));
        return toResponse(draft);
    }

    @Transactional(readOnly = true)
    public PageResponse<DraftResponse> getMyDrafts(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ListingDraft> drafts = draftRepository.findActiveByOwnerId(userId, pageable);
        List<DraftResponse> content = drafts.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return PageResponse.<DraftResponse>builder()
                .data(content).page(drafts.getNumber()).pageSize(drafts.getSize())
                .total(drafts.getTotalElements()).totalPages(drafts.getTotalPages())
                .first(drafts.isFirst()).last(drafts.isLast()).build();
    }

    @Transactional
    public void deleteDraft(UUID userId, UUID draftId) {
        ListingDraft draft = draftRepository.findByIdAndOwnerId(draftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft", "id", draftId));
        draft.setDeleted(true);
        draftRepository.save(draft);
        log.info("Draft deleted: {} by user: {}", draftId, userId);
    }

    @Transactional
    public DraftResponse markAsPublished(UUID userId, UUID draftId) {
        ListingDraft draft = draftRepository.findByIdAndOwnerId(draftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft", "id", draftId));
        draft.setStatus(DraftStatus.PUBLISHED);
        ListingDraft saved = draftRepository.save(draft);
        return toResponse(saved);
    }

    private int parseStepNumber(String step) {
        try {
            String num = step.replaceAll("[^0-9]", "");
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid step format: " + step);
        }
    }

    private DraftResponse toResponse(ListingDraft draft) {
        return DraftResponse.builder()
                .draftId(draft.getId())
                .status(draft.getStatus())
                .currentStep(draft.getCurrentStep())
                .step1(draft.getStep1Data())
                .step2(draft.getStep2Data())
                .step3(draft.getStep3Data())
                .step4(draft.getStep4Data())
                .step5(draft.getStep5Data())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}
