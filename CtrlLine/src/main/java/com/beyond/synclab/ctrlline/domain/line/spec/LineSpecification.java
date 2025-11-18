package com.beyond.synclab.ctrlline.domain.line.spec;

import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LineSpecification {

    public static Specification<Lines> lineNameContains(String lineName) {
        return (root, query, cb) ->
                (lineName == null || lineName.isEmpty())
                        ? null
                        : cb.like(root.get("lineName"), "%" + lineName + "%");
    }

    public static Specification<Lines> lineCodeEquals(String lineCode) {
        return (root, query, cb) ->
                (lineCode == null || lineCode.isEmpty())
                        ? null
                        : cb.equal(root.get("lineCode"), lineCode);
    }

    public static Specification<Lines> activeEquals(Boolean isActive) {
        return (root, query, cb) ->
                (isActive == null)
                        ? null
                        : cb.equal(root.get("isActive"), isActive);
    }

    // User 관련 조건을 하나로 통합
    public static Specification<Lines> withUserConditions(String userName, String department) {
        return (root, query, cb) -> {
            if ((userName == null || userName.isEmpty()) &&
                    (department == null || department.isEmpty())) {
                return null;
            }

            Join<Lines, Users> userJoin = root.join("user", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (userName != null && !userName.isEmpty()) {
                predicates.add(cb.like(userJoin.get("name"), "%" + userName + "%"));
            }

            if (department != null && !department.isEmpty()) {
                predicates.add(cb.equal(userJoin.get("department"), department));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
