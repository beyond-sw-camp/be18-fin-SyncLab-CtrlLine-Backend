package com.beyond.synclab.ctrlline.domain.log.spec;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import jakarta.persistence.criteria.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class LogSpecification {
    public Specification<Logs> logsBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) ->
        {
            if (startDate == null && endDate == null) return null;

            Path<LocalDateTime> createdAt = root.get("createdAt");

            if (startDate != null && endDate != null) {
                return cb.between(
                    createdAt,
                    startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay());
            }

            if (endDate == null) {
                return cb.greaterThanOrEqualTo(createdAt, startDate.atStartOfDay());
            }

            return cb.lessThanOrEqualTo(createdAt, endDate.plusDays(1).atStartOfDay());
        };
    }

    public Specification<Logs> logsEntityNameEquals(String entityName) {
        return (root, query, cb) ->
            entityName == null || entityName.isBlank() ? null : cb.equal(root.get("entityName"), entityName);
    }

    public Specification<Logs> logsActionTypeEquals(ActionType actionType) {
        return (root, query, cb) ->
            actionType == null ? null : cb.equal(root.get("actionType"), actionType);
    }

    public Specification<Logs> logsUserIdEquals(Long userId) {
        return (root, query, cb) ->
            userId == null ? null : cb.equal(root.get("userId"), userId);
    }
}
