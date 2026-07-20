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

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock - :quantity WHERE v.id = :id AND v.stock >= :quantity")
    int reserveStock(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :quantity WHERE v.id = :id")
    int restoreStock(@Param("id") UUID id, @Param("quantity") int quantity);
}
