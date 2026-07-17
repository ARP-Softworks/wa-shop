package uy.washop.seo.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.seo.domain.UrlRedirect;

public interface UrlRedirectRepository extends JpaRepository<UrlRedirect, UUID> {

    Optional<UrlRedirect> findBySourcePathAndActiveTrue(String sourcePath);

    List<UrlRedirect> findByActiveTrue();

    Optional<UrlRedirect> findBySourcePath(String sourcePath);
}
