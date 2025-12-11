package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentStatusEvent;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentRuntimeStatusSnapshot;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentStatusStreamService {

    private static final long SSE_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(4);
    private static final String EVENT_NAME = "equipment-status";

    private final EquipmentRepository equipmentRepository;
    private final Map<Long, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, EquipmentLocation> locationCache = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public SseEmitter registerEmitter(Long factoryId, String factoryCode, Long lineId, String lineCode) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        long id = sequence.incrementAndGet();
        Subscription subscription = new Subscription(
                id,
                emitter,
                factoryId,
                normalize(factoryCode),
                lineId,
                normalize(lineCode)
        );
        subscriptions.put(id, subscription);
        emitter.onTimeout(() -> removeEmitter(id));
        emitter.onCompletion(() -> removeEmitter(id));
        emitter.onError(throwable -> removeEmitter(id));
        sendKeepAlive(emitter);
        return emitter;
    }

    public void broadcast(EquipmentRuntimeStatusSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        EquipmentLocation location = resolveLocation(snapshot.equipmentCode());
        EquipmentStatusEvent event = EquipmentStatusEvent.from(snapshot, location);
        subscriptions.values().forEach(subscription -> sendIfMatch(subscription, event));
    }

    private void sendIfMatch(Subscription subscription, EquipmentStatusEvent event) {
        boolean matches = subscription.matches(event.factoryId(), event.factoryCode(), event.lineId(), event.lineCode());
        if (!matches) {
            log.debug("SSE 필터 미스. emitterId={} subFactory=[{}, {}] subLine=[{}, {}] eventFactory=[{}, {}] eventLine=[{}, {}]",
                    subscription.id,
                    subscription.factoryId, subscription.factoryCode,
                    subscription.lineId, subscription.lineCode,
                    event.factoryId(), event.factoryCode(),
                    event.lineId(), event.lineCode());
            return;
        }
        try {
            subscription.emitter.send(SseEmitter.event().name(EVENT_NAME).data(event));
        } catch (IOException ex) {
            log.warn("SSE 전송 실패. emitterId={} equipmentCode={}", subscription.id, event.equipmentCode(), ex);
            removeEmitter(subscription.id);
        }
    }

    private void sendKeepAlive(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("keepalive").data("connected"));
        } catch (IOException ex) {
            log.debug("SSE keepalive 전송 실패", ex);
        }
    }

    private void removeEmitter(long id) {
        subscriptions.remove(id);
    }

    private EquipmentLocation resolveLocation(String equipmentCode) {
        if (!StringUtils.hasText(equipmentCode)) {
            return null;
        }
        EquipmentLocation cached = locationCache.get(equipmentCode);
        if (cached != null) {
            return cached;
        }
        EquipmentLocation located = equipmentRepository.findLocationByEquipmentCode(equipmentCode)
                .orElse(null);
        if (located != null) {
            locationCache.put(equipmentCode, located);
        } else {
            log.debug("설비 위치 정보를 찾지 못했습니다. equipmentCode={}", equipmentCode);
        }
        return located;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record Subscription(long id, SseEmitter emitter, Long factoryId, String factoryCode, Long lineId, String lineCode) {
        private boolean matches(Long eventFactoryId, String eventFactoryCode, Long eventLineId, String eventLineCode) {
            return matchesFactory(eventFactoryId, eventFactoryCode) && matchesLine(eventLineId, eventLineCode);
        }

        private boolean matchesFactory(Long eventFactoryId, String eventFactoryCode) {
            if (eventFactoryId != null && factoryId != null) {
                if (factoryId.equals(eventFactoryId)) {
                    return true;
                }
            }
            if (StringUtils.hasText(factoryCode) && StringUtils.hasText(eventFactoryCode)) {
                if (factoryCode.equalsIgnoreCase(eventFactoryCode.trim())) {
                    return true;
                }
            }
            if (factoryId == null && !StringUtils.hasText(factoryCode)) {
                return true;
            }
            if (factoryId != null) {
                return eventFactoryId != null && factoryId.equals(eventFactoryId);
            }
            if (StringUtils.hasText(factoryCode)) {
                return StringUtils.hasText(eventFactoryCode) && factoryCode.equalsIgnoreCase(eventFactoryCode.trim());
            }
            return false;
        }

        private boolean matchesLine(Long eventLineId, String eventLineCode) {
            if (eventLineId != null && lineId != null) {
                if (lineId.equals(eventLineId)) {
                    return true;
                }
            }
            if (StringUtils.hasText(lineCode) && StringUtils.hasText(eventLineCode)) {
                if (lineCode.equalsIgnoreCase(eventLineCode.trim())) {
                    return true;
                }
            }
            if (lineId == null && !StringUtils.hasText(lineCode)) {
                return true;
            }
            if (lineId != null) {
                return eventLineId != null && lineId.equals(eventLineId);
            }
            if (StringUtils.hasText(lineCode)) {
                return StringUtils.hasText(eventLineCode) && lineCode.equalsIgnoreCase(eventLineCode.trim());
            }
            return false;
        }
    }
}
