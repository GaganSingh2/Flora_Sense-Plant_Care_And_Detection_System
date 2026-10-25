package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.TrendingPlantResponse;
import org.gagan.florasense.Dto.TrendingPlantsDashboardResponse;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Enum.UploadStatus;
import org.gagan.florasense.Exception.IllegalArgumentException;
import org.gagan.florasense.Repositary.PlantUploadRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrendingPlantService {

    @Autowired
    private PlantUploadRepositary plantUploadRepositary;

    @Autowired
    private UserRepositary userRepositary;

    //Indian Standard Time Zone
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    @Transactional(readOnly = true)
    public TrendingPlantsDashboardResponse getTrendingPlants(int limit) {

        //Validate limit
        if (limit < 1 || limit > 20){
            throw new IllegalArgumentException("Limit must be between 1 and 20");
        }

        //Monday = index 0, Tuesday = index 1, ......., Sunday = index 6
        DayOfWeek today =
                ZonedDateTime.now(APP_ZONE).getDayOfWeek();

        //Fetch enough plants to rotate the featured plant acorss the week
        int fetchSize = Math.max(7, limit);

        Pageable pageable = PageRequest.of(0, fetchSize);
        List<Object[]> rows =
                plantUploadRepositary.findTrendingPlants(
                        UploadStatus.SUCCESS,
                        pageable
                );

        //Handle empty table or no eligible successful uploads
        if (rows == null || rows.isEmpty()){
            return new TrendingPlantsDashboardResponse(
                    today,
                    null,
                    List.of(),
                    "No trending plants are available yet. "
                    + "Identify some plants to discover what's popular!"
            );
        }

        List<TrendingPlantResponse> rankedPlants = new ArrayList<>();
        int rank = 1;

        for(Object[] row : rows){
            PlantInfo plant = (PlantInfo)row[0];

            Long identificationCount =
                    ((Number)row[1]).longValue();

            TrendingPlantResponse response =
                    new TrendingPlantResponse(
                            plant.getPlantId(),
                            plant.getPlantName(),
                            plant.getScientificName(),
                            plant.getImageUrl(),
                            plant.getDescription(),
                            plant.getPlantCategory() == null
                                    ? null : plant.getPlantCategory(),
                            identificationCount,
                            rank
                    );

            rankedPlants.add(response);
            rank++;
        }

        int featuredIndex =
                (today.getValue() - 1)
                    % Math.min(7, rankedPlants.size());

        TrendingPlantResponse featuredPlantResponse =
                rankedPlants.get(featuredIndex);

        List<TrendingPlantResponse> trendingPlantResponses =
                rankedPlants
                        .stream()
                        .limit(limit)
                        .toList();

        return new TrendingPlantsDashboardResponse(
                today,
                featuredPlantResponse,
                trendingPlantResponses,
                "Trending plants retrived successfully!"
        );
    }
}
















