package uy.washop.product.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.product.domain.ProductCompatibleModel;

public interface ProductCompatibleModelRepository extends JpaRepository<ProductCompatibleModel, UUID> {

    List<ProductCompatibleModel> findByProductIdOrderByModelAsc(UUID productId);
}
