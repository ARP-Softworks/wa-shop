package uy.washop.product.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.product.domain.ProductGroup;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, UUID> {

    Optional<ProductGroup> findBySlug(String slug);
}
