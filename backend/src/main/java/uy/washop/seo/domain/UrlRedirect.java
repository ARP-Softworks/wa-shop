package uy.washop.seo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "url_redirects")
public class UrlRedirect extends BaseEntity {

    @NotBlank
    @Size(max = 500)
    @Column(name = "source_path", nullable = false, length = 500, unique = true)
    private String sourcePath;

    @NotBlank
    @Size(max = 500)
    @Column(name = "destination_path", nullable = false, length = 500)
    private String destinationPath;

    @Min(301)
    @Max(308)
    @Column(name = "status_code", nullable = false)
    private int statusCode = 301;

    @Column(nullable = false)
    private boolean active = true;

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
