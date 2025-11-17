package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemErrorCode;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ItemRepository itemRepository;
    private final Clock clock;

    @Override
    @Transactional
    public ProductionPlanResponseDto createProductionPlan(CreateProductionPlanRequestDto requestDto, Users user) {

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

        // 전표 번호 생성
        String documentNo = createDocumentNo();

        // 요청 DTO 정보로 생산계획 생성
        ProductionPlans productionPlan = requestDto.toEntity(salesManager, productionManager, line, documentNo);

        // 동일한 라인에서 가장 최근에 생성된 생산계획 조회
        // 종료 시각이 현재 이후 중에 최근
        Optional<ProductionPlans> latestProdPlan = productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            line.getLineCode(), List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED), LocalDate.now(clock)
        );

        ProductionPlans.PlanStatus requestedStatus;
        if (user.isUserRole()) {
            requestedStatus = PlanStatus.PENDING;
        } else {
            requestedStatus = PlanStatus.CONFIRMED;
        }

        LocalDateTime startTime;

        // 조회된게 없을때, confirmed 인거면 10분뒤로 pending 이면 30분 뒤로 설정
        if (latestProdPlan.isEmpty()) {
            if (requestedStatus.equals(PlanStatus.PENDING)) {
                startTime = LocalDateTime.now(clock).plusMinutes(30);
            } else {
                startTime = LocalDateTime.now(clock).plusMinutes(10);
            }
        } else {
            startTime = latestProdPlan.get().getEndTime().plusMinutes(30);
        }

        productionPlan.updateStartTime(startTime);

        productionPlanRepository.save(productionPlan);

        return ProductionPlanResponseDto.fromEntity(productionPlan, factory, item);
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

}
