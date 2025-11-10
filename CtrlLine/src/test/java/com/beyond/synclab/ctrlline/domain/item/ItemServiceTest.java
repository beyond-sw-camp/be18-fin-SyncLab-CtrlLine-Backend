package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemCodeConflictException;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemNotFoundException;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import com.beyond.synclab.ctrlline.domain.item.service.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("ItemService 순수 단위 테스트 (CtrlLine 기준)")
class ItemServiceTest {

    private ItemRepository itemRepository;
    private ItemService itemService;
    private Item baseItem;

    @BeforeEach
    void setup() {
        itemRepository = Mockito.mock(ItemRepository.class);
        itemService = new ItemServiceImpl(itemRepository);

        baseItem = Item.builder()
                .id(1L)
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("품목 등록 성공 - 정상 입력 시 저장됨")
    void createItem_success() {
        // given
        Item newItem = Item.builder()
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemSpecification("SPEC-001")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        given(itemRepository.existsByItemCode("ITEM-002")).willReturn(false);
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
                .isInstanceOf(ItemCodeConflictException.class)
                .hasMessageContaining("이미 존재하는 품목코드입니다.");
    }

    @Test
    @DisplayName("품목 단건 조회 성공 - ID 기준 조회")
    void getItemById_success() {
        // given
        given(itemRepository.findById(1L)).willReturn(Optional.of(baseItem));

        // when
        Item found = itemService.getItemById(1L);

        // then
        assertThat(found.getItemName()).isEqualTo("3P 차단기");
        assertThat(found.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
    }

    @Test
    @DisplayName("품목 단건 조회 실패 - 존재하지 않는 ID")
    void getItemById_fail() {
        // given
        given(itemRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.getItemById(999L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("해당 품목을 찾을 수 없습니다.");
    }
}
