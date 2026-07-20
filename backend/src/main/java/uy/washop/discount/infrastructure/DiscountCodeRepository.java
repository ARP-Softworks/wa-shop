package uy.washop.discount.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uy.washop.discount.domain.DiscountCode;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {

    Optional<DiscountCode> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    boolean existsByCodeIgnoreCase(String code);

    List<DiscountCode> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("""
            UPDATE DiscountCode d
            SET d.usedCount = d.usedCount + 1
            WHERE d.id = :id
              AND (d.maxUses IS NULL OR d.usedCount < d.maxUses)
            """)
    int incrementUsedCountIfAvailable(@Param("id") UUID id);
}
