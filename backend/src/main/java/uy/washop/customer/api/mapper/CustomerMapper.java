package uy.washop.customer.api.mapper;

import uy.washop.customer.api.dto.CustomerResponse;
import uy.washop.customer.domain.Customer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getNotes(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
