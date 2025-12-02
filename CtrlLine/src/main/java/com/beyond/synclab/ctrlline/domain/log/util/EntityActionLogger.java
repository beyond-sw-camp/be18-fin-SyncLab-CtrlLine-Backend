package com.beyond.synclab.ctrlline.domain.log.util;

import com.beyond.synclab.ctrlline.domain.log.event.LogEventPublisher;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.event.LogEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityActionLogger {

    @PostPersist
    public void afterPersist(Object entity) { publishEvent(entity, Logs.ActionType.CREATE); }

    @PostUpdate
    public void afterUpdate(Object entity) { publishEvent(entity, Logs.ActionType.UPDATE); }

    @PostRemove
    public void afterRemove(Object entity) { publishEvent(entity, Logs.ActionType.DELETE); }

    private String getTableName(Object entity) {
        Class<?> clazz = entity.getClass();
        if (clazz.isAnnotationPresent(Table.class)) {
            var table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                return table.name().replaceAll("[`\"]", ""); // 백틱, 쌍따옴표 제거
            }
        }
        return clazz.getSimpleName().replaceAll("[`\"]", ""); // fallback도 제거
    }

    private void publishEvent(Object entity, Logs.ActionType actionType) {
        log.debug("Publishing log event for entity: {}", entity);
        try {
            LogEventPublisher publisher = BeanUtils.getBean(LogEventPublisher.class);
            String entityName = getTableName(entity);
            Long entityId = extractId(entity);

            publisher.publish(new LogEvent(entityName, entityId, actionType));
        } catch (Exception e) {
            log.error("로그 이벤트 발행 실패", e);
        }
    }

    private Long extractId(Object entity) {
        try {
            EntityManager em = BeanUtils.getBean(EntityManager.class);
            PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
            Object id = util.getIdentifier(entity);
            return id != null ? (Long) id : null;
        } catch (Exception e) {
            log.warn("ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}