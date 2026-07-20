package uy.washop.discount.api;

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
import uy.washop.discount.api.dto.DiscountCodeResponse;
import uy.washop.discount.api.dto.DiscountCodeWriteRequest;
import uy.washop.discount.application.AdminDiscountCodeService;

@RestController
@RequestMapping("/api/admin/discount-codes")
public class AdminDiscountCodeController {

    private final AdminDiscountCodeService adminDiscountCodeService;

    public AdminDiscountCodeController(AdminDiscountCodeService adminDiscountCodeService) {
        this.adminDiscountCodeService = adminDiscountCodeService;
    }

    @GetMapping
    public List<DiscountCodeResponse> listAll() {
        return adminDiscountCodeService.listAll();
    }

    @GetMapping("/{id}")
    public DiscountCodeResponse get(@PathVariable UUID id) {
        return adminDiscountCodeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountCodeResponse create(@Valid @RequestBody DiscountCodeWriteRequest request) {
        return adminDiscountCodeService.create(request);
    }

    @PutMapping("/{id}")
    public DiscountCodeResponse update(@PathVariable UUID id, @Valid @RequestBody DiscountCodeWriteRequest request) {
        return adminDiscountCodeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminDiscountCodeService.delete(id);
    }
}
