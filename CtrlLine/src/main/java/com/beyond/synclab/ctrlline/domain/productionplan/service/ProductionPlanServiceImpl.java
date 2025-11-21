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
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;
    private final FactoryRepository factoryRepository;
    private final ItemLineRepository itemLineRepository;
    private final ItemRepository itemRepository;
    private final EquipmentRepository equipmentRepository;
    private final Clock clock;

    @Override
    @Transactional
    public GetProductionPlanResponseDto createProductionPlan(CreateProductionPlanRequestDto requestDto, Users user) {

        Users salesManager = userRepository.findByEmpNo(requestDto.getSalesManagerNo())
                .orElseThrow(() -> {
                    log.debug("SalesManager 가 존재하지 않습니다.");
                    return new AppException(UserErrorCode.USER_NOT_FOUND);
                });

        Users productionManager = userRepository.findByEmpNo(requestDto.getProductionManagerNo())
                .orElseThrow(() -> {
                    log.debug("ProductionManager 가 존재하지 않습니다.");
                    return new AppException(UserErrorCode.USER_NOT_FOUND);
                });

        Lines line = lineRepository.findBylineCode(requestDto.getLineCode())
                .orElseThrow(() -> {
                    log.debug("Line 이 존재하지 않습니다.");
                    return new AppException(LineErrorCode.LINE_NOT_FOUND);
                });

        Factories factory = factoryRepository.findByFactoryCode(requestDto.getFactoryCode())
                .orElseThrow(() -> {
                    log.debug("Factory 가 존재하지 않습니다.");
                    return new AppException(FactoryErrorCode.FACTORY_NOT_FOUND);
                });


        Items item = itemRepository.findByItemCode(requestDto.getItemCode())
            .orElseThrow(() -> {
                log.debug("Item 이 존재하지 않습니다.");
                return new AppException(ItemErrorCode.ITEM_NOT_FOUND);
            });

        ItemsLines itemsLines = itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())
                .orElseThrow(() -> {
                    log.debug("ItemLine 이 존재하지 않습니다.");
                    return new AppException(ItemLineErrorCode.ITEM_LINE_NOT_FOUND);
                });

        // 1. 전표 번호 생성
        String documentNo = createDocumentNo();

        // 2. 요청 DTO 정보로 생산계획 생성
        ProductionPlans productionPlan = requestDto.toEntity(salesManager, productionManager, itemsLines, documentNo);

        // 3. 동일한 라인에서 가장 최근에 생성된 생산계획 조회
        // 종료 시각이 현재 이후 중에 최근
        Optional<ProductionPlans> latestProdPlan = productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            line.getLineCode(), List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED), LocalDateTime.now(clock)
        );

        PlanStatus requestedStatus;
        if (user.isUserRole()) {
            requestedStatus = PlanStatus.PENDING;
        } else {
            requestedStatus = PlanStatus.CONFIRMED;
        }

        productionPlan.updateStatus(requestedStatus);

        // 4. 시작 시간 계산
        LocalDateTime startTime = calculateStartTime(latestProdPlan, requestedStatus);
        log.debug("예상 시작 시간 : {}", startTime);
        productionPlan.updateStartTime(startTime);

        // 5. 종료시간 설정
        List<Equipments> processingEquips = equipmentRepository.findAllByLineId(line.getId());

        LocalDateTime endTime = calculateEndTime(processingEquips, requestDto.getPlannedQty(), startTime);
        log.debug("예상 종료 시간 : {}", endTime);

        productionPlan.updateEndTime(endTime);

        productionPlanRepository.save(productionPlan);

        return GetProductionPlanResponseDto.fromEntity(productionPlan, factory, item);
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

    private LocalDateTime calculateEndTime(List<Equipments> equipments, BigDecimal plannedQty, LocalDateTime startTime) {
        if (equipments == null || equipments.isEmpty()) {
            throw new AppException(LineErrorCode.NO_EQUIPMENT_FOUND);
        }

        // 1. 라인 전체 유효 PPM 계산
        BigDecimal totalEffectivePPM = equipments.stream()
            .map(this::calculateEffectivePPM)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalEffectivePPM.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(LineErrorCode.INVALID_EQUIPMENT_PPM);
        }

        // 2. 예상 소요 시간 (분 단위)
        BigDecimal minutes = plannedQty.divide(totalEffectivePPM, 2, RoundingMode.CEILING);

        // 3. 소요 시간 계산 (분 단위)
        long minutesToAdd = minutes
            .setScale(0, RoundingMode.CEILING)
            .longValue();

        // 4. 종료 시간 계산
        return startTime.plusMinutes(minutesToAdd);
    }

    private LocalDateTime calculateStartTime(Optional<ProductionPlans> latestProdPlan, PlanStatus requestedStatus) {
        LocalDateTime startTime;
        // 조회된게 없을때, confirmed 면 10분뒤로 pending 이면 30분 뒤로 설정
        if (latestProdPlan.isEmpty()) {
            if (requestedStatus.equals(PlanStatus.PENDING)) {
                startTime = LocalDateTime.now(clock).plusMinutes(30);
            } else {
                startTime = LocalDateTime.now(clock).plusMinutes(10);
            }
        } else {
            startTime = latestProdPlan.get().getEndTime().plusMinutes(30);
        }
        return startTime;
    }

    String createDocumentNo() {
        LocalDate today = LocalDate.now(clock);
        // 1️⃣ 현재날짜 기준 prefix 생성
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

    private void validateRequestedStatusByRole(PlanStatus newStatus, Users requester) {
        if (!requester.isManagerRole()) return;

        // 관리자일때 수정 요청 확인
        if (!(newStatus == PlanStatus.PENDING || newStatus == PlanStatus.CONFIRMED)) {
            log.debug("담당자는 PENDING, CONFIRMED 요청으로만 수정가능합니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
        }
    }

    private Users findUserByEmpNo(String empNo) {
        if (empNo == null) return null;
        return userRepository.findByEmpNo(empNo)
            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

    private Lines findLine(String lineCode) {
        if (lineCode == null) return null;
        return lineRepository.findBylineCode(lineCode)
            .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));
    }

    private Items findItem(String itemCode) {
        if (itemCode == null) return null;
        return itemRepository.findByItemCode(itemCode)
            .orElseThrow(() -> new AppException(ItemErrorCode.ITEM_NOT_FOUND));
    }

    private Factories findFactory(String factoryCode) {
        if (factoryCode == null) return null;
        return factoryRepository.findByFactoryCode(factoryCode)
            .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));
    }

    private ItemsLines findValidatedItemLine(Lines line, Items item) {
        if (line == null || item == null) return null;

        Optional<ItemsLines> itemsLinesOptional = itemLineRepository
            .findByLineIdAndItemId(line.getId(), item.getId());


        if (itemsLinesOptional.isEmpty()) {
            throw new AppException(ItemLineErrorCode.ITEM_LINE_NOT_FOUND);
        }

        return itemsLinesOptional.get();
    }


    private void validateFactoryLine(Factories factory, Lines line) {
        if (factory == null || line == null) return;

        if (!line.getFactoryId().equals(factory.getId())) {
            throw new AppException(LineErrorCode.LINE_NOT_FOUND);
        }
    }

    private void shiftAfterPlansIfNeeded(
        LocalDateTime newStartTime,
        LocalDateTime newEndTime,
        ProductionPlans newPlan,
        Users requester
    ) {
        List<ProductionPlans> afterPlans =
            new ArrayList<>(
            productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(newStartTime, List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
                );

        afterPlans.forEach(p -> log.debug("id: {}, startTime : {}, endTime : {}",p.getId(),  p.getStartTime(), p.getEndTime()));

        // 현재 수정 중인 계획을 리스트에서 제거
        afterPlans.removeIf(newPlan::equals);

        if (afterPlans.isEmpty()) return;


        // Δ 시간 설정, 관리자면 30분
        Duration delta = Duration.ofMinutes(30L);

        // 이전 계획의 종료시각 기준으로 새 시작 시간을 계속 계산
        LocalDateTime lastEndTime = newEndTime;

        boolean isRequestManager = requester.isManagerRole();

        for (ProductionPlans plan :  afterPlans) {
            Duration originalDuration = Duration.between(plan.getStartTime(), plan.getEndTime());

            LocalDateTime candidateStart;
            LocalDateTime candidateEnd;

            // 겹치면 lastEndTime 기준으로 시작 시간 이동
            if (!plan.getStartTime().isAfter(lastEndTime)) {
                candidateStart = lastEndTime.plus(delta);
                candidateEnd = candidateStart.plus(originalDuration);

                if (isRequestManager && plan.getStatus().equals(PlanStatus.CONFIRMED)) {
                    log.debug("이후에 변경되는 생산 계획 중 confirmed가 있습니다. 담당자 권한으로 수정 불가능합니다.");
                    throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
                }
            }
            // 겹치지 않더라도 "간격이 delta 보다 작은 경우" -> 간격 보정
            else {
                Duration gap = Duration.between(lastEndTime, plan.getStartTime());

                if (gap.compareTo(delta) < 0) {
                    candidateStart = lastEndTime.plus(delta);
                    candidateEnd = candidateStart.plus(originalDuration);
                } else {
                    // 완전히 여유 있는 경우 → 이동 없음, endTime 기준만 업데이트
                    candidateStart = plan.getStartTime();
                    candidateEnd = plan.getEndTime();
                }
            }

            // 실제 업데이트
            plan.updateStartTime(candidateStart);
            plan.updateEndTime(candidateEnd);

            lastEndTime = candidateEnd;
        }

        productionPlanRepository.saveAll(afterPlans);
    }

    @Override
    @Transactional
    public GetProductionPlanResponseDto updateProductionPlan(
        UpdateProductionPlanRequestDto dto, Long planId, Users requester
    ) {
        // 시작시간이 끝나는 시간보다 길진 않은지 확인
        validateEndAndStartTime(dto.getStartTime(), dto.getEndTime());

        ProductionPlans productionPlan = productionPlanRepository.findById(planId)
            .orElseThrow(() -> new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND));

        // 1. 상태 수정 가능 여부 도메인에서 검증
        if (!productionPlan.isUpdatable()) {
            log.debug("수정하려는 생산계획이 PENDING 또는 CONFIRMED 일때만, 수정이 가능합니다.");
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_BAD_REQUEST);
        }

        // 2. 담당자 권한 검증
        validateRequestedStatusByRole(dto.getStatus(), requester);

        // 3. 관련 엔티티 조회 (optional → safe wrapper)
        Users salesManager = findUserByEmpNo(dto.getSalesManagerNo());
        Users productionManager = findUserByEmpNo(dto.getProductionManagerNo());
        Lines line = findLine(dto.getLineCode());
        Items item = findItem(dto.getItemCode());
        Factories factory = findFactory(dto.getFactoryCode());

        // 4. 라인-공장-아이템 조합 검증
        ItemsLines itemsLine = findValidatedItemLine(line, item);
        validateFactoryLine(factory, line);

        // 주어지는 종료 시각 이후에 존재하는 생산계획 중 시작시간이 그 이후인 것들 모두 조회
        // 5. 이후 계획들 이동 처리
        shiftAfterPlansIfNeeded(dto.getStartTime(), dto.getEndTime(), productionPlan, requester);

        // 6. 최종 업데이트 (도메인 주도)
        productionPlan.update(dto, salesManager, productionManager, itemsLine);

        return GetProductionPlanResponseDto.fromEntity(productionPlan, factory, item);
    }

    private void validateEndAndStartTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return;

        if (endTime.isBefore(startTime)) {
            log.debug("시작시간이 종료시간 이후입니다.");
            throw new AppException(CommonErrorCode.INVALID_REQUEST);
        }
    }

}
