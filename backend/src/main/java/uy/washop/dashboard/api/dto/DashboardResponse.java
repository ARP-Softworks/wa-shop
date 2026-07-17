package uy.washop.dashboard.api.dto;

import java.util.List;
import uy.washop.inquiry.api.dto.AdminInquirySummaryResponse;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.technicalservice.api.dto.TechnicalServiceResponse;

public record DashboardResponse(
        long publishedProducts,
        long newDevices,
        long usedDevices,
        long pendingInquiries,
        long pendingOrders,
        long activeTechnicalServices,
        List<AdminProductSummaryResponse> recentProducts,
        List<AdminInquirySummaryResponse> recentInquiries,
        List<TechnicalServiceResponse> activeServices
) {
}
