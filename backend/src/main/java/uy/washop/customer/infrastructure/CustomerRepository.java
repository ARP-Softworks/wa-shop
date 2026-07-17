package uy.washop.customer.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uy.washop.customer.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByPhoneNormalized(String phoneNormalized);

    boolean existsByPhoneNormalized(String phoneNormalized);

    Page<Customer> findByNameContainingIgnoreCaseOrPhoneContainingOrEmailContainingIgnoreCase(
            String name, String phone, String email, Pageable pageable
    );
}
