package uy.washop.category.api;

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
import uy.washop.category.api.dto.CategoryResponse;
import uy.washop.category.api.dto.CategoryWriteRequest;
import uy.washop.category.application.AdminCategoryService;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return adminCategoryService.listAll();
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        return adminCategoryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryWriteRequest request) {
        return adminCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryWriteRequest request) {
        return adminCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminCategoryService.delete(id);
    }
}
