package uy.washop.product.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uy.washop.product.domain.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProduct_IdOrderByPriceAsc(UUID productId);

    List<ProductVariant> findByProduct_IdAndPublishedTrueOrderByPriceAsc(UUID productId);

    List<ProductVariant> findByProduct_IdAndPublishedTrueAndStockGreaterThanOrderByPriceAsc(
            UUID productId,
            int stock
    );

    List<ProductVariant> findByProduct_IdIn(Collection<UUID> productIds);

    Optional<ProductVariant> findByIdAndPublishedTrue(UUID id);

    boolean existsByImei(String imei);

    boolean existsByImeiAndIdNot(String imei, UUID id);

    long countByProduct_Id(UUID productId);

    /**
     * A plain scalar aggregate — deliberately NOT "load all variants and sum in Java" — because
     * reserveStock()/restoreStock() below are bulk @Modifying updates that bypass the persistence
     * context. If a variant was already loaded earlier in the same transaction (as CheckoutService
     * does), Hibernate's identity map would hand back that stale cached entity instead of the
     * post-update row, and the sum would silently use the pre-decrement value.
     */
    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ProductVariant v WHERE v.product.id = :productId")
    int sumStockByProductId(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock - :quantity WHERE v.id = :id AND v.stock >= :quantity")
    int reserveStock(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :quantity WHERE v.id = :id")
    int restoreStock(@Param("id") UUID id, @Param("quantity") int quantity);
}
