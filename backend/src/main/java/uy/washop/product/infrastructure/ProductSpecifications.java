package uy.washop.product.infrastructure;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uy.washop.product.api.dto.ProductSearchCriteria;
import uy.washop.product.domain.Product;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> fromPublicCriteria(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("category", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("published")));

            if (criteria == null) {
                return cb.and(predicates.toArray(Predicate[]::new));
            }

            if (criteria.productType() != null) {
                predicates.add(cb.equal(root.get("productType"), criteria.productType()));
            }
            if (criteria.condition() != null) {
                predicates.add(cb.equal(root.get("condition"), criteria.condition()));
            }
            if (StringUtils.hasText(criteria.model())) {
                predicates.add(cb.equal(cb.lower(root.get("model")), criteria.model().trim().toLowerCase()));
            }
            if (StringUtils.hasText(criteria.storageCapacity())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("storageCapacity")),
                        criteria.storageCapacity().trim().toLowerCase()
                ));
            }
            if (StringUtils.hasText(criteria.color())) {
                predicates.add(cb.equal(cb.lower(root.get("color")), criteria.color().trim().toLowerCase()));
            }
            if (criteria.minBatteryHealth() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("batteryHealth"), criteria.minBatteryHealth()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }
            if (Boolean.TRUE.equals(criteria.featured())) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (Boolean.TRUE.equals(criteria.inStock())) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }
            if (StringUtils.hasText(criteria.q())) {
                String pattern = "%" + criteria.q().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("model")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("color")), pattern)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
