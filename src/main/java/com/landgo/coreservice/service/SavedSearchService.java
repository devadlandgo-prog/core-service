package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.entity.SavedSearch;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.SavedSearchMapper;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.repository.LandSpecification;
import com.landgo.coreservice.repository.SavedSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final LandRepository landRepository;
    private final SavedSearchMapper savedSearchMapper;

    private static final int MAX_SAVED_SEARCHES = 25;

    @Transactional
    public SavedSearchResponse createSavedSearch(UUID userId, SavedSearchRequest request) {
        long count = savedSearchRepository.countByUserIdAndDeletedFalse(userId);
        if (count >= MAX_SAVED_SEARCHES) {
            throw new BadRequestException("Maximum saved searches limit (" + MAX_SAVED_SEARCHES + ") reached");
        }

        if (savedSearchRepository.existsByUserIdAndNameAndDeletedFalse(userId, request.getName())) {
            throw new BadRequestException("A saved search with this name already exists");
        }

        SavedSearch savedSearch = savedSearchMapper.toEntity(request);
        savedSearch.setUserId(userId);

        // Calculate initial match count using Specification
        Specification<Land> spec = LandSpecification.forSavedSearchCriteria(
                request.getKeyword(), request.getCity(), request.getProjectStage(),
                request.getMinPrice(), request.getMaxPrice(),
                request.getMinLotSize(), request.getMaxLotSize());
        long matchCount = landRepository.count(spec);
        savedSearch.setMatchCount((int) matchCount);

        SavedSearch saved = savedSearchRepository.save(savedSearch);
        log.info("Saved search created: {} by user: {}", saved.getId(), userId);
        return savedSearchMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<SavedSearchResponse> getMySavedSearches(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SavedSearch> searches = savedSearchRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        List<SavedSearchResponse> content = searches.getContent().stream()
                .map(savedSearchMapper::toResponse).collect(Collectors.toList());

        return PageResponse.<SavedSearchResponse>builder()
                .content(content).number(searches.getNumber()).size(searches.getSize())
                .totalElements(searches.getTotalElements()).totalPages(searches.getTotalPages())
                .first(searches.isFirst()).last(searches.isLast()).build();
    }

    @Transactional(readOnly = true)
    public SavedSearchResponse getSavedSearch(UUID userId, UUID searchId) {
        SavedSearch search = savedSearchRepository.findByIdAndUserIdAndDeletedFalse(searchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        return savedSearchMapper.toResponse(search);
    }

    @Transactional
    public SavedSearchResponse updateSavedSearch(UUID userId, UUID searchId, SavedSearchRequest request) {
        SavedSearch search = savedSearchRepository.findByIdAndUserIdAndDeletedFalse(searchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));

        search.setName(request.getName());
        search.setKeyword(request.getKeyword());
        search.setCity(request.getCity());
        search.setProjectStage(request.getProjectStage());
        search.setMinPrice(request.getMinPrice());
        search.setMaxPrice(request.getMaxPrice());
        search.setMinLotSize(request.getMinLotSize());
        search.setMaxLotSize(request.getMaxLotSize());
        search.setNotificationsEnabled(request.isNotificationsEnabled());

        // Calculate match count using Specification
        Specification<Land> spec = LandSpecification.forSavedSearchCriteria(
                request.getKeyword(), request.getCity(), request.getProjectStage(),
                request.getMinPrice(), request.getMaxPrice(),
                request.getMinLotSize(), request.getMaxLotSize());
        long matchCount = landRepository.count(spec);
        search.setMatchCount((int) matchCount);

        SavedSearch saved = savedSearchRepository.save(search);
        return savedSearchMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSavedSearch(UUID userId, UUID searchId) {
        SavedSearch search = savedSearchRepository.findByIdAndUserIdAndDeletedFalse(searchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        search.setDeleted(true);
        savedSearchRepository.save(search);
        log.info("Saved search deleted: {} by user: {}", searchId, userId);
    }

    @Transactional
    public SavedSearchResponse toggleNotifications(UUID userId, UUID searchId) {
        SavedSearch search = savedSearchRepository.findByIdAndUserIdAndDeletedFalse(searchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        search.setNotificationsEnabled(!search.isNotificationsEnabled());
        SavedSearch saved = savedSearchRepository.save(search);
        return savedSearchMapper.toResponse(saved);
    }
}
