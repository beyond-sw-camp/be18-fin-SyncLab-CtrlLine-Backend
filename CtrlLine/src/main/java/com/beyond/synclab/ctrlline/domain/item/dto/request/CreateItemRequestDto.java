package com.beyond.synclab.ctrlline.domain.item.dto.request;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateItemRequestDto {

    @NotBlank(message = "품목코드는 필수 입력값입니다.")
    private String itemCode;

    @NotBlank(message = "품목명은 필수 입력값입니다.")
    private String itemName;

    private String itemSpecification;
    private String itemUnit;

    @NotNull(message = "품목 상태는 필수입니다.")
    private ItemStatus itemStatus;

    private Boolean isActive;

    public Items toEntity() {
        return Items.builder()
                .itemCode(itemCode.trim())
                .itemName(itemName.trim())
                .itemSpecification(itemSpecification)
                .itemUnit(itemUnit)
                .itemStatus(itemStatus)
                .isActive(isActive != null ? isActive : Boolean.TRUE)
                .build();
    }
}
