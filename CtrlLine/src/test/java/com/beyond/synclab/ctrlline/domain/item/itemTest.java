package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item 도메인 단위 테스트 (CtrlLine 기준)")
class ItemTest {

    @Test
    @DisplayName("Item 생성 성공 - 정상 입력 시 품목 객체 생성")
    void createItem_success() {
        // given
        String itemCode = "ITEM-001";
        String itemName = "3P 차단기";
        String itemSpec = "32A / 220V";
        String itemUnit = "EA";
        ItemStatus status = ItemStatus.FINISHED_PRODUCT;

        // when
        Item item = Item.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .itemSpecification(itemSpec)
                .itemUnit(itemUnit)
                .itemStatus(status)
                .isActive(true)
                .build();

        // then
        assertThat(item).isNotNull();
        assertThat(item.getItemCode()).isEqualTo(itemCode);
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
        assertThat(item.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Item 수정 성공 - updateItem 호출 시 필드값이 변경된다.")
    void updateItem_success() {
        // given
        Item item = Item.builder()
                .itemCode("ITEM-002")
                .itemName("MCCB 차단기")
                .itemSpecification("25A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.SEMI_FINISHED_PRODUCT)
                .isActive(true)
                .build();

        // when
        item.updateItem("ITEM-002", // ✅ itemCode 추가
                "MCCB 차단기(수정)",
                "50A / 220V",
                "BOX",
                ItemStatus.FINISHED_PRODUCT);

        // then
        assertThat(item.getItemName()).isEqualTo("MCCB 차단기(수정)");
        assertThat(item.getItemSpecification()).isEqualTo("50A / 220V");
        assertThat(item.getItemUnit()).isEqualTo("BOX");
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
    }

    @Test
    @DisplayName("Item 비활성화 성공 - deactivate 호출 시 isActive=false로 변경된다.")
    void deactivateItem_success() {
        // given
        Item item = Item.builder()
                .itemCode("ITEM-003")
                .itemName("누전차단기")
                .itemSpecification("20A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        // when
        item.deactivate();

        // then
        assertThat(item.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Item 재활성화 성공 - activate 호출 시 isActive=true로 변경된다.")
    void activateItem_success() {
        // given
        Item item = Item.builder()
                .itemCode("ITEM-004")
                .itemName("퓨즈박스")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(false)
                .build();

        // when
        item.activate();

        // then
        assertThat(item.getIsActive()).isTrue();
    }
}
