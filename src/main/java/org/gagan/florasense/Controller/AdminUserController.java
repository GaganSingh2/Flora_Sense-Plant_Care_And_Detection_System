package org.gagan.florasense.Controller;

import jakarta.validation.Valid;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Dto.UpdateStatusRequest;
import org.gagan.florasense.Enum.Role;
import org.gagan.florasense.Service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    //1) Get All Users with Pagination and Sorting
    @GetMapping("/allUsers")
    public ResponseEntity<ResponseStructure<?>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return adminUserService.getAllUsers(search, role, active, page, size);
    }

    //2)Get User by using UserId
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseStructure<?>> getUserByUserId(@PathVariable("userId") Integer userId) {
        return adminUserService.getUserByUserId(userId);
    }

    //3)Activate/deactivate user
    @PatchMapping("update/{userId}/status")
    public ResponseEntity<ResponseStructure<?>> updateUserStatusByUserId(@Valid @PathVariable("userId") Integer userId, @RequestBody UpdateStatusRequest request) {
        return adminUserService.updateUserStatusByUserId(userId, request);
    }

    //4)Export all Users in the form of CSV file i.e Excel or Google Sheet(CSV stands for Comma-Separated Values)
    @GetMapping(value = "/export", produces = "text/csv") //it tell that contain CSV data instead of JSON
    public ResponseEntity<byte[]> exportUsers(){
        byte[] csv = adminUserService.exportUsers();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv") //treat the response as a downloadable attachment and suggests the filename users.csv
                .body(csv);
    }

    //5)Get all User Stats
    @GetMapping("allUsers/stats")
    public ResponseEntity<ResponseStructure<?>> getUserStats() {
        return adminUserService.getUserStats();
    }
}
