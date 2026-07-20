package uy.washop.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import uy.washop.category.domain.Category;
import uy.washop.shared.domain.BaseEntity;

/** "Buy N from trigger category, get M from reward category at X% off." Same category on both
 *  sides covers mix-and-match (any 3 cases, cheapest one discounted). */
@Entity
@Table(name = "promotions")
public class Promotion extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_category_id", nullable = false)
    private Category triggerCategory;

    @Min(1)
    @Column(name = "trigger_quantity", nullable = false)
    private int triggerQuantity;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_category_id", nullable = false)
    private Category rewardCategory;

    @Min(1)
    @Column(name = "reward_quantity", nullable = false)
    private int rewardQuantity;

    @Min(1)
    @Max(100)
    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Category getTriggerCategory() {
        return triggerCategory;
    }

    public void setTriggerCategory(Category triggerCategory) {
        this.triggerCategory = triggerCategory;
    }

    public UUID getTriggerCategoryId() {
        return triggerCategory != null ? triggerCategory.getId() : null;
    }

    public int getTriggerQuantity() {
        return triggerQuantity;
    }

    public void setTriggerQuantity(int triggerQuantity) {
        this.triggerQuantity = triggerQuantity;
    }

    public Category getRewardCategory() {
        return rewardCategory;
    }

    public void setRewardCategory(Category rewardCategory) {
        this.rewardCategory = rewardCategory;
    }

    public UUID getRewardCategoryId() {
        return rewardCategory != null ? rewardCategory.getId() : null;
    }

    public int getRewardQuantity() {
        return rewardQuantity;
    }

    public void setRewardQuantity(int rewardQuantity) {
        this.rewardQuantity = rewardQuantity;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }
}
