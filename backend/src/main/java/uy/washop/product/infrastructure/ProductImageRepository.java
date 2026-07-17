package uy.washop.product.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uy.washop.product.domain.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdOrderByPositionAsc(UUID productId);

    Optional<ProductImage> findByProductIdAndMainImageTrue(UUID productId);

    @Query("""
            SELECT pi FROM ProductImage pi
            WHERE pi.product.id IN :productIds
            ORDER BY pi.mainImage DESC, pi.position ASC
            """)
    List<ProductImage> findByProductIdInOrderedForPrimary(@Param("productIds") Collection<UUID> productIds);

    boolean existsByPublicId(String publicId);

    long countByPublicId(String publicId);
}
