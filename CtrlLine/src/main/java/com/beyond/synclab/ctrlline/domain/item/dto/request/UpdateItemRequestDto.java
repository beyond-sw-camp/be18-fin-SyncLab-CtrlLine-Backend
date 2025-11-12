package com.beyond.synclab.ctrlline.domain.item.dto.request;

import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequestDto {

    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private String itemUnit;

    @NotNull(message = "품목 상태는 필수 입력값입니다.")
    private ItemStatus itemStatus;

    private Boolean isActive;
}
