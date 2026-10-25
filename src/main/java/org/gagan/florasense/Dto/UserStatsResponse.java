package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {

    private long totalUsers;
    private long totalActiveUsers;
    private long totalInactiveUsers;
    private long newUsersLast7Days;
    private long newUsersPrevious7Days;
    private double weeklyGrowthPercentage;
}
