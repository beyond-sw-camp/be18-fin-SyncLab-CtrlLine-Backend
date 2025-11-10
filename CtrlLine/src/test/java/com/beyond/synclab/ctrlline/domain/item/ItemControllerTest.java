package com.beyond.synclab.ctrlline.domain.item;

import com.beyond.synclab.ctrlline.domain.item.controller.ItemController;
import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemCodeConflictException;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("test")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ========================================================
       🔹 품목 등록 성공
    ======================================================== */
    @Test
    @DisplayName("POST /api/v1/items - 품목 등록 성공 (201 Created)")
    void createItem_success() throws Exception {
        Item mockItem = Item.builder()
                .id(1L)
                .itemCode("ITEM-001")
                .itemName("3P 차단기")
                .itemSpecification("32A / 220V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Mockito.when(itemService.createItem(any(Item.class))).thenReturn(mockItem);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itemCode").value("ITEM-001"))
                .andExpect(jsonPath("$.data.itemName").value("3P 차단기"));
    }

    /* ========================================================
       🔹 품목 등록 실패 - 중복 코드 (409)
    ======================================================== */
    @Test
    @DisplayName("POST /api/v1/items - 품목 등록 실패 (ITEM_CODE_CONFLICT)")
    void createItem_conflict() throws Exception {
        Item dupItem = Item.builder()
                .itemCode("ITEM-001")
                .itemName("중복 품목")
                .build();

        Mockito.when(itemService.createItem(any(Item.class)))
                .thenThrow(new ItemCodeConflictException("이미 존재하는 품목코드입니다."));

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupItem)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ITEMCODE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 품목코드입니다."));
    }

    /* ========================================================
       🔹 품목 단건 조회 성공
    ======================================================== */
    @Test
    @DisplayName("GET /api/v1/items/{itemId} - 품목 조회 성공")
    void getItemById_success() throws Exception {
        Item mockItem = Item.builder()
                .id(1L)
                .itemCode("ITEM-002")
                .itemName("퓨즈박스")
                .itemSpecification("10A / 110V")
                .itemUnit("EA")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .isActive(true)
                .build();

        Mockito.when(itemService.getItemById(1L)).thenReturn(mockItem);

        mockMvc.perform(get("/api/v1/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCode").value("ITEM-002"))
                .andExpect(jsonPath("$.data.itemName").value("퓨즈박스"));
    }

    /* ========================================================
       🔹 품목 수정 성공 (PATCH)
    ======================================================== */
    @Test
    @DisplayName("PATCH /api/v1/items/{itemId} - 품목 수정 성공")
    void updateItem_success() throws Exception {
        Item updated = Item.builder()
                .id(1L)
                .itemCode("ITEM-003")
                .itemName("MCCB 차단기(수정)")
                .itemSpecification("50A / 220V")
                .itemUnit("BOX")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        Mockito.when(itemService.updateItem(eq(1L), any(Item.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemName").value("MCCB 차단기(수정)"));
    }

    /* ========================================================
   🔹 품목 활성/비활성 성공
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
                .andExpect(jsonPath("$.data").isEmpty());

        Mockito.verify(itemService).deactivateItem(1L);
    }
}
