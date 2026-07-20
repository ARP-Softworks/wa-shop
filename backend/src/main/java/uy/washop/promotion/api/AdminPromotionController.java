package uy.washop.promotion.api;

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
import uy.washop.promotion.api.dto.PromotionResponse;
import uy.washop.promotion.api.dto.PromotionWriteRequest;
import uy.washop.promotion.application.AdminPromotionService;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;

    public AdminPromotionController(AdminPromotionService adminPromotionService) {
        this.adminPromotionService = adminPromotionService;
    }

    @GetMapping
    public List<PromotionResponse> listAll() {
        return adminPromotionService.listAll();
    }

    @GetMapping("/{id}")
    public PromotionResponse get(@PathVariable UUID id) {
        return adminPromotionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(@Valid @RequestBody PromotionWriteRequest request) {
        return adminPromotionService.create(request);
    }

    @PutMapping("/{id}")
    public PromotionResponse update(@PathVariable UUID id, @Valid @RequestBody PromotionWriteRequest request) {
        return adminPromotionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminPromotionService.delete(id);
    }
}
