package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.AdminUserResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.UpdateStatusRequest;
import org.gagan.florasense.Dto.UserStatsResponse;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.Role;
import org.gagan.florasense.Exception.IllegalArgumentException;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class AdminUserService {

    @Autowired
    private UserRepositary userRepositary;

    //1)Get all users
    public ResponseEntity<ResponseStructure<?>> getAllUsers(
            String search, Role role, Boolean active, int page, int size)
    {
        if(page < 0 || size < 1 || size > 100){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }

        //Always exclude ADMIN accounts
        if(role != null && role != Role.USER){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Only USER accounts can be listed");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String normalizedSearch = search == null || search.isBlank()
                ? ""
                : search.trim();


        Page<AdminUserResponse> users = userRepositary
                .searchUsers(Role.USER, normalizedSearch, active, pageable)
                .map(user -> new AdminUserResponse(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfileImageUrl(),
                        user.getRole(),
                        user.getIsActive(),
                        user.getCreatedAt()
                ));


        ResponseStructure<Page<AdminUserResponse>> response = new  ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Users fetched successfully");
        response.setData(users);

        return ResponseEntity.ok(response);
    }

    //Get user by using UserId
    public ResponseEntity<ResponseStructure<?>> getUserByUserId(Integer userId) {
        User user = userRepositary.findByUserIdAndRole(userId, Role.USER)
                .orElseThrow(()->
                    new ResourceNotFoundException("User not found with UserId: " + userId)
                );

        AdminUserResponse userResponse = new AdminUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt()
        );

        ResponseStructure<AdminUserResponse> response = new  ResponseStructure<>();
        response.setStatusCode(HttpStatus.FOUND.value());
        response.setMessage("User fetched successfully");
        response.setData(userResponse);

        return ResponseEntity.ok(response);
    }

    //3)Update user status
    public ResponseEntity<ResponseStructure<?>> updateUserStatusByUserId(Integer userId, UpdateStatusRequest request) {
        User user = userRepositary.findByUserIdAndRole(userId, Role.USER)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with UserId: " + userId)
                );

        user.setIsActive(request.getIsActive());
        User updatedUser = userRepositary.save(user);

        AdminUserResponse userResponse = new AdminUserResponse(
                updatedUser.getUserId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getProfileImageUrl(),
                updatedUser.getRole(),
                updatedUser.getIsActive(),
                updatedUser.getCreatedAt()
        );
        ResponseStructure<AdminUserResponse> response = new  ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(updatedUser.getIsActive()
                    ? "User activated successfully"
                    : "User has been deactivated successfully");
        response.setData(userResponse);
        return ResponseEntity.ok(response);
    }

    //4)Export all Users in the form of CSV file i.e Excel or Google Sheet
    public byte[] exportUsers() {
        List<User> users = userRepositary.findByRole(Role.USER);
        if(users.isEmpty()){
            throw new ResourceNotFoundException("No users found");
        }

        StringBuilder csv = new StringBuilder();
        //CSV header
        csv.append("UserId, Username, Email, Role, Active\n");
        //add each user in file
        for(User user: users){
            csv.append(user.getUserId()).append(", ")
                    .append(user.getUsername()).append(", ")
                    .append(user.getEmail()).append(", ")
                    .append(user.getRole()).append(", ")
                    .append(user.getIsActive()).append("\n");
        }

        return csv.toString().getBytes();
    }
    //Helper method for Export users
    private String csvField(String value){
        if (value == null){
            return "";
        }
        return "\"" + value.replace("\"", "\"\"")+"\"";
    }

    //5)Get all User Stats
    public ResponseEntity<ResponseStructure<?>> getUserStats() {
        long totalUsers = userRepositary.countByRole(Role.USER);

        long totalActiveUsers = userRepositary.countByRoleAndIsActive(Role.USER, true);

        long totalInActiveUsers = userRepositary.countByRoleAndIsActive(Role.USER, false);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);

        //Registration in last 7 days
        long newUsersLast7Days = userRepositary.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                Role.USER, sevenDaysAgo, now
        );

        //Registration in the previous 7 days
        long newUsersPrevious7Days = userRepositary.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                Role.USER,
                fourteenDaysAgo,
                sevenDaysAgo
        );

        //Calculate Percentage growth
        double growthPercentage = 0.0;

        if(newUsersPrevious7Days > 0){
            growthPercentage = ((double) (newUsersLast7Days - newUsersPrevious7Days) / newUsersPrevious7Days) * 100;
        }

        UserStatsResponse stats = new UserStatsResponse(
                totalUsers,
                totalActiveUsers,
                totalInActiveUsers,
                newUsersLast7Days,
                newUsersPrevious7Days,
                growthPercentage
        );

        ResponseStructure<UserStatsResponse> response = new  ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Users statistics fetched successfully");
        response.setData(stats);
        return ResponseEntity.ok(response);
    }
}
