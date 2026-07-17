package uy.washop.technicalservice.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.technicalservice.api.dto.TechnicalServiceResponse;
import uy.washop.technicalservice.api.dto.TechnicalServiceWriteRequest;
import uy.washop.technicalservice.application.AdminTechnicalServiceService;

@RestController
@RequestMapping("/api/admin/technical-services")
public class AdminTechnicalServiceController {

    private final AdminTechnicalServiceService adminTechnicalServiceService;

    public AdminTechnicalServiceController(AdminTechnicalServiceService adminTechnicalServiceService) {
        this.adminTechnicalServiceService = adminTechnicalServiceService;
    }

    @GetMapping
    public List<TechnicalServiceResponse> list() {
        return adminTechnicalServiceService.listAll();
    }

    @GetMapping("/{id}")
    public TechnicalServiceResponse get(@PathVariable UUID id) {
        return adminTechnicalServiceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicalServiceResponse create(@Valid @RequestBody TechnicalServiceWriteRequest request) {
        return adminTechnicalServiceService.create(request);
    }

    @PutMapping("/{id}")
    public TechnicalServiceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody TechnicalServiceWriteRequest request
    ) {
        return adminTechnicalServiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminTechnicalServiceService.delete(id);
    }
}
