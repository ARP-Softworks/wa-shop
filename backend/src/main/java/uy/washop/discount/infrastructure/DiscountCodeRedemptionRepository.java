package uy.washop.discount.infrastructure;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uy.washop.discount.domain.DiscountCodeRedemption;
import uy.washop.order.domain.OrderStatus;

public interface DiscountCodeRedemptionRepository extends JpaRepository<DiscountCodeRedemption, UUID> {

    @Query("""
            SELECT COUNT(r) FROM DiscountCodeRedemption r
            WHERE r.discountCode.id = :codeId
              AND r.order.status NOT IN :excluded
            """)
    long countActiveByCode(
            @Param("codeId") UUID codeId,
            @Param("excluded") Collection<OrderStatus> excluded
    );

    @Query("""
            SELECT COUNT(r) > 0 FROM DiscountCodeRedemption r
            WHERE r.discountCode.id = :codeId
              AND r.phoneNormalized = :phone
              AND r.order.status NOT IN :excluded
            """)
    boolean existsActiveByCodeAndPhone(
            @Param("codeId") UUID codeId,
            @Param("phone") String phone,
            @Param("excluded") Collection<OrderStatus> excluded
    );
}
