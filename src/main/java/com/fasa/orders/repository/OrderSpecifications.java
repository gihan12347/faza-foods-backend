package com.fasa.orders.repository;

import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.fasa.orders.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<OrderEntity> hasStatus(OrderStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> matchesSearch(String searchRaw) {
        if (!StringUtils.hasText(searchRaw)) {
            return (root, query, cb) -> cb.conjunction();
        }
        final String term = searchRaw.trim();
        final String like = "%" + term.toLowerCase(Locale.ROOT) + "%";

        return (root, query, cb) -> {
            query.distinct(true);
            Join<OrderEntity, OrderItemEntity> itemsJoin = root.join("items", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("district"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("addressLine1"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("addressLine2"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("orderSource"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("deliveryType"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(itemsJoin.get("name")), like));
            try {
                Long id = Long.parseLong(term);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {
                // not a numeric id
            }
            return cb.or(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
