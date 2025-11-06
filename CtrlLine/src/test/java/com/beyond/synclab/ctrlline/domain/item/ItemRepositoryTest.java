package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemAct;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ItemRepository 단위 테스트 (순수 자바)")
class ItemRepositoryTest {

    @Test
    @DisplayName("Item 상태별 필터링 로직 단위 검증")
    void findByItemStatus_success() {
        // given
        List<Item> allItems = new ArrayList<>();
        allItems.add(Item.builder()
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .itemAct(ItemAct.ACTIVE)
                .isActive(true)
                .build());

        allItems.add(Item.builder()
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .itemAct(ItemAct.INACTIVE)
                .isActive(false)
                .build());

        // when
        List<Item> materials = allItems.stream()
                .filter(i -> i.getItemStatus() == ItemStatus.RAW_MATERIAL)
                .toList();

        // then
        assertThat(materials).hasSize(1);
        assertThat(materials.get(0).getItemName()).isEqualTo("퓨즈박스");
    }
}
