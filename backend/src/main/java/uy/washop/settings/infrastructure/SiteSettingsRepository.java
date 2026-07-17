package uy.washop.settings.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.settings.domain.SiteSettings;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, UUID> {

    boolean existsByLogoPublicId(String logoPublicId);
}
