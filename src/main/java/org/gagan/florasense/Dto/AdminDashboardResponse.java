package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    //Users
    private long totalUsers;
    private long totalActiveUsers;
    private long totalInActiveUsers;
    private long newUsersLast7Days;
    private long newUsersPrevious7Days;
    private Double growthPercentage;

    //Plants
    private long totalActivePlants;

    //Uploads
    private long totalUploads;

    //Favorites
    private long totalFavoritePlants;
}
