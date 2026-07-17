package uy.washop.technicalservice.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.shared.exception.ResourceNotFoundException;
import uy.washop.technicalservice.api.dto.TechnicalServiceResponse;
import uy.washop.technicalservice.api.mapper.TechnicalServiceMapper;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@RestController
@RequestMapping("/api/public/technical-services")
public class PublicTechnicalServiceController {

    private final TechnicalServiceRepository technicalServiceRepository;

    public PublicTechnicalServiceController(TechnicalServiceRepository technicalServiceRepository) {
        this.technicalServiceRepository = technicalServiceRepository;
    }

    @GetMapping
    public List<TechnicalServiceResponse> listActive() {
        return technicalServiceRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(TechnicalServiceMapper::toResponse)
                .toList();
    }

    @GetMapping("/{slug}")
    public TechnicalServiceResponse getBySlug(@PathVariable String slug) {
        return technicalServiceRepository.findBySlug(slug)
                .filter(service -> service.isActive())
                .map(TechnicalServiceMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }
}
