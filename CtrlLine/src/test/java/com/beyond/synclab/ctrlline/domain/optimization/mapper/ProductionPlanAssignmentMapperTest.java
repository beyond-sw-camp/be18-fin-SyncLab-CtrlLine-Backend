package com.beyond.synclab.ctrlline.domain.optimization.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionPlanAssignmentMapperTest {

    @InjectMocks
    private ProductionPlanAssignmentMapper mapper;

    private ItemsLines itemsLines;
    private Items items;
    @BeforeEach
    void setUp() {
        items = Items.builder()
            .itemName("아이템")
            .itemCode("ITEM001")
            .id(1L)
            .build();

        itemsLines = ItemsLines.builder()
            .item(items)
            .itemId(items.getId())
            .build();
    }
    @Test
    @DisplayName("ADMIN 은 confirmed 도 locked=false 이어야 한다")
    void adminDoesNotLockConfirmedPlans() {
        // given
        ProductionPlans plan = ProductionPlans.builder()
            .id(1L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .itemLine(itemsLines)
            .itemLineId(itemsLines.getId())
            .dueDate(LocalDate.of(2025, 1, 12))
            .status(PlanStatus.CONFIRMED)
            .build();

        Users admin = Users.builder()
            .role(Users.UserRole.ADMIN)
            .build();

        // when
        ProductionPlanAssignment result = mapper.toAssignment(plan, admin);

        // then
        assertThat(result.isLocked()).isFalse();
        assertThat(result.isConfirmed()).isTrue();
        assertThat(result.getOriginalStartTime()).isEqualTo(plan.getStartTime());
    }


    @Test
    @DisplayName("MANAGER 는 confirmed plan 을 locked=true 로 만든다")
    void managerLocksConfirmedPlans() {
        ProductionPlans plan = ProductionPlans.builder()
            .id(1L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .dueDate(LocalDate.of(2025, 1, 12))
            .itemLine(itemsLines)
            .itemLineId(itemsLines.getId())
            .status(PlanStatus.CONFIRMED)
            .build();

        Users manager = Users.builder()
            .role(Users.UserRole.MANAGER)
            .build();

        ProductionPlanAssignment result = mapper.toAssignment(plan, manager);

        assertThat(result.isLocked()).isTrue();
        assertThat(result.isConfirmed()).isTrue();
    }


    @Test
    @DisplayName("USER 는 어떤 plan 도 locked 되지 않는다")
    void userDoesNotLockAnyPlan() {

        ProductionPlans plan = ProductionPlans.builder()
            .id(1L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 12, 0))
            .dueDate(LocalDate.of(2025, 1, 12))
            .itemLine(itemsLines)
            .itemLineId(itemsLines.getId())
            .status(PlanStatus.PENDING)
            .build();

        Users user = Users.builder()
            .role(Users.UserRole.USER)
            .build();

        ProductionPlanAssignment result = mapper.toAssignment(plan, user);

        assertThat(result.isLocked()).isFalse();
    }


    @Test
    @DisplayName("durationMinutes = endTime - startTime 차이(분) 로 계산된다")
    void durationCalculation() {
        ProductionPlans plan = ProductionPlans.builder()
            .id(1L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 12, 30))
            .dueDate(LocalDate.of(2025, 1, 12))
            .itemLine(itemsLines)
            .itemLineId(itemsLines.getId())
            .build();

        Users admin = Users.builder()
            .role(Users.UserRole.ADMIN)
            .build();

        ProductionPlanAssignment result = mapper.toAssignment(plan, admin);

        assertThat(result.getDurationMinutes()).isEqualTo(150);
    }
}