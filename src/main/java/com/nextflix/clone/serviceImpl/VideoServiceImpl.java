package com.nextflix.clone.serviceImpl;

import com.nextflix.clone.dao.UserRepository;
import com.nextflix.clone.dao.VideoRepository;
import com.nextflix.clone.dto.request.VideoRequest;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.dto.response.PageResponse;
import com.nextflix.clone.dto.response.VideoResponse;
import com.nextflix.clone.dto.response.VideoStatsResponse;
import com.nextflix.clone.entity.Video;
import com.nextflix.clone.service.VideoService;
import com.nextflix.clone.util.PaginationUtils;
import com.nextflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse createVideoByAdmin(VideoRequest videoRequest) {
        Video video = new Video();
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setRating(videoRequest.getRating());
        video.setDuration(videoRequest.getDuration());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.isPublished());
        video.setCategories(videoRequest.getCategories()!=null ? videoRequest.getCategories() : List.of());
        videoRepository.save(video);
        return new MessageResponse("Video created successfully");
    }

    @Override
    public PageResponse<VideoResponse> getAllAdminVideo(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Video> videoPage;
        if(search!=null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchVideos(search, pageable);
        } else  {
            videoPage = videoRepository.findAll(pageable);
        }
        return PaginationUtils.toPageResponse(videoPage, VideoResponse::fromEntity);
    }

    @Override
    public MessageResponse updateVideoByAdmin(Long id, VideoRequest videoRequest) {
        Video video = serviceUtils.getVideoByIdOrThrow(id);
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setRating(videoRequest.getRating());
        video.setDuration(videoRequest.getDuration());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.isPublished());
        video.setCategories(videoRequest.getCategories()!=null ? videoRequest.getCategories() : List.of());
        videoRepository.save(video);
        return new MessageResponse("Video updated successfully");
    }

    @Override
    public MessageResponse deleteVideoByAdmin(Long id) {
        if(!videoRepository.existsById(id)) {
            throw new IllegalArgumentException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
        return new MessageResponse("Video deleted successfully");
    }

    @Override
    public MessageResponse toggleVideoPublishedStatusByAdmin(Long id, boolean status) {
        Video video = serviceUtils.getVideoByIdOrThrow(id);
        video.setPublished(status);
        videoRepository.save(video);
        return new MessageResponse("Video published status updated successfully");
    }

    @Override
    public VideoStatsResponse getAdminStats() {
        long totalVideos = videoRepository.count();
        long publishedVideos = videoRepository.countPublishedVideo();
        long totalDuration = videoRepository.getTotalDuration();

       return new VideoStatsResponse(
                totalVideos,
                publishedVideos,
                totalDuration
       );
    }

    @Override
    public PageResponse<VideoResponse> getPublishedVideos(int page, int size, String search, String email) {

        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Video> videoPage;

        if(!search.trim().isEmpty()) {
            videoPage = videoRepository.searchPublishedVideos(search, pageable);
        } else  {
            videoPage = videoRepository.findPublishedVideos(pageable);
        }

        // Fetch the videos for the current page
        List<Video> videos = videoPage.getContent();
        // Tạo Watchlist ID set để kiểm tra xem video nào có trong watchlist của người dùng
        Set<Long> watchlistIds = Set.of();
        if(!videos.isEmpty()){
            try {
                // Lấy danh sách video IDs từ các video đã lấy được trong trang hiện tại
                List<Long> videoIds = videos.stream().map(Video::getId).toList();
                // Truy vấn cơ sở dữ liệu để lấy danh sách video IDs có trong watchlist của người dùng
                watchlistIds = userRepository.findWatchlistVideo(email, videoIds);
            } catch (Exception e) {
                watchlistIds = Set.of(); // In case of any error, treat as if no videos are in the watchlist
            }
        }

        // Chuyển đổi danh sách video thành PageResponse<VideoResponse> và đánh dấu video nào có trong watchlist
        Set<Long> finalWatchlistIds = watchlistIds;

        // Đánh dấu video nào có trong watchlist của người dùng
        videos.forEach(video -> video.setIsInWatchlist(finalWatchlistIds.contains(video.getId())));

        // Chuyển đổi danh sách video thành danh sách VideoResponse
        List<VideoResponse> videoResponses = videos.stream()
                .map(VideoResponse::fromEntity)
                .toList();

        return PaginationUtils.toPageResponse(videoPage, videoResponses);


    }

    @Override
    public List<VideoResponse> getFeaturedVideos() {
        Pageable pageable = PageRequest.of(0,5);
        List<Video> videos = videoRepository.findRandomFeaturedVideos(pageable);
        return videos.stream().map(VideoResponse::fromEntity).toList();
    }
}
