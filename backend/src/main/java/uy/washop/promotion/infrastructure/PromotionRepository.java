package uy.washop.promotion.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.promotion.domain.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    List<Promotion> findByActiveTrueOrderByNameAsc();
}
