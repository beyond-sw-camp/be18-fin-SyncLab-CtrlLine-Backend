package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanResolver;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryProgressDto;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.LineProgressDto;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LineFinalInspectionProgressService {

    private final EquipmentRepository equipmentRepository;
    private final ProductionPlanResolver productionPlanResolver;

    private final Map<String, LineMachineProgress> machineProgress = new ConcurrentHashMap<>();
    private final Map<String, LineProgressSnapshot> snapshots = new ConcurrentHashMap<>();

    public void initializeProgress(ProductionPlans plan, String factoryCode, String lineCode) {
        if (plan == null || !StringUtils.hasText(plan.getDocumentNo()) || !StringUtils.hasText(lineCode)) {
            return;
        }
        String lineKey = buildLineKey(factoryCode, lineCode);
        machineProgress.put(lineKey, new LineMachineProgress(plan.getDocumentNo(), new HashMap<>()));
        snapshots.put(lineKey, new LineProgressSnapshot(
                factoryCode,
                lineCode,
                null,
                plan.getDocumentNo(),
                BigDecimal.ZERO,
                Optional.ofNullable(plan.getPlannedQty()).orElse(BigDecimal.ZERO),
                Instant.now()
        ));
    }

    public void clearProgress(String factoryCode, String lineCode) {
        if (!StringUtils.hasText(lineCode)) {
            return;
        }
        String lineKey = buildLineKey(factoryCode, lineCode);
        machineProgress.remove(lineKey);
        snapshots.remove(lineKey);
    }

    public void updateFromSummary(OrderSummaryTelemetryPayload payload) {
        if (payload == null) {
            return;
        }
        String orderNo = payload.orderNo();
        if (!StringUtils.hasText(orderNo)) {
            return;
        }

        Optional<MachineIdentity> identity = resolveMachineIdentity(payload);
        if (identity.isEmpty()) {
            return;
        }
        MachineIdentity machineId = identity.get();
        if (!isFinalInspectionMachine(machineId)) {
            return;
        }

        BigDecimal producedQty = Optional.ofNullable(payload.producedQuantity()).orElse(BigDecimal.ZERO);
        String lineKey = buildLineKey(machineId.factoryCode(), machineId.lineCode());
        machineProgress.compute(lineKey, (key, existing) -> {
            Map<String, BigDecimal> machines;
            if (existing == null || !Objects.equals(existing.orderNo, orderNo)) {
                machines = new HashMap<>();
            } else {
                machines = new HashMap<>(existing.machineProduced);
            }
            machines.put(machineId.equipmentCode(), producedQty);
            return new LineMachineProgress(orderNo, machines);
        });

        LineMachineProgress updatedProgress = machineProgress.get(lineKey);
        BigDecimal lineProduced = updatedProgress.machineProduced.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal targetQty = productionPlanResolver.resolveLatestPlan(orderNo)
                .map(ProductionPlans::getPlannedQty)
                .orElse(BigDecimal.ZERO);

        snapshots.put(lineKey, new LineProgressSnapshot(
                machineId.factoryCode(),
                machineId.lineCode(),
                machineId.equipmentCode(),
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

    public List<FactoryProgressDto> listFactoryProgress(String factoryCode) {
        Map<String, List<LineProgressDto>> grouped = listProgress(null).stream()
                .collect(Collectors.groupingBy(LineProgressDto::factoryCode));

        return grouped.entrySet().stream()
                .filter(entry -> factoryCode == null
                        || factoryCode.equalsIgnoreCase(entry.getKey()))
                .map(entry -> {
                    List<LineProgressDto> lines = entry.getValue();
                    BigDecimal totalProduced = lines.stream()
                            .map(LineProgressDto::producedQty)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalTarget = lines.stream()
                            .map(LineProgressDto::targetQty)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long updatedAt = lines.stream()
                            .mapToLong(LineProgressDto::updatedAt)
                            .max()
                            .orElse(0L);
                    BigDecimal progressRate = calculateProgressRate(totalProduced, totalTarget);
                    return FactoryProgressDto.of(
                            entry.getKey(),
                            totalProduced,
                            totalTarget,
                            progressRate,
                            updatedAt,
                            lines
                    );
                })
                .sorted(Comparator.comparing(FactoryProgressDto::factoryCode,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
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

    private Optional<MachineIdentity> resolveMachineIdentity(OrderSummaryTelemetryPayload payload) {
        String machine = payload.machine();
        MachineIdentity identity = parseMachine(machine);
        if (identity != null) {
            return Optional.of(identity);
        }
        String equipmentCode = payload.equipmentCode();
        if (!StringUtils.hasText(equipmentCode)) {
            return Optional.empty();
        }
        Equipments equipment = equipmentRepository.findByEquipmentCode(equipmentCode).orElse(null);
        if (equipment == null || !isFinalInspection(equipment)) {
            return Optional.empty();
        }
        EquipmentLocation location = equipmentRepository.findLocationByEquipmentCode(equipmentCode).orElse(null);
        if (location == null || !StringUtils.hasText(location.lineCode())) {
            return Optional.empty();
        }
        return Optional.of(new MachineIdentity(
                location.factoryCode(),
                location.lineCode(),
                equipmentCode
        ));
    }

    private MachineIdentity parseMachine(String machine) {
        if (!StringUtils.hasText(machine)) {
            return null;
        }
        String[] parts = machine.split("\\.");

        if (parts.length < 3) {
            return null;
        }
        return new MachineIdentity(parts[0], parts[1], parts[2]);
    }

    private boolean isFinalInspectionMachine(MachineIdentity identity) {
        if (identity == null) {
            return false;
        }
        String equipmentPart = identity.equipmentCode();
        return StringUtils.hasText(equipmentPart) && equipmentPart.toLowerCase().contains("finalinspection");
    }

    private BigDecimal calculateProgressRate(BigDecimal produced, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sanitizedProduced = (produced == null)
                ? BigDecimal.ZERO
                : produced.max(BigDecimal.ZERO);
        return sanitizedProduced
                .divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String buildLineKey(String factoryCode, String lineCode) {
        return StringUtils.hasText(factoryCode)
                ? factoryCode + ":" + lineCode
                : ":" + lineCode;
    }

    private record LineProgressSnapshot(
            String factoryCode,
            String lineCode,
            String equipmentCode,
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

    private record MachineIdentity(
            String factoryCode,
            String lineCode,
            String equipmentCode
    ) {}
}
