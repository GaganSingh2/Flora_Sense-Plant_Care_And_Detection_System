package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.TrendingPlantsDashboardResponse;
import org.gagan.florasense.Service.TrendingPlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plants/trending")
public class TrendingPlantController {

    @Autowired
    private TrendingPlantService trendingPlantService;

    @GetMapping("/features")
    public ResponseEntity<ResponseStructure<TrendingPlantsDashboardResponse>> getTrendingPlants(
            @RequestParam(defaultValue = "3") int limit
    )
    {
        TrendingPlantsDashboardResponse trendingPlantsDashboardResponse =
                trendingPlantService.getTrendingPlants(limit);

        ResponseStructure<TrendingPlantsDashboardResponse> response = new ResponseStructure<>();
        response.setMessage(trendingPlantsDashboardResponse.message());
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(trendingPlantsDashboardResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
