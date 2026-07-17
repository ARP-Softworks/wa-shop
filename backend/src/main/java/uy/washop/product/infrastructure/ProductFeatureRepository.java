package uy.washop.product.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.product.domain.ProductFeature;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, UUID> {

    List<ProductFeature> findByProductIdOrderByNameAsc(UUID productId);
}
