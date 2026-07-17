package uy.washop.category.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.category.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByActiveTrueOrderByNameAsc();

    List<Category> findByActiveTrueAndIndexableTrueOrderByNameAsc();

    boolean existsBySlug(String slug);
}
