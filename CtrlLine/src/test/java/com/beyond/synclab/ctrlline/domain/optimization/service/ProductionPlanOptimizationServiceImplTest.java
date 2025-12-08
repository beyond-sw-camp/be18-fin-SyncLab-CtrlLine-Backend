package com.beyond.synclab.ctrlline.domain.optimization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizationPreviewSnapshot;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.factory.StartTimeRangeFactory;
import com.beyond.synclab.ctrlline.domain.optimization.mapper.ProductionPlanAssignmentMapper;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionScheduleSolution;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ProductionPlanOptimizationServiceImplTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private ProductionPlanAssignmentMapper assignmentMapper;

    @Mock
    private StartTimeRangeFactory startTimeRangeFactory;

    @Mock
    private SolverManager<ProductionScheduleSolution, UUID> solverManager;

    @Mock
    private RedisTemplate<String, OptimizationPreviewSnapshot> redisTemplate;

    @InjectMocks
    private ProductionPlanOptimizationServiceImpl service;

    private Users mockUser() {
        return Users.builder()
            .id(1L)
            .name("관리자")
            .empNo("2025001")
            .role(Users.UserRole.ADMIN)
            .email("a@a.com")
            .password("encoded")
            .hiredDate(LocalDateTime.now().toLocalDate())
            .address("Seoul")
            .department("DEV")
            .position(Users.UserPosition.MANAGER)
            .status(Users.UserStatus.ACTIVE)
            .phoneNumber("01011112222")
            .build();
    }

    @Test
    @DisplayName("previewOptimization: Solver 결과를 Preview DTO로 반환한다")
    void previewOptimization_success() throws ExecutionException, InterruptedException {

        String lineCode = "LINE001";
        Users user = mockUser();

        ValueOperations<String, OptimizationPreviewSnapshot> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // ---- 1) Repository Mock ----
        ProductionPlans plan = mock(ProductionPlans.class);

        when(productionPlanRepository.findAllByLineCodeAndStatusInOrderByStartTimeAsc(
            eq(lineCode), anyList())
        ).thenReturn(List.of(plan));

        // ---- 2) Mapper Mock ----
        ProductionPlanAssignment assignment = ProductionPlanAssignment.builder()
            .planId(10L)
            .documentNo("DOC-1")
            .durationMinutes(60)
            .dueDateTime(LocalDateTime.of(2025,1,2,12,0))
            .confirmed(false)
            .locked(false)
            .originalStartTime(LocalDateTime.of(2025,1,1,9,0))
            .startTime(LocalDateTime.of(2025,1,1,9,0))
            .build();

        when(assignmentMapper.toAssignment(any(ProductionPlans.class), eq(user)))
            .thenReturn(assignment);

        // ---- 3) StartTimeRangeFactory Mock ----
        List<LocalDateTime> range = List.of(
            LocalDateTime.of(2025,1,1,9,0),
            LocalDateTime.of(2025,1,1,9,5)
        );
        when(startTimeRangeFactory.buildStartTimeRange(anyList()))
            .thenReturn(range);

        // ---- 4) Solver Mock ----
        SolverJob<ProductionScheduleSolution, UUID> job = mock(SolverJob.class);

        ProductionScheduleSolution solved = ProductionScheduleSolution.builder()
            .lineCode(lineCode)
            .assignments(List.of(assignment))
            .startTimeRange(range)
            .build();

        when(solverManager.solve(any(UUID.class), any()))
            .thenReturn(job);
        when(job.getFinalBestSolution())
            .thenReturn(solved);

        // ---- 5) Redis 저장 Mock ----
        // (성공 케이스 → 그냥 호출만 되는지 확인)

        // ---- 6) Execute ----
        OptimizePreviewResponseDto result = service.previewOptimization(lineCode, user);

        // ---- 7) 검증 ----
        assertThat(result.getLineCode()).isEqualTo(lineCode);
        assertThat(result.getPlans()).hasSize(1);
        assertThat(result.getPlans().getFirst().getPlanId()).isEqualTo(10L);

        verify(redisTemplate, times(1)).opsForValue();
    }


    @Test
    @DisplayName("commitOptimization: Snapshot 반영 후 DB 업데이트 수행한다")
    void commitOptimization_success() {

        String lineCode = "LINE001";
        String previewKey = "opt:plan:preview:aaa";

        // ---- 1) Redis ValueOperations mock 설정 ----
        ValueOperations<String, OptimizationPreviewSnapshot> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // ---- Snapshot Mock ----
        OptimizationPreviewSnapshot snapshot = OptimizationPreviewSnapshot.builder()
            .lineCode(lineCode)
            .assignments(List.of(
                OptimizationPreviewSnapshot.PlanSnapshot.builder()
                    .planId(10L)
                    .startTime(LocalDateTime.of(2025,1,1,9,30))
                    .endTime(LocalDateTime.of(2025,1,1,10,30))
                    .build()
            ))
            .build();

        when(valueOps.get(previewKey)).thenReturn(snapshot);


        // ---- DB Plan Mock ----
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(10L);
        when(plan.isUpdatable()).thenReturn(true);

        when(productionPlanRepository.findAllById(anyList()))
            .thenReturn(List.of(plan));

        // ---- Execute ----
        OptimizeCommitResponseDto result =
            service.commitOptimization(lineCode, previewKey, mockUser());

        // ---- Verify ----

        // startTime / endTime setter called
        verify(plan, times(1))
            .updateStartTime(LocalDateTime.of(2025,1,1,9,30));

        verify(plan, times(1))
            .updateEndTime(LocalDateTime.of(2025,1,1,10,30));

        // Redis key deleted
        verify(redisTemplate, times(1)).delete(previewKey);

        assertThat(result.getLineCode()).isEqualTo(lineCode);
        assertThat(result.getUpdatedCount()).isEqualTo(1);
    }



    @Test
    @DisplayName("commitOptimization: Snapshot 없으면 INVALID_REQUEST")
    void commitOptimization_snapshotMissing() {

        ValueOperations<String, OptimizationPreviewSnapshot> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        when(redisTemplate.opsForValue().get(anyString()))
            .thenReturn(null);

        Users u = mockUser();

        assertThatThrownBy(() ->
            service.commitOptimization("L1", "invalid-key", u)
        )
            .isInstanceOf(AppException.class)
            .hasMessageContaining(CommonErrorCode.INVALID_REQUEST.getMessage());
    }
}