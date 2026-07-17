package uy.washop.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Size(max = 140)
    @Column(nullable = false, length = 140, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Size(max = 70)
    @Column(name = "seo_title", length = 70)
    private String seoTitle;

    @Size(max = 320)
    @Column(name = "meta_description", length = 320)
    private String metaDescription;

    @Column(nullable = false)
    private boolean indexable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public boolean isIndexable() {
        return indexable;
    }

    public void setIndexable(boolean indexable) {
        this.indexable = indexable;
    }
}
