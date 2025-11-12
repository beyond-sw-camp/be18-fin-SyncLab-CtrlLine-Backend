package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemCodeConflictException;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemNotFoundException;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.item.service.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@DisplayName("ItemService 단위 테스트 (CtrlLine 기준)")
class ItemServiceTest {

    private ItemRepository itemRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemRepository = Mockito.mock(ItemRepository.class);
        itemService = new ItemServiceImpl(itemRepository);
    }

    /* ========================================================
       🔹 품목 등록 성공
    ======================================================== */
    @Test
    @DisplayName("품목 등록 성공 - 신규 코드 저장 시 DB에 정상 저장된다.")
    void createItem_success() {
        // given
        CreateItemRequestDto request = CreateItemRequestDto.builder()
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Items savedItem = request.toEntity();

        given(itemRepository.existsByItemCode("ITEM-001")).willReturn(false);
        given(itemRepository.save(any(Items.class))).willReturn(savedItem);

        // when
        GetItemDetailResponseDto result = itemService.createItem(request);

        // then
        assertThat(result.getItemCode()).isEqualTo("ITEM-001");
        assertThat(result.getItemName()).isEqualTo("3P 차단기");
        then(itemRepository).should(times(1)).save(any(Items.class));
    }

    /* ========================================================
       🔹 품목 등록 실패 - 중복 코드
    ======================================================== */
    @Test
    @DisplayName("품목 등록 실패 - 중복된 코드로 저장 시 예외 발생")
    void createItem_conflict_fail() {
        // given
        CreateItemRequestDto request = CreateItemRequestDto.builder()
                .itemCode("ITEM-001")
                .itemName("중복 품목")
                .build();

        given(itemRepository.existsByItemCode("ITEM-001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(ItemCodeConflictException.class);
        then(itemRepository).should(never()).save(any());
    }

    /* ========================================================
       🔹 품목 상세 조회 성공
    ======================================================== */
    @Test
    @DisplayName("품목 상세 조회 성공 - ID 기준으로 조회 시 정상 반환된다.")
    void getItemDetail_success() {
        // given
        Items item = Items.builder()
                .id(1L)
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        given(itemRepository.findById(1L)).willReturn(Optional.of(item));

        // when
        GetItemDetailResponseDto result = itemService.getItemDetail(1L);

        // then
        assertThat(result.getItemName()).isEqualTo("퓨즈박스");
        assertThat(result.getItemStatus()).isEqualTo(ItemStatus.RAW_MATERIAL);
    }

    /* ========================================================
       🔹 품목 상세 조회 실패
    ======================================================== */
    @Test
    @DisplayName("품목 상세 조회 실패 - 존재하지 않는 ID로 조회 시 예외 발생")
    void getItemDetail_fail() {
        // given
        given(itemRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.getItemDetail(999L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    /* ========================================================
       🔹 품목 수정 성공
    ======================================================== */
    @Test
    @DisplayName("품목 수정 성공 - 변경된 필드만 갱신된다.")
    void updateItem_success() {
        // given
        Items item = Items.builder()
                .id(1L)
                .itemCode("ITEM-003")
                .itemName("MCCB 차단기")
                .itemSpecification("25A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.SEMI_FINISHED_PRODUCT)
                .isActive(true)
                .build();

        UpdateItemRequestDto request = UpdateItemRequestDto.builder()
                .itemName("MCCB 차단기(수정)")
                .itemSpecification("50A / 220V")
                .build();

        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
        given(itemRepository.existsByItemCode(anyString())).willReturn(false);

        // when
        var result = itemService.updateItem(1L, request);

        // then
        assertThat(result.getItemName()).isEqualTo("MCCB 차단기(수정)");
        assertThat(result.getItemSpecification()).isEqualTo("50A / 220V");
        then(itemRepository).should(times(1)).findById(1L);
    }

    /* ========================================================
       🔹 품목 수정 실패 - 중복 코드
    ======================================================== */
    @Test
    @DisplayName("품목 수정 실패 - 수정 중 코드 중복 시 예외 발생")
    void updateItem_conflict_fail() {
        // given
        Items item = Items.builder()
                .id(1L)
                .itemCode("ITEM-004")
                .itemName("ACB 차단기")
                .itemSpecification("100A / 380V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        UpdateItemRequestDto request = UpdateItemRequestDto.builder()
                .itemCode("ITEM-005")
                .build();

        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
        given(itemRepository.existsByItemCode("ITEM-005")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> itemService.updateItem(1L, request))
                .isInstanceOf(ItemCodeConflictException.class);
    }

    /* ========================================================
       🔹 품목 다건 활성/비활성 성공
    ======================================================== */
    @Test
    @DisplayName("품목 다건 활성/비활성 처리 성공")
    void updateItemAct_success() {
        // given
        Items item = Items.builder()
                .id(1L)
                .itemCode("ITEM-006")
                .itemName("퓨즈박스")
                .itemSpecification("5A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        UpdateItemActRequestDto request = UpdateItemActRequestDto.builder()
                .itemIds(List.of(1L))
                .isActive(false)
                .build();

        given(itemRepository.findById(1L)).willReturn(Optional.of(item));

        // when
        itemService.updateItemAct(request);

        // then
        assertThat(item.getIsActive()).isFalse();
        then(itemRepository).should(times(1)).findById(1L);
    }

    /* ========================================================
       🔹 품목 다건 활성/비활성 실패
    ======================================================== */
    @Test
    @DisplayName("품목 다건 활성/비활성 처리 실패 - itemIds가 비어있을 때 예외 발생")
    void updateItemAct_fail_noIds() {
        // given
        UpdateItemActRequestDto request = UpdateItemActRequestDto.builder()
                .itemIds(List.of())
                .isActive(false)
                .build();

        // when & then
        assertThatThrownBy(() -> itemService.updateItemAct(request))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
