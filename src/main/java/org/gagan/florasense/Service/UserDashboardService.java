package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.RecentPlantDiscoveryResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.UserDashboardResponse;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Entity.PlantUpload;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.UploadStatus;
import org.gagan.florasense.Exception.IllegalArgumentException;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.FavoritePlantRepositary;
import org.gagan.florasense.Repositary.PlantUploadRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class UserDashboardService {

    @Autowired
    private PlantUploadRepositary plantUploadRepositary;

    @Autowired
    private UserRepositary userRepositary;

    @Autowired
    private FavoritePlantRepositary favoritePlantRepositary;

    //Dashboard Summary details like Username, totalUploads, Favorite Uploads, Accuracy
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseStructure<UserDashboardResponse>> getUserDashboardSummary(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException("User is not authenticated");
        }

        //1 Find the logged-in user
        String username =  authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //2 Count total uploads
        long countTotalUploads = plantUploadRepositary.countByUser(user);

        //3 Count successful identifications
        long successfulIdentifications =
                plantUploadRepositary.countByUserAndUploadStatus(
                        user,
                        UploadStatus.SUCCESS
                );

        //4 Count Un-Identified attempts
        long unIdentifiedAttempts =
                plantUploadRepositary.countByUserAndUploadStatus(
                        user,
                        UploadStatus.NOT_IDENTIFIED
                );

        //5 count failed attempts
        long failedAttempts =
                plantUploadRepositary.countByUserAndUploadStatus(
                        user,
                        UploadStatus.FAILED
                );

        //6 Count total Favorites
        long totalFavorites =
                favoritePlantRepositary.countByUser(user);

        //7 Calculate the current week's successful discoveries
        LocalDate today = LocalDate.now();

        //Get Monday of the current week
        LocalDate startOfWeek = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        //Start of the following Monday
        LocalDate startOfNextWeek = startOfWeek.plusWeeks(1);

        LocalDateTime startDate = startOfWeek.atStartOfDay();
        LocalDateTime endDate = startOfNextWeek.atStartOfDay();

        //Count successful identification this week
        long weeklyDiscoveries =
                plantUploadRepositary.countDiscoveriesBetweenDates(
                        user,
                        startDate,
                        endDate,
                        UploadStatus.SUCCESS
                );

        //8 Calculate average confidence score
        Double successRate = countTotalUploads == 0
                ? 0.0
                : Math.round((successfulIdentifications * 100.0 / countTotalUploads) * 100.0) / 100.0;

        UserDashboardResponse dashboardResponse = new UserDashboardResponse(
                username,
                countTotalUploads,
                successfulIdentifications,
                unIdentifiedAttempts,
                failedAttempts,
                totalFavorites,
                weeklyDiscoveries,
                successRate
        );

        ResponseStructure<UserDashboardResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(
                countTotalUploads == 0
                    ? "Your botanical journey starts here. Identify your first plant to see your discoveries."
                    : "The botanical world is waking up. You've identified "+weeklyDiscoveries+" new species this week--keep exploring the green around you."
        );
        response.setData(dashboardResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Top 3-Recent Discoveries
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseStructure<List<RecentPlantDiscoveryResponse>>> getUserDashboardRecentDiscoveries(
            Authentication authentication, int limit)
    {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException("User is not authenticated");
        }

        //Validate the limit
        if (limit < 1 || limit > 20) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException("limit must be between 1 and 20");
        }

        //Find the logged-in User
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Fetch Top 3 Discoveries
        Pageable pageable = PageRequest.of(0, limit);
        Page<PlantUpload> uploadPage=
            plantUploadRepositary.findByUserAndUploadStatusOrderByUploadedAtDesc(
                    user,
                    UploadStatus.SUCCESS,
                    pageable
            );

        List<RecentPlantDiscoveryResponse> recentDiscoveries =
                uploadPage.getContent()
                        .stream()
                        .map(this::convertToRecentPlantDiscoveryResponse)
                        .toList();

        //Create a Response
        ResponseStructure<List<RecentPlantDiscoveryResponse>> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(
                !recentDiscoveries.isEmpty()
                    ? "Top Recent Discoveries have been found"
                    : "Your botanical journey starts here! You haven't identified any plants yet. Upload a plant photo to discover your first plant"
        );
        response.setData(recentDiscoveries);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    //Helper method to se the response for RecentDiscoveries of Plant
    private RecentPlantDiscoveryResponse convertToRecentPlantDiscoveryResponse(PlantUpload plantUpload) {
        PlantInfo info = plantUpload.getPlantInfo();

        return new RecentPlantDiscoveryResponse(
                plantUpload.getUploadId(),
                plantUpload.getImageUrl(),
                plantUpload.getImageName(),
                info == null ? -1 : info.getPlantId(),
                info == null ? null : info.getPlantName(),
                info == null ? null : info.getScientificName(),
                info != null && info.getPlantCategory() != null
                        ? info.getPlantCategory() : null,
                plantUpload.getConfidenceScore(),
                plantUpload.getUploadedAt()
        );
    }
}













