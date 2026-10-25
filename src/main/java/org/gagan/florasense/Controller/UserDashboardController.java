package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.RecentPlantDiscoveryResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.UserDashboardResponse;
import org.gagan.florasense.Service.UserDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-dashboard")
public class UserDashboardController {

    @Autowired
    private UserDashboardService userDashboardService;

    //Dashboard Summary details like Username, totalUploads, Favorite Uploads, Accuracy
    @GetMapping("/summary")
    public ResponseEntity<ResponseStructure<UserDashboardResponse>> getUserDashboardSummary(
            Authentication authentication
    )
    {
        return userDashboardService.getUserDashboardSummary(authentication);
    }

    //Top 3-Recent Discoveries
    @GetMapping("/recent-discoveries")
    public ResponseEntity<ResponseStructure<List<RecentPlantDiscoveryResponse>>> getUserDashboardRecentDiscoveries(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int limit
    )
    {
        return userDashboardService.getUserDashboardRecentDiscoveries(
                authentication, limit
        );
    }
}
