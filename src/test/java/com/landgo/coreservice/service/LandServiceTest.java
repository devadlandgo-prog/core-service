package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.mapper.LandMapper;
import com.landgo.coreservice.repository.LandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LandServiceTest {

    @Mock
    private LandRepository landRepository;

    @Mock
    private LandMapper landMapper;

    @InjectMocks
    private LandService landService;

    @Test
    public void testGetHotDeveloperDeals() {
        Land land = new Land();
        land.setHotDeal(true);
        Page<Land> page = new PageImpl<>(Collections.singletonList(land));
        
        when(landRepository.findHotDeveloperDeals(any(PageRequest.class))).thenReturn(page);
        when(landMapper.toResponse(any(Land.class))).thenReturn(new LandResponse());

        PageResponse<LandResponse> result = landService.getHotDeveloperDeals(0, 10);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(landRepository).findHotDeveloperDeals(any(PageRequest.class));
    }

    @Test
    public void testGetFeaturedListings() {
        Land land = new Land();
        land.setFeatured(true);
        Page<Land> page = new PageImpl<>(Collections.singletonList(land));
        
        when(landRepository.findFeaturedListings(any(PageRequest.class))).thenReturn(page);
        when(landMapper.toResponse(any(Land.class))).thenReturn(new LandResponse());

        PageResponse<LandResponse> result = landService.getFeaturedListings(0, 10);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(landRepository).findFeaturedListings(any(PageRequest.class));
    }

    @Test
    public void testIncrementViewCount() {
        UUID id = UUID.randomUUID();
        Land land = new Land();
        land.setId(id);
        land.setViewCount(10);
        
        when(landRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(land));
        
        Long result = landService.incrementViewCount(id);
        
        assertEquals(11L, result);
        verify(landRepository).incrementViewCount(id);
    }
}
