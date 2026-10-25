package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.AdminDashboardResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Enum.Role;
import org.gagan.florasense.Repositary.FavoritePlantRepositary;
import org.gagan.florasense.Repositary.PlantInfoRepositary;
import org.gagan.florasense.Repositary.PlantUploadRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepositary  userRepositary;

    @Autowired
    private PlantInfoRepositary plantInfoRepositary;

    @Autowired
    private PlantUploadRepositary plantUploadRepositary;

    @Autowired
    private FavoritePlantRepositary favoritePlantRepositary;

    //Get all the stats of User and Plants for Dashboard
    public ResponseEntity<ResponseStructure<?>> getUserAndPlantStats() {
        //User Statistics
        long totalUsers = userRepositary.countByRole(Role.USER);

        long totalActiveUsers = userRepositary.countByRoleAndIsActive(Role.USER, true);

        long totalInActiveUsers = userRepositary.countByRoleAndIsActive(Role.USER, false);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);

        //Registration in last 7 days
        long newUsersLast7Days =
                userRepositary.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        Role.USER,
                        sevenDaysAgo,
                        now
                );

        //Registration in the previous 7 days i.e before 7 days
        long newUsersPrevious7Days =
                userRepositary.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        Role.USER,
                        fourteenDaysAgo,
                        now
                );

        Double growthPercentage = null;
        if(newUsersPrevious7Days == 0){
            if (newUsersLast7Days == 0){
                growthPercentage = 0.0; //no registration in either period
            }
            else{
                growthPercentage = null;
            }
        }
        else{
            growthPercentage = Math.round(
                    (((double) (newUsersLast7Days - newUsersPrevious7Days)
                            / newUsersPrevious7Days) * 100) * 10.0 ) / 10.0;
        }

        //Plant Statistics
        long totalActivePlants = plantInfoRepositary.countByIsActiveTrue();

        //Upload Statistics
        long totalUploads = plantUploadRepositary.count();

        //Favorite Statistics
        long totalFavorites = favoritePlantRepositary.count();

        //Response
        AdminDashboardResponse dashboardResponse = new AdminDashboardResponse(
                totalUsers,
                totalActiveUsers,
                totalInActiveUsers,
                newUsersLast7Days,
                newUsersPrevious7Days,
                growthPercentage,
                totalActivePlants,
                totalUploads,
                totalFavorites
        );

        ResponseStructure<AdminDashboardResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Dashboard statics fetched successfully");
        response.setData(dashboardResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}










