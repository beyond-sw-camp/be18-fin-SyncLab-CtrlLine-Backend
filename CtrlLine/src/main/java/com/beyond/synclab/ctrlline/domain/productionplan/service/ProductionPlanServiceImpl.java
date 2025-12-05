package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemErrorCode;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.errorcode.ItemLineErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.AffectedPlanDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.DeleteProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.DueDateExceededPlanDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanBoundaryResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.PlanScheduleChangeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanStatusResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.UpdateProductionPlanStatusRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.spec.PlanSpecification;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private static final BigDecimal TRAY_CAPACITY = BigDecimal.valueOf(36L);
    private static final int PERFORMANCE_SAMPLE_SIZE = 5;
    private static final long BASE_BUFFER_MINUTES = 30;

    private final ProductionPlanRepository productionPlanRepository;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;
    private final FactoryRepository factoryRepository;
    private final ItemLineRepository itemLineRepository;
    private final ItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final ProductionPerformanceRepository productionPerformanceRepository;
    private final ProductionPlanStatusNotificationService planStatusNotificationService;
    private final Clock clock;


    private List<ProductionPlans> findAllActivePlans(Long lineId) {
        return productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
            lineId, List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
        );
    }

    private ProductionPlans findPlanById(Long planId) {
        return productionPlanRepository.findById(planId)
            .orElseThrow(() -> new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND));
    }

    private Users findUserByEmpNo(String empNo) {
        if (empNo == null) return null;
        return userRepository.findByEmpNo(empNo)
            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

    private Items findItem(String itemCode) {
        if (itemCode == null) return null;
        return itemRepository.findByItemCode(itemCode)
            .orElseThrow(() -> new AppException(ItemErrorCode.ITEM_NOT_FOUND));
    }

    private ItemsLines findValidatedItemLine(Lines line, Items item) {
        if (item == null) return null;

        Optional<ItemsLines> itemsLinesOptional = itemLineRepository
            .findByLineIdAndItemId(line.getId(), item.getId());


        if (itemsLinesOptional.isEmpty()) {
            throw new AppException(ItemLineErrorCode.ITEM_LINE_NOT_FOUND);
        }

        return itemsLinesOptional.get();
    }


    private void validateRequestedStatusByRole(PlanStatus newStatus, Users requester) {
        if (!requester.isManagerRole() || newStatus == null) return;

        // 관리자일때 수정 요청 확인
        if (!(newStatus == PlanStatus.PENDING || newStatus == PlanStatus.CONFIRMED)) {
            log.debug("담당자는 PENDING, CONFIRMED 요청으로만 수정가능합니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
        }
    }



    private void validateEndAndStartTime(LocalDateTime startTime) {
        if (startTime == null) return;

        if (startTime.isBefore(LocalDateTime.now(clock))) {
            log.debug("시작시간이 오늘 날짜 이전입니다.");
            throw new AppException(CommonErrorCode.INVALID_REQUEST);
        }
    }


    private void validateUpdatable(ProductionPlans plan) {
        if (plan == null) return;
        if (!plan.isUpdatable()) {
            log.debug("해당 플랜은 업데이트가 불가능합니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
        }
    }

    private void checkDueDate(ProductionPlans plan, LocalDateTime endTime) {
        LocalDate dueDate = plan.getDueDate();

        if (dueDate != null) {
            LocalDateTime dueDateEnd = dueDate.atStartOfDay().withHour(12);

            if (endTime.isAfter(dueDateEnd)) {
                log.debug(
                    "납기일 초과로 등록 됩니다. end={}, dueDate={}",
                    endTime, dueDateEnd
                );

                planStatusNotificationService.notifyDueDateExceeded(plan);
            }
        }
    }

    private void setEmergentPlanTime(ProductionPlans newPlan,
        Long lineId,
        List<Equipments> equipments
    ) {

        // 1) 긴급계획 Start/End 설정 (맨 앞 배치)
        //    첫 계획으로 예약하므로 start = now + 10m
        LocalDateTime start = LocalDateTime.now(clock).plusMinutes(10);
        LocalDateTime end = calculateEndTime(lineId, equipments, newPlan.getPlannedQty(), start);

        newPlan.updateStartTime(start);
        newPlan.updateEndTime(end);
    }

    private List<ProductionPlans> buildFullPlanList(
        ProductionPlans newPlan,
        List<ProductionPlans> existingPlans
    ) {
        List<ProductionPlans> full = new ArrayList<>();
        full.add(newPlan);
        full.addAll(existingPlans);
        full.sort(Comparator.comparing(ProductionPlans::getStartTime));
        return full;
    }

    private List<ProductionPlans> buildPlansForUpdate(
        ProductionPlans updatedPlan,
        Long lineId
    ) {
        ArrayList<ProductionPlans> dbPlans = new ArrayList<>(findAllActivePlans(lineId));

        dbPlans.removeIf(p -> p.getId().equals(updatedPlan.getId()));

        return buildFullPlanList(updatedPlan, dbPlans);
    }

    private PlanScheduleChangeResponseDto buildScheduleChangeResponse(
        ProductionPlans targetPlan,
        Map<Long, LocalDateTime> beforeStart,
        Map<Long, LocalDateTime> beforeEnd,
        List<ProductionPlans> afterPlans
    ) {

        List<AffectedPlanDto> affected = new ArrayList<>();
        List<DueDateExceededPlanDto> dueExceeded = new ArrayList<>();

        for (ProductionPlans p : afterPlans) {

            LocalDateTime oldStart = beforeStart.get(p.getId());
            LocalDateTime oldEnd = beforeEnd.get(p.getId());
            LocalDateTime newStart = p.getStartTime();
            LocalDateTime newEnd = p.getEndTime();

            // 변경된 계획만
            if (!newStart.equals(oldStart) || !newEnd.equals(oldEnd)) {
                affected.add(
                    AffectedPlanDto.builder()
                        .id(p.getId())
                        .oldStartTime(oldStart)
                        .oldEndTime(oldEnd)
                        .newStartTime(newStart)
                        .newEndTime(newEnd)
                        .build()
                );
            }

            if (p.getDueDate() != null) {
                LocalDateTime dueLimit = p.getDueDateTime(clock);
                if (newEnd.isAfter(dueLimit)) {
                    dueExceeded.add(
                        DueDateExceededPlanDto.builder()
                            .id(p.getId())
                            .newEndTime(newEnd)
                            .dueDateLimit(dueLimit)
                            .build()
                    );
                }
            }
        }

        return PlanScheduleChangeResponseDto.builder()
            .planId(targetPlan.getId())
            .planDocumentNo(targetPlan.getDocumentNo())
            .affectedPlans(affected)
            .dueDateExceededPlans(dueExceeded)
            .build();
    }

    /* ===========================================================
     *  공통 유틸
     * ===========================================================
     */

    private LocalDateTime withBuffer(LocalDateTime endTime) {
        return endTime.plusMinutes(BASE_BUFFER_MINUTES);
    }

    private Map<Long, LocalDateTime> snapshotStart(List<ProductionPlans> plans) {
        return plans.stream().collect(Collectors.toMap(ProductionPlans::getId, ProductionPlans::getStartTime));
    }

    private Map<Long, LocalDateTime> snapshotEnd(List<ProductionPlans> plans) {
        return plans.stream().collect(Collectors.toMap(ProductionPlans::getId, ProductionPlans::getEndTime));
    }

    @Override
    @Transactional
    public PlanScheduleChangeResponseDto createProductionPlan(
        CreateProductionPlanRequestDto requestDto,
        Users user
    ) {
        Users salesManager = findUserByEmpNo(requestDto.getSalesManagerNo());
        Users productionManager = findUserByEmpNo(requestDto.getProductionManagerNo());
        Lines line = lineRepository.findBylineCode(requestDto.getLineCode())
            .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));
        Items item = itemRepository.findByItemCode(requestDto.getItemCode())
            .orElseThrow(() -> new AppException(ItemErrorCode.ITEM_NOT_FOUND));

        List<Equipments> processingEquips = equipmentRepository.findAllByLineId(line.getId());

        ItemsLines itemsLines = itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())
                .orElseThrow(() -> {
                    log.debug("ItemLine 이 존재하지 않습니다.");
                    return new AppException(ItemLineErrorCode.ITEM_LINE_NOT_FOUND);
                });

        // 1. 전표 번호 생성
        String documentNo = createDocumentNo();

        // 2. 요청 DTO 정보로 생산계획 생성
        ProductionPlans productionPlan = requestDto.toEntity(salesManager, productionManager, itemsLines, documentNo);

        // ! 긴급큐면 맨 앞에 삽입 + 전체 shift
        if (Boolean.TRUE.equals(requestDto.getIsEmergent())) {
            if (!user.isAdminRole()) throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);

            return insertEmergentPlan(productionPlan, line, processingEquips, user);
        }

        // 3. 동일한 라인에서 가장 최근에 생성된 생산계획 조회
        // 종료 시각이 현재 이후 중에 최근
        Optional<ProductionPlans> latestProdPlan = productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            line.getLineCode(), List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED), LocalDateTime.now(clock)
        );

        PlanStatus requestedStatus = user.isAdminRole() ? PlanStatus.CONFIRMED : PlanStatus.PENDING;

        productionPlan.updateStatus(requestedStatus);

        // 4. 시작 시간 계산
        LocalDateTime startTime = calculateStartTime(latestProdPlan, requestedStatus);

        // 5. 종료시간 설정
        LocalDateTime endTime = calculateEndTime(line.getId(), processingEquips, requestDto.getPlannedQty(), startTime);

        productionPlan.updateStartTime(startTime);
        productionPlan.updateEndTime(endTime);

        productionPlanRepository.save(productionPlan);

        // 6. 납기일 체크
        checkDueDate(productionPlan, endTime);


        return PlanScheduleChangeResponseDto.builder()
            .planId(productionPlan.getId())
            .planDocumentNo(productionPlan.getDocumentNo())
            .build();
    }

    private PlanScheduleChangeResponseDto insertEmergentPlan(
        ProductionPlans newPlan,
        Lines line,
        List<Equipments> equipments,
        Users requester
    ) {
        Long lineId = line.getId();

        // 0) 기존 계획 전체 조회
        List<ProductionPlans> dbPlans = findAllActivePlans(lineId);

        // Snapshot (Before)
        Map<Long, LocalDateTime> beforeStart = snapshotStart(dbPlans);
        Map<Long, LocalDateTime> beforeEnd   = snapshotEnd(dbPlans);

        // 1) 긴급계획 start/end 설정
        setEmergentPlanTime(newPlan, lineId, equipments);

        // 2) fullPlans 구성
        List<ProductionPlans> fullPlans = buildFullPlanList(newPlan, dbPlans);

        applyShift(fullPlans, newPlan, requester);

        applyCompact(fullPlans, requester);

        productionPlanRepository.saveAll(fullPlans);

        return buildScheduleChangeResponse(
            newPlan,
            beforeStart,
            beforeEnd,
            fullPlans
        );
    }

    private List<ProductionPlans> scheduleUpdatedPlan(
        ProductionPlans updatedPlan,
        Users requester,
        Long lineId
    ) {
        List<ProductionPlans> plans = buildPlansForUpdate(updatedPlan, lineId);

        applyShift(plans, updatedPlan, requester);

        applyCompact(plans, requester);

        productionPlanRepository.saveAll(plans);

        return plans;
    }

    /* ===========================================================
     *  생산계획 수정(Update)
     *  - compact 수행
     * ===========================================================
     */
    @Override
    @Transactional
    public PlanScheduleChangeResponseDto updateProductionPlan(
        UpdateProductionPlanRequestDto dto,
        Long planId,
        Users requester
    ) {
        // 시작시간이 현재시간 이후여야 한다.
        validateEndAndStartTime(dto.getStartTime());

        ProductionPlans productionPlan = findPlanById(planId);

        // 1. 상태 수정 가능 여부 도메인에서 검증
        validateUpdatable(productionPlan);

        // 2. 담당자 권한 검증
        validateRequestedStatusByRole(dto.getStatus(), requester);

        // 3. 관련 엔티티 조회 (optional -> safe wrapper)
        Users salesManager = findUserByEmpNo(dto.getSalesManagerNo());
        Users productionManager = findUserByEmpNo(dto.getProductionManagerNo());
        Lines line = dto.getLineCode() == null
            ? productionPlan.getItemLine().getLine()
            : lineRepository.findBylineCode(dto.getLineCode()).orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));

        Items item = findItem(dto.getItemCode());
        ItemsLines itemsLine = findValidatedItemLine(line, item);

        LocalDateTime previousStartTime = productionPlan.getStartTime();
        LocalDateTime previousEndTime = productionPlan.getEndTime();
        LocalDateTime newStartTime = calculateNewStart(dto, productionPlan);
        LocalDateTime newEndTime = calculateNewEnd(dto, productionPlan, newStartTime, line.getId());

        checkDueDate(productionPlan, newEndTime);

        // BEFORE SNAPSHOT
        List<ProductionPlans> beforePlans = findAllActivePlans(line.getId());
        Map<Long, LocalDateTime> beforeStart = snapshotStart(beforePlans);
        Map<Long, LocalDateTime> beforeEnd   = snapshotEnd(beforePlans);

        // 5. 최종 업데이트
        PlanStatus previousStatus = productionPlan.getStatus();
        productionPlan.update(dto, newStartTime, newEndTime, salesManager, productionManager, itemsLine);

        // 6. 전후 계획들 재배치 업데이트
        List<ProductionPlans> afterPlans = scheduleUpdatedPlan(productionPlan, requester, line.getId());

        planStatusNotificationService.notifyStatusChange(productionPlan, previousStatus);
        planStatusNotificationService.notifyScheduleChange(productionPlan, previousStartTime, previousEndTime);

        return buildScheduleChangeResponse(productionPlan, beforeStart, beforeEnd, afterPlans);
    }

    // PENDING 인 계획일때
    // - 자유롭게 땡기거나 미룸.
    // CONFIRMED 인 계획일때
    // - 이동되는 것과 겹친다. 담당자 -> 에러 / 관리자 -> 미룸
    // - 안 겹친다. 담당자/관리자 -> 그대로 둠
    private void applyShift(
        List<ProductionPlans> fullPlans,
        ProductionPlans updatedPlan,
        Users requester
    ) {
        boolean isManager = requester.isManagerRole();
        LocalDateTime baseStart = updatedPlan.getStartTime();
        LocalDateTime baseEnd   = updatedPlan.getEndTime();

        for (ProductionPlans plan : fullPlans) {

            // updatedPlan 이전 계획은 건드리지 않는다, updatedPlan 본인은 패스
            if (plan.getStartTime().isBefore(baseStart) || plan.equals(updatedPlan)) {
                continue;
            }

            Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

            // Manager가 CONFIRMED 를 밀어야 하면 금지
            if (isManager && plan.isConfirmed()) {
                if (baseEnd.isAfter(plan.getStartTime())) {   // 충돌 시 금지
                    log.debug("Manager cannot shift a confirmed plan (id={}).", plan.getId());
                    throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
                } else {
                    // 충돌 없으면 그냥 그 자리에 두고 다음으로 진행
                    baseEnd = plan.getEndTime();
                }
            }
            else {
                // 밀어야 하는 경우 → updatedPlan 뒤로 위치 조정
                LocalDateTime newStart = baseEnd;
                LocalDateTime newEnd   = newStart.plus(duration);

                plan.updateStartTime(newStart);
                plan.updateEndTime(newEnd);

                // dueDate 초과 체크
                checkDueDate(plan, newEnd);

                baseEnd = newEnd;
            }
        }
    }

    private void applyCompact(
        List<ProductionPlans> fullPlans,
        Users requester
    ) {
        boolean isAdmin = requester.isAdminRole();

        if (fullPlans == null || fullPlans.isEmpty()) return;

        // 1) 시간순으로 정렬 (updatedPlan 포함)
        fullPlans.sort(Comparator.comparing(ProductionPlans::getStartTime));

        // 2) 첫 계획(startTime)은 원래 유지
        LocalDateTime current = fullPlans.getFirst().getStartTime().isAfter(LocalDateTime.now(clock).plusMinutes(BASE_BUFFER_MINUTES))
            ? LocalDateTime.now(clock).plusMinutes(BASE_BUFFER_MINUTES)
            : fullPlans.getFirst().getStartTime();

        for (ProductionPlans plan : fullPlans) {
            Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

            // MANAGER → CONFIRMED 이동 금지
            if (!isAdmin && plan.isConfirmed()) {
                // CONFIRMED는 기존 시간 고정
                current = plan.getEndTime();
                continue;
            }

            // ADMIN → 모든 계획 이동 가능
            // MANAGER → PENDING은 idle 제거 목적 이동
            LocalDateTime newStart = current;
            LocalDateTime newEnd = newStart.plus(duration);

            plan.updateStartTime(newStart);
            plan.updateEndTime(newEnd);

            current = newEnd;
        }
    }

    private LocalDateTime calculateNewStart(
        UpdateProductionPlanRequestDto dto,
        ProductionPlans plan
    ) {
        // startTime 직접 변경하는 경우
        // 아니면 기존 startTime 유지
        return dto.getStartTime() != null ? dto.getStartTime() : plan.getStartTime();
    }

    private LocalDateTime calculateNewEnd(UpdateProductionPlanRequestDto dto,
        ProductionPlans plan,
        LocalDateTime newStart,
        Long lineId)
    {
        // 수량 변경이 없는 경우 기존 duration 그대로 유지
        if (dto.getPlannedQty() == null || dto.getPlannedQty().compareTo(plan.getPlannedQty()) == 0) {
            Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());
            return newStart.plus(duration);
        }

        // 수량 변경 → 종료시간 재계산 필요
        List<Equipments> equips = equipmentRepository.findAllByLineId(lineId);

        return calculateEndTime(lineId, equips, dto.getPlannedQty(), newStart);
    }

    private LocalDateTime calculateStartTime(Optional<ProductionPlans> latestProdPlan, PlanStatus requestedStatus) {
        // 조회된게 없을때, confirmed 면 10분뒤로 pending 이면 30분 뒤로 설정
        return latestProdPlan.map(
                productionPlans -> productionPlans.getEndTime().plusMinutes(30))
            .orElseGet(() -> requestedStatus.equals(PlanStatus.PENDING)
                ? LocalDateTime.now(clock).plusMinutes(30)
                : LocalDateTime.now(clock).plusMinutes(10)
            );
    }

    String createDocumentNo() {
        LocalDate today = LocalDate.now(clock);
        // 1️현재날짜 기준 prefix 생성
        String prefix = String.format("%04d/%02d/%02d", today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        // 기존 전표 번호 조회 + Lock
        List<String> productionPlansDocNos = productionPlanRepository.findByDocumentNoByPrefix(prefix);
        int nextSeq = 1;

        if (!productionPlansDocNos.isEmpty()) {
            String lastDocNo = productionPlansDocNos.getFirst();
            String lastSeqStr = lastDocNo.substring(lastDocNo.indexOf("-") + 1); // YYYY/MM/DD-X 중 X
            nextSeq = Integer.parseInt(lastSeqStr) + 1;
        }

        return prefix + String.format("-%d", nextSeq);
    }



    // 설비별 유효 PPM 계산
    private BigDecimal calculateEffectivePPM(Equipments equipment) {
        BigDecimal ppm = equipment.getEquipmentPpm();
        BigDecimal defectiveRate = BigDecimal.ZERO;

        if (equipment.getTotalCount() != null && equipment.getTotalCount().compareTo(BigDecimal.ZERO) > 0) {
            defectiveRate = equipment.getDefectiveCount()
                .divide(equipment.getTotalCount(), 4, RoundingMode.HALF_UP);
            defectiveRate = defectiveRate.min(BigDecimal.ONE); // 최대 1
            defectiveRate = defectiveRate.max(BigDecimal.ZERO); // 최소 0
        }

        return ppm.multiply(BigDecimal.ONE.subtract(defectiveRate));
    }

    private LocalDateTime calculateEndTime(Long lineId, List<Equipments> equipments, BigDecimal plannedQty, LocalDateTime startTime) {
        if (equipments == null || equipments.isEmpty()) {
            throw new AppException(LineErrorCode.NO_EQUIPMENT_FOUND);
        }

        // 라인은 Stage 의 순차 공정으로 구성되므로 Stage 별 유효 PPM 을 계산한 뒤
        // 가장 느린 Stage 를 병목으로 간주한다.
        AtomicInteger stageSequence = new AtomicInteger();
        Map<String, BigDecimal> stageEffectivePpm = equipments.stream()
            .collect(Collectors.groupingBy(
                equipment -> resolveStageKey(equipment, stageSequence.getAndIncrement()),
                Collectors.mapping(this::calculateEffectivePPM, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        int stageCount = stageEffectivePpm.size();

        BigDecimal totalEffectivePpm = stageEffectivePpm.values().stream()
            .filter(ppm -> ppm.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalEffectivePpm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(LineErrorCode.INVALID_EQUIPMENT_PPM);
        }

        BigDecimal effectiveQty = adjustQuantityForTrayProcess(plannedQty, stageCount);
        BigDecimal performanceMinutes = calculatePerformanceMinutes(lineId, effectiveQty);
        BigDecimal minutes;
        if (performanceMinutes != null) {
            minutes = performanceMinutes;
        } else {
            BigDecimal stageTraversalMinutes = calculateStageTraversalMinutes(stageEffectivePpm);
            // 2. 예상 소요 시간 (분 단위)
            minutes = effectiveQty.divide(totalEffectivePpm, 2, RoundingMode.CEILING)
                .add(stageTraversalMinutes);
        }

        // 3. 소요 시간 계산 (분 단위)
        long minutesToAdd = minutes
            .setScale(0, RoundingMode.CEILING)
            .longValue();

        // 4. 종료 시간 계산
        return withBuffer(startTime.plusMinutes(minutesToAdd));
    }

    private BigDecimal calculateStageTraversalMinutes(Map<String, BigDecimal> stageEffectivePpm) {
        return stageEffectivePpm.values().stream()
            .filter(ppm -> ppm.compareTo(BigDecimal.ZERO) > 0)
            .map(ppm -> TRAY_CAPACITY.divide(ppm, 2, RoundingMode.CEILING))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePerformanceMinutes(Long lineId, BigDecimal effectiveQty) {
        if (lineId == null || effectiveQty == null || effectiveQty.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        List<ProductionPerformances> performances = productionPerformanceRepository.findRecentByLineId(
            lineId,
            PageRequest.of(0, PERFORMANCE_SAMPLE_SIZE)
        );

        if (performances == null || performances.isEmpty()) {
            return null;
        }

        BigDecimal totalMinutes = BigDecimal.ZERO;
        BigDecimal totalOutputQty = BigDecimal.ZERO;

        for (ProductionPerformances performance : performances) {
            if (performance.getPerformanceQty() == null ||
                performance.getStartTime() == null ||
                performance.getEndTime() == null) {
                continue;
            }

            Duration duration = Duration.between(performance.getStartTime(), performance.getEndTime());
            long seconds = duration.getSeconds();
            if (seconds > 0) {
                BigDecimal minutes = BigDecimal.valueOf(seconds)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
                totalMinutes = totalMinutes.add(minutes);
                totalOutputQty = totalOutputQty.add(performance.getPerformanceQty());
            }
        }

        if (totalMinutes.compareTo(BigDecimal.ZERO) <= 0 || totalOutputQty.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal minutesPerUnit = totalMinutes.divide(totalOutputQty, 6, RoundingMode.HALF_UP);
        if (minutesPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return minutesPerUnit.multiply(effectiveQty)
            .setScale(2, RoundingMode.CEILING);
    }

    private BigDecimal adjustQuantityForTrayProcess(BigDecimal plannedQty, int stageCount) {
        BigDecimal sanitizedQty = plannedQty == null ? BigDecimal.ZERO : plannedQty.max(BigDecimal.ZERO);
        BigDecimal traysNeeded = sanitizedQty.divide(TRAY_CAPACITY, 0, RoundingMode.CEILING);
        if (traysNeeded.compareTo(BigDecimal.ONE) < 0) {
            traysNeeded = BigDecimal.ONE;
        }
        BigDecimal pipelineBufferTrays = stageCount > 1 ? BigDecimal.ONE : BigDecimal.ZERO;
        BigDecimal totalTrays = traysNeeded.add(pipelineBufferTrays);
        return totalTrays.multiply(TRAY_CAPACITY);
    }

    private String resolveStageKey(Equipments equipment, int fallbackIndex) {
        String equipmentType = equipment.getEquipmentType();
        if (equipmentType != null && !equipmentType.isBlank()) {
            return equipmentType.trim().toUpperCase(Locale.ROOT);
        }
        if (equipment.getId() != null) {
            return "ID_" + equipment.getId();
        }
        if (equipment.getEquipmentCode() != null && !equipment.getEquipmentCode().isBlank()) {
            return equipment.getEquipmentCode().trim().toUpperCase(Locale.ROOT);
        }
        return "IDX_" + fallbackIndex;
    }



    @Override
    @Transactional(readOnly = true)
    public GetProductionPlanDetailResponseDto getProductionPlan(Long planId) {
        ProductionPlans productionPlans = productionPlanRepository.findById(planId)
            .orElseThrow(() -> new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND));

        Factories factory = productionPlans.getItemLine().getLine().getFactory();

        Items item = productionPlans.getItemLine().getItem();

        return GetProductionPlanDetailResponseDto.fromEntity(productionPlans, factory, item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetProductionPlanListResponseDto> getProductionPlanList(
        SearchProductionPlanCommand command,
        Pageable pageable
    ) {
        // size 10 고정
        Pageable finalPageable = PageRequest.of(
            pageable.getPageNumber(),
            10,
            pageable.getSort()
        );

        Specification<ProductionPlans> spec = Specification.allOf(
            PlanSpecification.planStatusEquals(command.status()),
            PlanSpecification.planFactoryNameContains(command.factoryName()),
            PlanSpecification.planSalesManagerNameContains(command.salesManagerName()),
            PlanSpecification.planProductionManagerNameContains(command.productionManagerName()),
            PlanSpecification.planItemNameContains(command.itemName()),
            PlanSpecification.planDueDateBefore(command.dueDate()),
            PlanSpecification.planStartTimeAfter(command.startTime()),
            PlanSpecification.planEndTimeBefore(command.endTime())
        );


        return productionPlanRepository.findAll(spec, finalPageable)
            .map(GetProductionPlanListResponseDto::fromEntity);
    }




    @Override
    @Transactional(readOnly = true)
    public List<GetAllProductionPlanResponseDto> getAllProductionPlan(
        GetAllProductionPlanRequestDto requestDto
    ) {
        Specification<ProductionPlans> spec = Specification.allOf(
            PlanSpecification.planFactoryNameContains(requestDto.factoryName()),
            PlanSpecification.planLineNameContains(requestDto.lineName()),
            PlanSpecification.planItemNameContains(requestDto.itemName()),
            PlanSpecification.planItemCodeEquals(requestDto.itemCode()),
            PlanSpecification.planSalesManagerNameContains(requestDto.salesManagerName()),
            PlanSpecification.planProductionManagerNameContains(requestDto.productionManagerName()),
            PlanSpecification.planDueDateBefore(requestDto.dueDate()),
            PlanSpecification.planStartTimeAfter(requestDto.startTime()),
            PlanSpecification.planEndTimeBefore(requestDto.endTime())
        );

        List<ProductionPlans> result = productionPlanRepository.findAll(spec, Sort.by(Direction.DESC, "createdAt"));

        return result.stream().map(GetAllProductionPlanResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "productionPlanSchedule",
        key = "T(com.beyond.synclab.ctrlline.common.util.CacheKeyUtil).getProductionPlanScheduleKey(#requestDto)"
    )
    public List<GetProductionPlanScheduleResponseDto> getProductionPlanSchedule(
        GetProductionPlanScheduleRequestDto requestDto
    ) {
        Duration maxRange = Duration.ofDays(31); // 최대 31일 조회 허용
        if (Duration.between(requestDto.startTime(), requestDto.endTime()).compareTo(maxRange) > 0) {
            log.debug("조회 기간은 최대 30일을 초과할 수 없습니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_BAD_REQUEST);
        }

        Specification<ProductionPlans> spec = Specification.allOf(
            PlanSpecification.planFactoryCodeEquals(requestDto.factoryCode()),
            PlanSpecification.planLineCodeEquals(requestDto.lineCode()),
            PlanSpecification.planStatusNotEquals(PlanStatus.RETURNED), // 반려 조건 제외해서 조회
            PlanSpecification.planFactoryNameContains(requestDto.factoryName()),
            PlanSpecification.planLineNameContains(requestDto.lineName()),
            PlanSpecification.planStartTimeAfter(requestDto.startTime()),
            PlanSpecification.planEndTimeBefore(requestDto.endTime())
        );

        List<ProductionPlans> result = productionPlanRepository.findAll(spec, Sort.by(Direction.ASC, "startTime"));

        return result.stream().map(GetProductionPlanScheduleResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetProductionPlanEndTimeResponseDto getProductionPlanEndTime(
        GetProductionPlanEndTimeRequestDto requestDto)
    {
        Lines line = lineRepository.findBylineCode(requestDto.getLineCode())
            .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));

        List<Equipments> equipments =  equipmentRepository.findAllByLineId(line.getId());

        LocalDateTime endTime =
            calculateEndTime(line.getId(), equipments, requestDto.getPlannedQty(), requestDto.getStartTime());

        return GetProductionPlanEndTimeResponseDto.builder()
            .endTime(endTime)
            .build();
    }

    @Override
    @Transactional
    public UpdateProductionPlanStatusResponseDto updateProductionPlanStatus(
        UpdateProductionPlanStatusRequestDto requestDto
    ) {
        int success = productionPlanRepository.updateAllStatusById(requestDto.getPlanIds(), requestDto.getPlanStatus());

        if (success != requestDto.getPlanIds().size()) {
            throw new AppException(CommonErrorCode.UNEXPECTED_ERROR);
        }

        // 영속성 컨텍스트 초기화 후, 업데이트된 엔티티를 다시 조회
        List<ProductionPlans> updatedPlans = productionPlanRepository.findAllByIdIn(requestDto.getPlanIds());

        return UpdateProductionPlanStatusResponseDto.builder()
            .planIds(updatedPlans.stream().map(ProductionPlans::getId).toList())
            .planStatus(requestDto.getPlanStatus())
            .build();
    }

    @Override
    @Transactional
    public void deleteProductionPlan(Long planId, Users user) {
        ProductionPlans productionPlans = productionPlanRepository.findById(planId)
            .orElseThrow(() -> new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND));

        validateUpdatable(productionPlans);

        if (user.isManagerRole() && !productionPlans.getProductionManagerId().equals(user.getId())) {
            log.debug("MANAGER는 자신이 생산 담당자인 생산계획만 제거할 수 있습니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
        }

        productionPlanRepository.deleteById(planId);
    }

    @Override
    @Transactional
    public void deleteProductionPlans(DeleteProductionPlanRequestDto requestDto, Users user) {
        List<Long> planIds = requestDto.getPlanIds();

        // 삭제 대상 엔티티 조회
        List<ProductionPlans> plans = productionPlanRepository.findAllById(planIds);

        // 요청 ID와 실제 조회된 엔티티 개수 불일치 검증
        if (plans.size() != planIds.size()) {
            List<Long> foundIds = plans.stream()
                .map(ProductionPlans::getId)
                .toList();
            List<Long> notFoundIds = planIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

            log.debug("해당 ID들을 찾을 수 없습니다. {}", notFoundIds);
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND);
        }

        // 각 계획별 삭제 가능 여부 검증
        for (ProductionPlans plan : plans) {
            validateUpdatable(plan);

            // MANAGER 권한 검증
            if (user.isManagerRole() && !plan.getProductionManagerId().equals(user.getId())) {
                log.debug("생산계획 [{}]번은 요청자에 의해 제거할 수 없습니다.", plan.getId());
                throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
            }
        }

        // 일괄 삭제
        productionPlanRepository.deleteAll(plans);
    }


    @Override
    @Transactional(readOnly = true)
    public GetProductionPlanBoundaryResponseDto getPlanBoundaries(String factoryCode,
        String lineCode) {

        // 1. Factory & Line 검증
        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
            .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));

        Lines line = lineRepository.findBylineCode(lineCode)
            .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));

        if (!line.getFactoryId().equals(factory.getId())) {
            throw new AppException(FactoryErrorCode.FACTORY_NOT_FOUND);
        }

        // 2. 라인 전체 계획 조회
        List<ProductionPlans> plans =
            productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                line.getId(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
                );

        if (plans.isEmpty()) {
            return GetProductionPlanBoundaryResponseDto.builder()
                .earliestStartTime(null)
                .latestEndTime(null)
                .build();
        }

        // 3. 맨 앞 / 맨 뒤
        ProductionPlans first = plans.getFirst();
        ProductionPlans last = plans.getLast();

        return GetProductionPlanBoundaryResponseDto.builder()
            .earliestStartTime(first.getStartTime())
            .latestEndTime(last.getEndTime())
            .build();
    }
}
