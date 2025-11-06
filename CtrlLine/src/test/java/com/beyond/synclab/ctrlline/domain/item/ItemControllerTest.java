package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.controller.ItemController;
import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ItemController 순수 단위 테스트 (CtrlLine)")
class ItemControllerTest {

    private ItemService itemService;
    private ItemController itemController;

    private Item baseItem;

    @BeforeEach
    void setup() {
        // ✅ Mock 생성 (Spring Bean 아님)
        itemService = Mockito.mock(ItemService.class);

        // ✅ Controller 직접 new로 생성
        itemController = new ItemController(itemService);

        // ✅ 테스트용 기본 데이터
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
        given(itemService.createItem(Mockito.any(Item.class))).willReturn(baseItem);

        // when
        ResponseEntity<Item> response = itemController.createItem(baseItem);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getItemName()).isEqualTo("3P 차단기");
        verify(itemService).createItem(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("품목 수정 성공")
    void updateItem_success() {
        // given
        Item updated = Item.builder()
                .itemName("3P 차단기 (수정)")
                .itemSpecification("50A / 220V")
                .itemUnit("BOX")
                .itemStatus(ItemStatus.SEMI_FINISHED_PRODUCT)
                .build();
        given(itemService.updateItem(Mockito.eq(1L), Mockito.any(Item.class))).willReturn(updated);

        // when
        ResponseEntity<Item> response = itemController.updateItem(1L, updated);

        // then
        assertThat(response.getBody().getItemName()).isEqualTo("3P 차단기 (수정)");
        verify(itemService).updateItem(1L, updated);
    }

    @Test
    @DisplayName("품목 상세 조회 성공")
    void getItemDetail_success() {
        // given
        given(itemService.findItemByCode("ITEM-001")).willReturn(baseItem);

        // when
        ResponseEntity<Item> response = itemController.getItemDetail("ITEM-001");

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getItemCode()).isEqualTo("ITEM-001");
        verify(itemService).findItemByCode("ITEM-001");
    }

    @Test
    @DisplayName("품목 목록 조회 성공")
    void getItemList_success() {
        // given
        List<Item> items = List.of(
                baseItem,
                Item.builder().itemCode("ITEM-002").itemName("퓨즈박스").build()
        );
        given(itemService.findItems()).willReturn(items);

        // when
        ResponseEntity<List<Item>> response = itemController.getItemList();

        // then
        assertThat(response.getBody()).hasSize(2);
        verify(itemService).findItems();
    }

    @Test
    @DisplayName("품목 상태별 조회 성공")
    void getItemsByStatus_success() {
        // given
        List<Item> finishedItems = List.of(baseItem);
        given(itemService.findItemsByStatus(ItemStatus.FINISHED_PRODUCT)).willReturn(finishedItems);

        // when
        ResponseEntity<List<Item>> response = itemController.getItemsByStatus(ItemStatus.FINISHED_PRODUCT);

        // then
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
        verify(itemService).findItemsByStatus(ItemStatus.FINISHED_PRODUCT);
    }

    @Test
    @DisplayName("품목 사용여부별 조회 성공")
    void getItemsByAct_success() {
        // given
        List<Item> activeItems = List.of(baseItem);
        given(itemService.findItemsByAct(ItemAct.ACTIVE)).willReturn(activeItems);

        // when
        ResponseEntity<List<Item>> response = itemController.getItemsByAct(ItemAct.ACTIVE);

        // then
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getItemAct()).isEqualTo(ItemAct.ACTIVE);
        verify(itemService).findItemsByAct(ItemAct.ACTIVE);
    }
}
