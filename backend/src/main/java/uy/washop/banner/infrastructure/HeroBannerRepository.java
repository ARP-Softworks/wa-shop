package uy.washop.banner.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.banner.domain.HeroBanner;

public interface HeroBannerRepository extends JpaRepository<HeroBanner, UUID> {

    List<HeroBanner> findAllByOrderByPositionAsc();

    List<HeroBanner> findByActiveTrueOrderByPositionAsc();

    boolean existsByImagePublicId(String imagePublicId);
}
