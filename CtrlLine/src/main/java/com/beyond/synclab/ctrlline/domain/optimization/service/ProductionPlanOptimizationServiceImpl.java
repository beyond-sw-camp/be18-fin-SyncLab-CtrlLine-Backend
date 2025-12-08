package com.beyond.synclab.ctrlline.domain.optimization.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizationPreviewSnapshot;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto.PreviewPlanDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
import com.beyond.synclab.ctrlline.domain.optimization.factory.StartTimeRangeFactory;
import com.beyond.synclab.ctrlline.domain.optimization.mapper.ProductionPlanAssignmentMapper;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionScheduleSolution;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanOptimizationServiceImpl implements ProductionPlanOptimizationService {

    private static final String PREVIEW_KEY_PREFIX = "opt:plan:preview:";

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionPlanAssignmentMapper assignmentMapper;
    private final StartTimeRangeFactory startTimeRangeFactory;

    private final SolverManager<ProductionScheduleSolution, UUID> solverManager;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public OptimizePreviewResponseDto previewOptimization(String lineCode, Users user) {

        // 1. 대상 라인 플랜 조회 (PENDING + CONFIRMED만 최적화 대상)
        List<ProductionPlans> plans = productionPlanRepository
            .findAllByLineCodeAndStatusInOrderByStartTimeAsc(
                lineCode,
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            );

        if (plans.isEmpty()) {
            throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND);
        }

        // 2. Entity → PlanningAssignment 매핑 (role 기반 locked 처리 포함)
        List<ProductionPlanAssignment> assignments = plans.stream()
            .map(plan -> assignmentMapper.toAssignment(plan, user))
            .toList();

        // 3. startTimeRange 생성 (5분 단위 등)
        List<java.time.LocalDateTime> startTimeRange =
            startTimeRangeFactory.buildStartTimeRange(assignments);

        // 4. Solver Problem 생성
        ProductionScheduleSolution problem = ProductionScheduleSolution.builder()
            .lineCode(lineCode)
            .assignments(assignments)
            .startTimeRange(startTimeRange)
            .build();

        // 5. Solver 실행 (동기 방식: getFinalBestSolution() 대기)
        UUID problemId = UUID.randomUUID();
        SolverJob<ProductionScheduleSolution, UUID> job =
            solverManager.solve(problemId, problem);

        ProductionScheduleSolution solved;
        try {
            solved = job.getFinalBestSolution();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
            log.error("OptaPlanner solving interrupted.", ie);
            throw new IllegalStateException("생산계획 최적화가 인터럽트되었습니다.", ie);

        } catch (ExecutionException ee) {
            log.error("OptaPlanner solving failed.", ee);
            throw new IllegalStateException("생산계획 최적화에 실패했습니다.", ee);
        }

        // 6. 결과 assignment 꺼내기
        List<ProductionPlanAssignment> solvedAssignments = solved.getAssignments();

        // 7. Preview 응답 DTO로 변환
        List<PreviewPlanDto> previewPlans = solvedAssignments.stream()
            .map(a -> PreviewPlanDto.builder()
                .planId(a.getPlanId())
                .documentNo(a.getDocumentNo())
                .originalStartTime(a.getOriginalStartTime())
                .optimizedStartTime(a.getStartTime())
                .optimizedEndTime(a.getEndTime())
                .dueDateTime(a.getDueDateTime())
                .confirmed(a.isConfirmed())
                .locked(a.isLocked())
                .build()
            ).toList();

        // 8. Redis에 Snapshot 저장
        OptimizationPreviewSnapshot snapshot = OptimizationPreviewSnapshot.builder()
            .lineCode(lineCode)
            .assignments(
                solvedAssignments.stream()
                    .map(a -> OptimizationPreviewSnapshot.PlanSnapshot.builder()
                        .planId(a.getPlanId())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .build()
                    ).toList()
            )
            .build();

        String previewKey = PREVIEW_KEY_PREFIX + problemId;
        redisTemplate.opsForValue().set(
            previewKey,
            snapshot,
            Duration.ofMinutes(10) // 10분만 유지
        );

        return OptimizePreviewResponseDto.builder()
            .previewKey(previewKey)
            .lineCode(lineCode)
            .plans(previewPlans)
            .build();
    }

    @Override
    @Transactional
    public OptimizeCommitResponseDto commitOptimization(String lineCode, String previewKey, Users user) {

        // 1. Redis에서 Snapshot 조회
        Object raw = redisTemplate.opsForValue().get(previewKey);
        if (raw == null) {
            throw new AppException(CommonErrorCode.INVALID_REQUEST);
        }

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        OptimizationPreviewSnapshot snapshot = mapper.convertValue(raw, OptimizationPreviewSnapshot.class);

        if (!Objects.equals(snapshot.getLineCode(), lineCode)) {
            log.debug("LineCode mismatch. request={}, snapshot={}", lineCode, snapshot.getLineCode());
            throw new AppException(CommonErrorCode.INVALID_REQUEST);
        }

        // 2. DB에서 대상 플랜들 조회
        List<Long> planIds = snapshot.getAssignments().stream()
            .map(OptimizationPreviewSnapshot.PlanSnapshot::getPlanId)
            .toList();

        Map<Long, ProductionPlans> planMap = productionPlanRepository.findAllById(planIds)
            .stream()
            .collect(Collectors.toMap(ProductionPlans::getId, p -> p));

        // 3. Snapshot 기준으로 start/end 반영
        for (OptimizationPreviewSnapshot.PlanSnapshot planSnap : snapshot.getAssignments()) {
            ProductionPlans plan = planMap.get(planSnap.getPlanId());
            if (plan == null) {
                log.debug("Plan not found during commit. id={}", planSnap.getPlanId());
                throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND);
            }

            // 기존 도메인 정책: PENDING/CONFIRMED 만 수정 가능
            if (!plan.isUpdatable()) {
                log.debug("Plan is not updatable. id={}, status={}", plan.getId(), plan.getStatus());
                throw new AppException(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN);
            }

            // Manager의 경우 Confirmed 이동 금지는 Constraint에서 이미 제어했으므로
            // 여기서는 단순 반영만 수행
            plan.updateStartTime(planSnap.getStartTime());
            plan.updateEndTime(planSnap.getEndTime());
        }

        // 4. 커밋 완료 후 Redis 키 삭제(선택)
        redisTemplate.delete(previewKey);

        return OptimizeCommitResponseDto.builder()
            .lineCode(lineCode)
            .updatedCount(planIds.size())
            .build();
    }
}
