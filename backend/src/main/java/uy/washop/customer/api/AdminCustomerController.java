package uy.washop.customer.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import uy.washop.customer.api.dto.CustomerResponse;
import uy.washop.customer.api.dto.CustomerWriteRequest;
import uy.washop.customer.application.AdminCustomerService;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public PageResponse<CustomerResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminCustomerService.search(q, page, size);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id) {
        return adminCustomerService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerWriteRequest request) {
        return adminCustomerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody CustomerWriteRequest request) {
        return adminCustomerService.update(id, request);
    }
}
