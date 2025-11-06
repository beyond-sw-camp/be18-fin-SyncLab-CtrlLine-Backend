package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemAct;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import com.beyond.synclab.ctrlline.domain.item.service.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("ItemService 순수 단위 테스트 (CtrlLine)")
class ItemServiceTest {

    private ItemRepository itemRepository;  // mock
    private ItemService itemService;

    private Item baseItem;

    @BeforeEach
    void setup() {
        itemRepository = Mockito.mock(ItemRepository.class);
        itemService = new ItemServiceImpl(itemRepository);

        baseItem = Item.builder()
                .itemId(1L)
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .itemAct(ItemAct.ACTIVE)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("품목 등록 성공")
    void createItem_success() {
        // given
        Item newItem = Item.builder()
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .itemAct(ItemAct.ACTIVE)
                .isActive(true)
                .build();

        given(itemRepository.save(any(Item.class))).willReturn(newItem);

        // when
        Item saved = itemService.createItem(newItem);

        // then
        assertThat(saved.getItemCode()).isEqualTo("ITEM-002");
        assertThat(saved.getItemName()).isEqualTo("퓨즈박스");
    }

    @Test
    @DisplayName("품목 등록 실패 - 중복 코드 예외 발생")
    void createItem_duplicateCode_fail() {
        // given
        given(itemRepository.existsByItemCode("ITEM-001")).willReturn(true);

        Item duplicate = Item.builder()
                .itemCode("ITEM-001")
                .itemName("중복 품목")
                .build();

        // when & then
        assertThatThrownBy(() -> itemService.createItem(duplicate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 품목 코드");
    }

    @Test
    @DisplayName("품목 상세 조회 성공")
    void findItemByCode_success() {
        // given
        given(itemRepository.findByItemCode("ITEM-001")).willReturn(Optional.of(baseItem));

        // when
        Item found = itemService.findItemByCode("ITEM-001");

        // then
        assertThat(found.getItemName()).isEqualTo("3P 차단기");
    }

    @Test
    @DisplayName("품목 상세 조회 실패 - 존재하지 않는 코드")
    void findItemByCode_fail() {
        // given
        given(itemRepository.findByItemCode("ITEM-999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.findItemByCode("ITEM-999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item not found");
    }
}
