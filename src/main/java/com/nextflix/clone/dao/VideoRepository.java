package com.nextflix.clone.dao;

import com.nextflix.clone.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {


    @Query(
        """
        SELECT v FROM Video v
        WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%'))
    """
    )
    Page<Video> searchVideos(@Param("search") String search, Pageable pageable);

    @Query(
        """
        SELECT COUNT(v) FROM Video v
        WHERE v.published = true
     """
    )
    long countPublishedVideo();

    @Query(
        """
        SELECT SUM(v.duration) FROM Video v
        WHERE v.published = true
     """
    )
    long getTotalDuration();

    @Query(
        """
        SELECT v FROM Video v
        WHERE v.published = true AND
              (LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """
    )
    Page<Video> searchPublishedVideos(String search, Pageable pageable);

    @Query(
        """
        SELECT v FROM Video v
        WHERE v.published = true
     """
    )
    Page<Video> findPublishedVideos(Pageable pageable);

    @Query(
        """
        SELECT v FROM Video v
        WHERE v.published = true
        ORDER BY function('RAND')
     """
    )
    List<Video> findRandomFeaturedVideos(Pageable pageable);
}
