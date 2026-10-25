package org.gagan.florasense.Controller;

import lombok.RequiredArgsConstructor;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    //Get all the stats of User and Plants for Dashboard
    @GetMapping("/stats")
    public ResponseEntity<ResponseStructure<?>> getUserAndPlantStats() {
        return adminDashboardService.getUserAndPlantStats();
    }
}
