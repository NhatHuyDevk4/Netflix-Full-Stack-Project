package com.nextflix.clone.service;

import com.nextflix.clone.dto.request.VideoRequest;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.dto.response.PageResponse;
import com.nextflix.clone.dto.response.VideoResponse;
import com.nextflix.clone.dto.response.VideoStatsResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface VideoService {
    MessageResponse createVideoByAdmin( VideoRequest videoRequest);

    PageResponse<VideoResponse> getAllAdminVideo(int page, int size, String search);

    MessageResponse updateVideoByAdmin(Long id,VideoRequest videoRequest);

    MessageResponse deleteVideoByAdmin(Long id);

    MessageResponse toggleVideoPublishedStatusByAdmin(Long id, boolean status);

    VideoStatsResponse getAdminStats();

    PageResponse<VideoResponse> getPublishedVideos(int page, int size, String search, String email);

    List<VideoResponse> getFeaturedVideos();
}
