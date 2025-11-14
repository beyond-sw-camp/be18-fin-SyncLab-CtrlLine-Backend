package com.beyond.synclab.ctrlline.domain.itemline;

import com.beyond.synclab.ctrlline.config.QuerydslConfig;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ItemLineRepositoryTest
 * <p>
 * CTRLLINE 표준: 교차 엔티티 매핑 및 품목 필터링 검증용 Repository 통합 테스트
 */
@DataJpaTest
@Import(QuerydslConfig.class)
class ItemLineRepositoryTest {

    @Autowired
    private ItemLineRepository itemLineRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private LineRepository lineRepository;

    @Test
    @DisplayName("isActive=true && itemStatus=FINISHED_PRODUCT 인 품목만 조회되어야 한다")
    void findActiveFinishedItemsByLine_shouldReturnOnlyActiveFinishedProducts() {
        // given
        Lines line = lineRepository.save(Lines.builder()
                .id(1L)
                .factoryId(1L)
                .lineCode("PL0001")
                .lineName("각형전지생산라인")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Items activeFinishedItem = itemRepository.save(Items.builder()
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A/220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build());

        Items inactiveItem = itemRepository.save(Items.builder()
                .itemCode("ITEM-002")
                .itemName("2P 차단기")
                .itemSpecification("20A/220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(false)
                .build());

        itemLineRepository.save(ItemsLines.builder()
                .line(line)
                .item(activeFinishedItem)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        itemLineRepository.save(ItemsLines.builder()
                .line(line)
                .item(inactiveItem)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // when
        List<Items> result = itemLineRepository.findActiveFinishedItemsByLine(line);

        // then
        assertThat(result)
                .isNotEmpty()
                .contains(activeFinishedItem)
                .doesNotContain(inactiveItem);
    }
}
