package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Items 도메인 단위 테스트 (CtrlLine 기준)")
class ItemTest {

    @Test
    @DisplayName("Item 생성 성공 - 정상 입력 시 품목 객체가 올바르게 생성된다.")
    void createItem_success() {
        // given
        String itemCode = "ITEM-001";
        String itemName = "3P 차단기";
        String itemSpec = "32A / 220V";
        String itemUnit = "EA";
        ItemStatus status = ItemStatus.FINISHED_PRODUCT;

        // when
        Items item = Items.builder()
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
        assertThat(item.getItemName()).isEqualTo(itemName);
        assertThat(item.getItemSpecification()).isEqualTo(itemSpec);
        assertThat(item.getItemUnit()).isEqualTo(itemUnit);
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
        assertThat(item.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Item 수정 성공 - updateItem 호출 시 null이 아닌 필드만 변경된다.")
    void updateItem_success() {
        // given
        Items item = Items.builder()
                .itemCode("ITEM-002")
                .itemName("MCCB 차단기")
                .itemSpecification("25A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.SEMI_FINISHED_PRODUCT)
                .isActive(true)
                .build();

        UpdateItemRequestDto dto = UpdateItemRequestDto.builder()
                .itemName("MCCB 차단기(수정)")
                .itemSpecification("50A / 220V")
                .itemUnit("BOX")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .build();

        // when
        item.updateItem(dto);

        // then
        assertThat(item.getItemCode()).isEqualTo("ITEM-002"); // 변경되지 않음
        assertThat(item.getItemName()).isEqualTo("MCCB 차단기(수정)");
        assertThat(item.getItemSpecification()).isEqualTo("50A / 220V");
        assertThat(item.getItemUnit()).isEqualTo("BOX");
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.FINISHED_PRODUCT);
    }

    @Test
    @DisplayName("Item 다건 사용여부 변경 성공 - updateItemAct 호출 시 isActive 값이 변경된다.")
    void updateItemAct_success() {
        // given
        Items item = Items.builder()
                .itemCode("ITEM-005")
                .itemName("ACB 차단기")
                .itemSpecification("100A / 380V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        // when
        item.updateItemAct(false);

        // then
        assertThat(item.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Item 수정 시 null로 전달된 필드는 기존 값이 유지된다.")
    void updateItem_nullFields_ignored() {
        // given
        Items item = Items.builder()
                .itemCode("ITEM-006")
                .itemName("퓨즈박스")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        UpdateItemRequestDto dto = UpdateItemRequestDto.builder()
                .itemName(null)
                .itemSpecification(null)
                .build();

        // when
        item.updateItem(dto);

        // then
        assertThat(item.getItemName()).isEqualTo("퓨즈박스"); // 유지
        assertThat(item.getItemSpecification()).isEqualTo("10A / 110V"); // 유지
        assertThat(item.getIsActive()).isTrue(); // 유지
    }
}
