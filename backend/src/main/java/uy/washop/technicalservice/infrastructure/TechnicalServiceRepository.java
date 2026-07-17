package uy.washop.technicalservice.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.technicalservice.domain.TechnicalService;

public interface TechnicalServiceRepository extends JpaRepository<TechnicalService, UUID> {

    Optional<TechnicalService> findBySlug(String slug);

    List<TechnicalService> findByActiveTrueOrderByNameAsc();

    List<TechnicalService> findByActiveTrueAndIndexableTrueOrderByNameAsc();

    long countByActiveTrue();
}
