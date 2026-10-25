package org.gagan.florasense.Repositary;

import org.gagan.florasense.Entity.PlantUpload;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.UploadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlantUploadRepositary extends JpaRepository<PlantUpload, Integer> {

    long count();

    List<PlantUpload> findByUserUserIdOrderByUploadedAtDesc(Integer userId);

    Optional<PlantUpload> findByUploadIdAndUserUserId(
            Integer uploadId,
            Integer userId
    );

    long countByUserUserId(Integer userId);

    //Get identification history with search, sorting and pagination
    @Query("""
        SELECT pu
        FROM PlantUpload pu
        LEFT JOIN pu.plantInfo p
        WHERE pu.user = :user
            AND(
                :keyword = ''
                OR LOWER(p.plantName)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.scientificName)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(pu.imageName)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
    """)
    Page<PlantUpload> searchMyAllHistory(
            User user,
            String keyword,
            Pageable pageable);

    //Get total identification count
    Long countByUser(User user);

    //Check whether another upload references the same image
    boolean existsByImageUrlAndUploadIdNot(String imageUrl, Integer uploadId);

    //Count successful Identification or Not-Identified Attempts or Failed Attempts
    long countByUserAndUploadStatus(User user, UploadStatus uploadStatus);

    //Count the total Successful identification of current week
    @Query("""
        SELECT COUNT(pu)
        FROM PlantUpload pu
        WHERE pu.user = :user
            AND pu.uploadedAt >= :startDate
            AND pu.uploadedAt < :endDate
            AND pu.uploadStatus = :status
    """)
    long countDiscoveriesBetweenDates(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            UploadStatus status
    );

    //Fetch Top 3 Discoveries
    Page<PlantUpload> findByUserAndUploadStatusOrderByUploadedAtDesc(
            User user,
            UploadStatus uploadStatus,
            Pageable pageable
    );

    //Fetch enough plants to rotate the featured plant acorss the week
    @Query("""
    SELECT pu.plantInfo, COUNT(pu.uploadId)
    FROM PlantUpload pu
    WHERE pu.uploadStatus = :status
      AND pu.plantInfo IS NOT NULL
    GROUP BY pu.plantInfo
    ORDER BY COUNT(pu.uploadId) DESC
    """)
    List<Object[]> findTrendingPlants(
            @Param("status") UploadStatus status,
            Pageable pageable
    );
}
