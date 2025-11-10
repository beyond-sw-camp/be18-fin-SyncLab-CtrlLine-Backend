package com.beyond.synclab.ctrlline.domain.item;  // ✅ controller 폴더 없으므로 이렇게 수정

import com.beyond.synclab.ctrlline.domain.item.controller.ItemController;
import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ========================================================
       🔹 품목 등록 테스트
    ======================================================== */
    @Test
    @DisplayName("POST /api/v1/items - 품목 등록 성공")
    void createItem_success() throws Exception {
        Item mockItem = Item.builder()
                .id(1L)
                .itemCode("A20251105")
                .itemName("테스트품목")
                .itemSpecification("SPEC-001")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        Mockito.when(itemService.createItem(any(Item.class))).thenReturn(mockItem);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itemCode").value("A20251105"))
                .andExpect(jsonPath("$.data.itemName").value("테스트품목"))
                .andExpect(jsonPath("$.data.itemStatus").value("RAW_MATERIAL"));
    }

    /* ========================================================
       🔹 단건 조회 테스트
    ======================================================== */
    @Test
    @DisplayName("GET /api/v1/items/{itemId} - 품목 단건 조회 성공")
    void getItemById_success() throws Exception {
        Item mockItem = Item.builder()
                .id(1L)
                .itemCode("A20251105")
                .itemName("전류센서")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Mockito.when(itemService.getItemById(1L)).thenReturn(mockItem);

        mockMvc.perform(get("/api/v1/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCode").value("A20251105"))
                .andExpect(jsonPath("$.data.itemName").value("전류센서"))
                .andExpect(jsonPath("$.data.itemStatus").value("FINISHED_PRODUCT"));
    }

    /* ========================================================
       🔹 목록 조회 테스트
    ======================================================== */
    @Test
    @DisplayName("GET /api/v1/items - 품목 목록 조회 성공")
    void getItems_success() throws Exception {
        Item item1 = Item.builder()
                .id(1L)
                .itemCode("A001")
                .itemName("리드선")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.SUB_MATERIAL)
                .isActive(true)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .itemCode("A002")
                .itemName("조립모듈")
                .itemSpecification("20A / 220V")
                .itemUnit("SET")
                .itemStatus(ItemStatus.SEMI_FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Mockito.when(itemService.searchByIsActive(true)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].itemCode").value("A001"))
                .andExpect(jsonPath("$.data[1].itemCode").value("A002"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    /* ========================================================
       🔹 수정 테스트
    ======================================================== */
    @Test
    @DisplayName("PUT /api/v1/items/{itemId} - 품목 수정 성공")
    void updateItem_success() throws Exception {
        Item updated = Item.builder()
                .id(1L)
                .itemCode("A20251105")
                .itemName("테스트품목(수정)")
                .itemSpecification("SPEC-999")
                .itemUnit("BOX")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Mockito.when(itemService.updateItem(eq(1L), any(Item.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemName").value("테스트품목(수정)"))
                .andExpect(jsonPath("$.data.itemSpecification").value("SPEC-999"));
    }

    /* ========================================================
       🔹 활성/비활성 전환 테스트
    ======================================================== */
    @Test
    @DisplayName("PATCH /api/v1/items - 품목 비활성화 성공")
    void deactivateItem_success() throws Exception {
        String requestJson = """
            {
                "itemIds": [1],
                "isActive": false
            }
        """;

        mockMvc.perform(patch("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("품목 사용여부가 수정되었습니다."));

        Mockito.verify(itemService).deactivateItem(1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/items - 품목 활성화 성공")
    void activateItem_success() throws Exception {
        String requestJson = """
            {
                "itemIds": [1],
                "isActive": true
            }
        """;

        mockMvc.perform(patch("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("품목 사용여부가 수정되었습니다."));

        Mockito.verify(itemService).activateItem(1L);
    }
}