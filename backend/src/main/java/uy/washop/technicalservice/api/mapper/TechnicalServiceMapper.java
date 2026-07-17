package uy.washop.technicalservice.api.mapper;

import uy.washop.technicalservice.api.dto.TechnicalServiceResponse;
import uy.washop.technicalservice.domain.TechnicalService;

public final class TechnicalServiceMapper {

    private TechnicalServiceMapper() {
    }

    public static TechnicalServiceResponse toResponse(TechnicalService service) {
        return new TechnicalServiceResponse(
                service.getId(),
                service.getName(),
                service.getSlug(),
                service.getDescription(),
                service.getPrice(),
                service.getCurrency(),
                service.getEstimatedTime(),
                service.isActive(),
                service.getSeoTitle(),
                service.getMetaDescription(),
                service.isIndexable(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
