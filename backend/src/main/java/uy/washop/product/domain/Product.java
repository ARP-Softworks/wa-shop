package uy.washop.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uy.washop.category.domain.Category;
import uy.washop.shared.domain.BaseEntity;
import uy.washop.shared.domain.CurrencyCode;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @NotBlank
    @Size(max = 220)
    @Column(nullable = false, length = 220, unique = true)
    private String slug;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 120)
    @Column(length = 120)
    private String model;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private ProductType productType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCondition condition;

    @Size(max = 40)
    @Column(name = "storage_capacity", length = 40)
    private String storageCapacity;

    @Size(max = 80)
    @Column(length = 80)
    private String color;

    @Min(0)
    @Max(100)
    @Column(name = "battery_health")
    private Integer batteryHealth;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @DecimalMin("0.00")
    @Column(name = "previous_price", precision = 12, scale = 2)
    private BigDecimal previousPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Min(0)
    @Column(nullable = false)
    private int stock;

    @Size(max = 200)
    @Column(length = 200)
    private String warranty;

    @Size(min = 14, max = 17)
    @Column(length = 20)
    private String imei;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean featured;

    @Size(max = 70)
    @Column(name = "seo_title", length = 70)
    private String seoTitle;

    @Size(max = 320)
    @Column(name = "meta_description", length = 320)
    private String metaDescription;

    @Column(nullable = false)
    private boolean indexable = true;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public ProductCondition getCondition() {
        return condition;
    }

    public void setCondition(ProductCondition condition) {
        this.condition = condition;
    }

    public String getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(Integer batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(BigDecimal previousPrice) {
        this.previousPrice = previousPrice;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
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

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public UUID getCategoryId() {
        return category != null ? category.getId() : null;
    }
}
