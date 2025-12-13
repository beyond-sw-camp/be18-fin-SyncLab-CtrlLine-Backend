package com.beyond.synclab.ctrlline.domain.itemline;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.dto.request.ManageItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.errorcode.ItemLineErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.itemline.service.ItemLineServiceImpl;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemLineServiceImplTest {

    @Mock
    private ItemLineRepository itemLineRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private LineRepository lineRepository;

    @InjectMocks
    private ItemLineServiceImpl itemLineService;

    @Test
    @DisplayName("라인에 신규 생산 가능 품목을 등록하면 매핑 정보가 저장된다")
    @SuppressWarnings("unchecked")
    void createItemLine_shouldPersistNewMappings() {
        // given
        ManageItemLineRequestDto request = ManageItemLineRequestDto.builder()
                .itemCodes(List.of("ITEM-001", "ITEM-002"))
                .build();
        Lines line = buildLine(10L, "PL0001");
        Items item1 = buildItem(1L, "ITEM-001");
        Items item2 = buildItem(2L, "ITEM-002");

        when(lineRepository.findBylineCodeAndIsActiveTrue("PL0001")).thenReturn(Optional.of(line));
        when(itemRepository.findByItemCodeIn(List.of("ITEM-001", "ITEM-002")))
                .thenReturn(List.of(item1, item2));
        when(itemLineRepository.findByLine(line)).thenReturn(List.of());

        // when
        itemLineService.createItemLine("PL0001", request);

        // then
        ArgumentCaptor<List<ItemsLines>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemLineRepository).saveAll(captor.capture());
        List<ItemsLines> saved = captor.getValue();
        assertThat(saved)
                .hasSize(2)
                .extracting(ItemsLines::getItemId)
                .containsExactly(1L, 2L);
        assertThat(saved)
                .extracting(ItemsLines::getLineId)
                .containsOnly(10L);
    }

    @Test
    @DisplayName("이미 등록된 품목만 요청하면 예외가 발생한다")
    void createItemLine_whenAllItemsAlreadyExist_shouldThrow() {
        // given
        ManageItemLineRequestDto request = ManageItemLineRequestDto.builder()
                .itemCodes(List.of("ITEM-001"))
                .build();
        Lines line = buildLine(5L, "LINE-A");
        Items item = buildItem(100L, "ITEM-001");
        ItemsLines existing = ItemsLines.builder()
                .lineId(line.getId())
                .itemId(item.getId())
                .build();

        when(lineRepository.findBylineCodeAndIsActiveTrue("LINE-A")).thenReturn(Optional.of(line));
        when(itemRepository.findByItemCodeIn(List.of("ITEM-001"))).thenReturn(List.of(item));
        when(itemLineRepository.findByLine(line)).thenReturn(List.of(existing));

        // expect
        assertThatThrownBy(() -> itemLineService.createItemLine("LINE-A", request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ItemLineErrorCode.DUPLICATED_ITEM_LINE.getMessage());
    }

    @Test
    @DisplayName("라인의 생산 가능 품목 전체를 수정하면 기존 매핑이 대체된다")
    @SuppressWarnings("unchecked")
    void updateItemLine_shouldReplaceMappings() {
        // given
        ManageItemLineRequestDto request = ManageItemLineRequestDto.builder()
                .itemCodes(List.of("ITEM-003"))
                .build();
        Lines line = buildLine(20L, "LINE-B");
        Items newItem = buildItem(200L, "ITEM-003");
        ItemsLines existing = ItemsLines.builder()
                .lineId(line.getId())
                .itemId(99L)
                .build();

        when(lineRepository.findBylineCodeAndIsActiveTrue("LINE-B")).thenReturn(Optional.of(line));
        when(itemRepository.findByItemCodeIn(List.of("ITEM-003"))).thenReturn(List.of(newItem));
        when(itemLineRepository.findByLine(line)).thenReturn(List.of(existing));

        // when
        itemLineService.updateItemLine("LINE-B", request);

        // then
        ArgumentCaptor<List<ItemsLines>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemLineRepository).saveAll(captor.capture());
        List<ItemsLines> saved = captor.getValue();
        assertThat(saved)
                .hasSize(1)
                .first()
                .extracting(ItemsLines::getItemId, ItemsLines::getLineId)
                .containsExactly(200L, 20L);
    }

    private Lines buildLine(Long id, String lineCode) {
        return Lines.builder()
                .id(id)
                .factoryId(1L)
                .factory(Factories.builder().id(1L).build())
                .userId(1L)
                .lineCode(lineCode)
                .lineName("라인")
                .isActive(true)
                .build();
    }

    private Items buildItem(Long id, String itemCode) {
        return Items.builder()
                .id(id)
                .itemCode(itemCode)
                .itemName("품목")
                .itemSpecification("SPEC")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
    }
}
