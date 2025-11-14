package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.controller.ItemController;
import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemListResponseDto;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = true)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ========================================================
       🔹 품목 등록 성공
    ======================================================== */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 역할은 품목을 등록할 수 있다.")
    void createItem_success() throws Exception {
        CreateItemRequestDto request = CreateItemRequestDto.builder()
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        GetItemDetailResponseDto response = GetItemDetailResponseDto.builder()
                .id(1L)
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        when(itemService.createItem(any(CreateItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itemCode").value("ITEM-001"))
                .andExpect(jsonPath("$.data.itemName").value("3P 차단기"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andDo(print());
    }

    /* ========================================================
       🔹 품목 목록 조회 성공 (PageResponse 적용)
    ======================================================== */
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER 역할은 품목 목록을 PageResponse 형태로 조회할 수 있다.")
    void getItemList_success() throws Exception {
        GetItemListResponseDto item1 = GetItemListResponseDto.builder()
                .id(1L)
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        GetItemListResponseDto item2 = GetItemListResponseDto.builder()
                .id(2L)
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        Page<GetItemListResponseDto> page = new PageImpl<>(List.of(item1, item2), pageable, 2);

        when(itemService.getItemList(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/items")
                        .param("itemCode", "ITEM")
                        .param("itemName", "차단기")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].itemCode").value("ITEM-001"))
                .andExpect(jsonPath("$.data.content[1].itemCode").value("ITEM-002"))
                .andExpect(jsonPath("$.data.pageInfo.currentPage").value(1))
                .andExpect(jsonPath("$.data.pageInfo.pageSize").value(10))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2))
                .andDo(print());
    }

    /* ========================================================
       🔹 품목 단건 조회 성공
    ======================================================== */
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER 역할은 품목 단건을 조회할 수 있다.")
    void getItemDetail_success() throws Exception {
        GetItemDetailResponseDto response = GetItemDetailResponseDto.builder()
                .id(1L)
                .itemCode("ITEM-003")
                .itemName("MCCB 차단기")
                .itemSpecification("50A / 220V")
                .itemUnit("BOX")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        when(itemService.getItemDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemName").value("MCCB 차단기"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andDo(print());
    }

    /* ========================================================
       🔹 품목 수정 성공
    ======================================================== */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 역할은 품목을 수정할 수 있다.")
    void updateItem_success() throws Exception {
        UpdateItemRequestDto request = UpdateItemRequestDto.builder()
                .itemName("퓨즈박스(수정)")
                .itemSpecification("20A / 110V")
                .build();

        GetItemDetailResponseDto response = GetItemDetailResponseDto.builder()
                .id(2L)
                .itemCode("ITEM-002")
                .itemName("퓨즈박스(수정)")
                .itemSpecification("20A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        when(itemService.updateItem(eq(2L), any(UpdateItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/items/{itemId}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemName").value("퓨즈박스(수정)"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andDo(print());
    }

    /* ========================================================
       🔹 품목 다건 사용여부 변경 성공
    ======================================================== */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 역할은 품목 다건의 사용여부를 변경할 수 있다.")
    void updateItemAct_success() throws Exception {
        UpdateItemActRequestDto request = UpdateItemActRequestDto.builder()
                .itemIds(List.of(1L, 2L))
                .isActive(false)
                .build();

        mockMvc.perform(patch("/api/v1/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false))                .andDo(print());
    }
}
