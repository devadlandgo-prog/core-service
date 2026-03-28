package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.entity.SavedSearch;
import com.landgo.coreservice.entity.User;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.SavedSearchMapper;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.repository.SavedSearchRepository;
import com.landgo.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserRepository userRepository;
    private final LandRepository landRepository;
    private final SavedSearchMapper savedSearchMapper;

    private static final int MAX_SAVED_SEARCHES = 25;

    @Transactional
    public SavedSearchResponse createSavedSearch(UUID userId, SavedSearchRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long count = savedSearchRepository.countByUserAndDeletedFalse(user);
        if (count >= MAX_SAVED_SEARCHES) {
            throw new BadRequestException("Maximum saved searches limit (" + MAX_SAVED_SEARCHES + ") reached");
        }

        if (savedSearchRepository.existsByUserAndNameAndDeletedFalse(user, request.getName())) {
            throw new BadRequestException("A saved search with this name already exists");
        }

        SavedSearch savedSearch = savedSearchMapper.toEntity(request);
        savedSearch.setUser(user);

        // Calculate initial match count
        long matchCount = landRepository.countBySavedSearchCriteria(
                request.getKeyword(), request.getCity(), request.getProjectStage(),
                request.getMinPrice(), request.getMaxPrice(),
                request.getMinLotSize(), request.getMaxLotSize());
        savedSearch.setMatchCount((int) matchCount);

        SavedSearch saved = savedSearchRepository.save(savedSearch);
        log.info("Saved search created: {} by user: {}", saved.getId(), userId);
        return savedSearchMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<SavedSearchResponse> getMySavedSearches(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<SavedSearch> searches = savedSearchRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable);
        List<SavedSearchResponse> content = searches.getContent().stream()
                .map(savedSearchMapper::toResponse).collect(Collectors.toList());

        return PageResponse.<SavedSearchResponse>builder()
                .content(content).pageNumber(searches.getNumber()).pageSize(searches.getSize())
                .totalElements(searches.getTotalElements()).totalPages(searches.getTotalPages())
                .first(searches.isFirst()).last(searches.isLast()).build();
    }

    @Transactional(readOnly = true)
    public SavedSearchResponse getSavedSearch(UUID userId, UUID searchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        SavedSearch search = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        return savedSearchMapper.toResponse(search);
    }

    @Transactional
    public SavedSearchResponse updateSavedSearch(UUID userId, UUID searchId, SavedSearchRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        SavedSearch search = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
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

        long matchCount = landRepository.countBySavedSearchCriteria(
                request.getKeyword(), request.getCity(), request.getProjectStage(),
                request.getMinPrice(), request.getMaxPrice(),
                request.getMinLotSize(), request.getMaxLotSize());
        search.setMatchCount((int) matchCount);

        SavedSearch saved = savedSearchRepository.save(search);
        return savedSearchMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSavedSearch(UUID userId, UUID searchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        SavedSearch search = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        search.setDeleted(true);
        savedSearchRepository.save(search);
        log.info("Saved search deleted: {} by user: {}", searchId, userId);
    }

    @Transactional
    public SavedSearchResponse toggleNotifications(UUID userId, UUID searchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        SavedSearch search = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", searchId));
        search.setNotificationsEnabled(!search.isNotificationsEnabled());
        SavedSearch saved = savedSearchRepository.save(search);
        return savedSearchMapper.toResponse(saved);
    }
}
