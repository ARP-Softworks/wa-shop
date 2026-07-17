package uy.washop.dashboard.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.dashboard.api.dto.DashboardResponse;
import uy.washop.dashboard.application.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public DashboardResponse get() {
        return adminDashboardService.getDashboard();
    }
}
