package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemAct;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ItemRepository JPA 테스트 (CtrlLine)")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Item 저장 및 조회 성공")
    void saveAndFindItem_success() {
        // given
        Item item = Item.builder()
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .itemAct(ItemAct.ACTIVE)
                .isActive(true)
                .build();

        // when
        Item saved = itemRepository.save(item);

        // then
        Item found = itemRepository.findByItemCode("ITEM-001").orElseThrow();
        assertThat(found.getItemName()).isEqualTo("3P 차단기");
        assertThat(found.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
    }

    @Test
    @DisplayName("Item 상태별 조회 성공")
    void findByItemStatus_success() {
        // given
        Item item1 = itemRepository.save(Item.builder()
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .itemAct(ItemAct.ACTIVE)
                .isActive(true)
                .build());

        Item item2 = itemRepository.save(Item.builder()
                .itemCode("ITEM-003")
                .itemName("누전차단기")
                .itemSpecification("25A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .itemAct(ItemAct.INACTIVE)
                .isActive(false)
                .build());

        // when
        List<Item> materials = itemRepository.findByItemStatus(ItemStatus.RAW_MATERIAL);

        // then
        assertThat(materials).hasSize(2);
    }
}
