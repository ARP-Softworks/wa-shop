package uy.washop.product.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySlugAndPublishedTrue(String slug);

    Optional<Product> findByImei(String imei);

    List<Product> findByPublishedTrueAndProductTypeOrderByCreatedAtDesc(ProductType productType);

    List<Product> findByPublishedTrueAndConditionOrderByCreatedAtDesc(ProductCondition condition);

    List<Product> findByPublishedTrueAndFeaturedTrueOrderByUpdatedAtDesc();

    List<Product> findByPublishedTrueAndIndexableTrueOrderByUpdatedAtDesc();

    List<Product> findTop4ByPublishedTrueAndProductTypeAndIdNotOrderByFeaturedDescCreatedAtDesc(
            ProductType productType,
            UUID id
    );

    boolean existsBySlug(String slug);

    boolean existsByImei(String imei);

    long countByPublishedTrue();

    long countByPublishedTrueAndProductTypeAndCondition(ProductType productType, ProductCondition condition);

    /** Atomically reserves stock; returns affected rows (0 = insufficient stock, caller must check). */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int reserveStock(@Param("id") UUID id, @Param("quantity") int quantity);

    /** Restores previously reserved stock (order cancelled/rejected/expired). */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int restoreStock(@Param("id") UUID id, @Param("quantity") int quantity);
}
