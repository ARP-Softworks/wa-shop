package uy.washop.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Size(max = 255)
    @Column(name = "public_id", length = 255)
    private String publicId;

    @Size(max = 255)
    @Column(name = "alt_text", length = 255)
    private String altText;

    @Min(0)
    @Column(nullable = false)
    private int position;

    @Column(name = "main_image", nullable = false)
    private boolean mainImage;

    @Size(max = 20)
    @Column(length = 20)
    private String format;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    private Integer width;

    private Integer height;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isMainImage() {
        return mainImage;
    }

    public void setMainImage(boolean mainImage) {
        this.mainImage = mainImage;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
