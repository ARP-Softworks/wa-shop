package uy.washop.banner.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

/** A rotating promotional image on the home page hero, optionally clickable to a link. */
@Entity
@Table(name = "hero_banners")
public class HeroBanner extends BaseEntity {

    @NotBlank
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Size(max = 300)
    @Column(name = "image_public_id", length = 300)
    private String imagePublicId;

    @Size(max = 255)
    @Column(name = "alt_text", length = 255)
    private String altText;

    @Size(max = 500)
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean active = true;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(String imagePublicId) {
        this.imagePublicId = imagePublicId;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
