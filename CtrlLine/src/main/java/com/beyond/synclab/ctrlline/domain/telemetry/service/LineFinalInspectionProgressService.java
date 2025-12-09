package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanResolver;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.LineProgressDto;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LineFinalInspectionProgressService {

    private final EquipmentRepository equipmentRepository;
    private final ProductionPlanResolver productionPlanResolver;

    private final Map<String, LineMachineProgress> machineProgress = new ConcurrentHashMap<>();
    private final Map<String, LineProgressSnapshot> snapshots = new ConcurrentHashMap<>();

    public void updateFromSummary(OrderSummaryTelemetryPayload payload) {
        if (payload == null) return;
        String equipmentCode = payload.equipmentCode();
        if (!StringUtils.hasText(equipmentCode)) {
            return;
        }
        Equipments equipment = equipmentRepository.findByEquipmentCode(equipmentCode).orElse(null);
        if (!isFinalInspection(equipment)) {
            return;
        }
        EquipmentLocation location = equipmentRepository.findLocationByEquipmentCode(equipmentCode).orElse(null);
        if (location == null || !StringUtils.hasText(location.lineCode())) {
            return;
        }
        String lineCode = location.lineCode();
        String orderNo = payload.orderNo();
        if (!StringUtils.hasText(orderNo)) {
            return;
        }
        BigDecimal producedQty = Optional.ofNullable(payload.producedQuantity()).orElse(BigDecimal.ZERO);

        machineProgress.compute(lineCode, (key, existing) -> {
            Map<String, BigDecimal> machines;
            if (existing == null || !Objects.equals(existing.orderNo, orderNo)) {
                machines = new HashMap<>();
            } else {
                machines = new HashMap<>(existing.machineProduced);
            }
            machines.put(equipmentCode, producedQty);
            return new LineMachineProgress(orderNo, machines);
        });

        LineMachineProgress updatedProgress = machineProgress.get(lineCode);
        BigDecimal lineProduced = updatedProgress.machineProduced.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal targetQty = productionPlanResolver.resolveLatestPlan(orderNo)
                .map(ProductionPlans::getPlannedQty)
                .orElse(BigDecimal.ZERO);

        snapshots.put(lineCode, new LineProgressSnapshot(
                location.factoryCode(),
                lineCode,
                orderNo,
                lineProduced,
                targetQty,
                Instant.now()
        ));
    }

    public List<LineProgressDto> listProgress(String factoryCode) {
        return snapshots.values().stream()
                .filter(entry -> factoryCode == null ||
                        factoryCode.equalsIgnoreCase(entry.factoryCode()))
                .map(entry -> new LineProgressDto(
                        entry.factoryCode(),
                        entry.lineCode(),
                        entry.orderNo(),
                        entry.producedQty(),
                        entry.targetQty(),
                        entry.updatedAt().toEpochMilli()
                ))
                .toList();
    }

    private boolean isFinalInspection(Equipments equipment) {
        if (equipment == null) {
            return false;
        }
        String type = equipment.getEquipmentType();
        if (StringUtils.hasText(type) && "finalinspection".equalsIgnoreCase(type)) {
            return true;
        }
        String name = equipment.getEquipmentName();
        return StringUtils.hasText(name) && name.toLowerCase().contains("finalinspection");
    }

    private record LineProgressSnapshot(
            String factoryCode,
            String lineCode,
            String orderNo,
            BigDecimal producedQty,
            BigDecimal targetQty,
            Instant updatedAt
    ) {}

    private static final class LineMachineProgress {
        private final String orderNo;
        private final Map<String, BigDecimal> machineProduced;

        private LineMachineProgress(String orderNo, Map<String, BigDecimal> machineProduced) {
            this.orderNo = orderNo;
            this.machineProduced = machineProduced;
        }
    }
}
