package uy.washop.customer.application;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.customer.api.dto.CustomerResponse;
import uy.washop.customer.api.dto.CustomerWriteRequest;
import uy.washop.customer.api.mapper.CustomerMapper;
import uy.washop.customer.domain.Customer;
import uy.washop.customer.domain.PhoneNormalizer;
import uy.washop.customer.infrastructure.CustomerRepository;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public AdminCustomerService(CustomerRepository customerRepository, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 48),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String term = StringUtils.hasText(q) ? q.trim() : "";
        Page<Customer> result = customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContainingOrEmailContainingIgnoreCase(
                        term, term, term, pageable
                );
        return new PageResponse<>(
                result.getContent().stream().map(CustomerMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        return CustomerMapper.toResponse(require(id));
    }

    @Transactional
    public CustomerResponse create(CustomerWriteRequest request) {
        String normalizedPhone = PhoneNormalizer.normalize(request.phone());
        validatePhone(normalizedPhone, null);
        Customer customer = new Customer();
        apply(customer, request, normalizedPhone);
        customer = customerRepository.save(customer);
        auditService.record(AuditAction.CREATE, "Customer", customer.getId(), "Cliente creado: " + customer.getName());
        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerWriteRequest request) {
        Customer customer = require(id);
        String normalizedPhone = PhoneNormalizer.normalize(request.phone());
        validatePhone(normalizedPhone, id);
        apply(customer, request, normalizedPhone);
        customer = customerRepository.save(customer);
        auditService.record(AuditAction.UPDATE, "Customer", id, "Cliente actualizado: " + customer.getName());
        return CustomerMapper.toResponse(customer);
    }

    private void apply(Customer customer, CustomerWriteRequest request, String normalizedPhone) {
        customer.setName(request.name().trim());
        customer.setPhone(request.phone().trim());
        customer.setPhoneNormalized(normalizedPhone);
        customer.setEmail(StringUtils.hasText(request.email()) ? request.email().trim() : null);
        customer.setAddress(StringUtils.hasText(request.address()) ? request.address().trim() : null);
        customer.setNotes(StringUtils.hasText(request.notes()) ? request.notes().trim() : null);
    }

    private void validatePhone(String normalizedPhone, UUID currentId) {
        if (!StringUtils.hasText(normalizedPhone)) {
            throw new BusinessConflictException("El teléfono es obligatorio");
        }
        customerRepository.findByPhoneNormalized(normalizedPhone).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessConflictException("Ya existe un cliente con ese teléfono");
            }
        });
    }

    private Customer require(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }
}
