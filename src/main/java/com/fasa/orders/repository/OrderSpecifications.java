package com.fasa.orders.repository;

import com.fasa.orders.controller.OrderController;
import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.fasa.orders.entity.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecifications {
    private static final Logger log = LoggerFactory.getLogger(OrderSpecifications.class);

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
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("customerName"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("addressLine1"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("addressLine2"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("orderSource"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("deliveryType"), cb.literal(""))), like));
            predicates.add(cb.like(cb.lower(itemsJoin.get("name")), like));
            try {
                Long id = Long.parseLong(term);
                predicates.add(cb.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {
                log.error("convert format error : " + ignored.getMessage());
            }
            return cb.or(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
