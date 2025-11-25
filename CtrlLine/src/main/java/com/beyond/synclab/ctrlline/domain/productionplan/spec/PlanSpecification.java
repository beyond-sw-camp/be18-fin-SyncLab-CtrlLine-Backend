package com.beyond.synclab.ctrlline.domain.productionplan.spec;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class PlanSpecification {
    private final String itemLineColumn = "itemLine";

    public Specification<ProductionPlans> planStatusEquals(PlanStatus planStatus) {
        return (root, query, cb) ->
            planStatus == null
                ? null
                : cb.equal(root.get("status"), planStatus);
    }

    public Specification<ProductionPlans> planFactoryNameContains(String factoryName) {
        return (root, query, cb) -> {
            if (factoryName == null)
                return null;

            // plan -> itemLine -> line -> factory
            Join<ProductionPlans, ItemsLines> itemLine = root.join(itemLineColumn, JoinType.LEFT);
            Join<ItemsLines, Lines> line = itemLine.join("line", JoinType.LEFT);
            Join<Lines, Factories> factory = line.join("factory", JoinType.LEFT);

            return cb.equal(factory.get("factoryName"), factoryName);
        };
    }

    public Specification<ProductionPlans> planItemNameContains(String itemName) {
        return (root, query, cb) -> {
            if (itemName == null)
                return null;

            // plan -> itemLine -> item
            Join<ProductionPlans, ItemsLines> itemLine = root.join(itemLineColumn, JoinType.LEFT);
            Join<ItemsLines, Items> item = itemLine.join("item", JoinType.LEFT);

            return cb.like(item.get("itemName"), "%" + itemName + "%");
        };
    }

    public Specification<ProductionPlans> planSalesManagerNameContains(String name) {
        return (root, query, cb) -> {
            if (name == null)
                return null;

            Join<ProductionPlans, Users> pm = root.join("salesManager", JoinType.LEFT);
            return cb.like(pm.get("name"), "%" + name + "%");
        };
    }

    public Specification<ProductionPlans> planProductionManagerNameContains(String name) {
        return (root, query, cb) -> {
            if (name == null)
                return null;

            Join<ProductionPlans, Users> pm = root.join("productionManager", JoinType.LEFT);
            return cb.like(pm.get("name"), "%" + name + "%");
        };
    }

    public Specification<ProductionPlans> planDueDateBefore(LocalDate dueDate) {
        return (root, query, cb) -> {
            if (dueDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("dueDate"), dueDate);
        };
    }

    public Specification<ProductionPlans> planStartTimeAfter(LocalDateTime start) {
        return (root, query, cb) -> {
            if (start == null) return null;
            return cb.greaterThanOrEqualTo(root.get("startTime"), start);
        };
    }

    public Specification<ProductionPlans> planEndTimeBefore(LocalDateTime end) {
        return (root, query, cb) -> {
            if (end == null) return null;
            return cb.lessThanOrEqualTo(root.get("endTime"), end);
        };
    }

    public Specification<ProductionPlans> planLineNameContains(String lineName) {
        return (root, query, cb) -> {
            if (lineName == null)
                return null;

            // plan -> itemLine -> line
            Join<ProductionPlans, ItemsLines> itemLine = root.join(itemLineColumn, JoinType.LEFT);
            Join<ItemsLines, Lines> line = itemLine.join("line", JoinType.LEFT);

            return cb.like(line.get("lineName"), "%" + lineName + "%");
        };
    }

    public Specification<ProductionPlans> planItemCodeEquals(String itemCode) {
        return (root, query, cb) -> {
            if (itemCode == null)
                return null;

            // plan -> itemLine -> line
            Join<ProductionPlans, ItemsLines> itemLine = root.join(itemLineColumn, JoinType.LEFT);
            Join<ItemsLines, Items> item = itemLine.join("item", JoinType.LEFT);

            return cb.equal(item.get("itemCode"), itemCode);
        };
    }
}
