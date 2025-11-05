package com.beyond.synclab.ctrlline.domain.item.entity;

import com.beyond.synclab.ctrlline.domain.item.enums.ItemAct;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "itemId")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(nullable = false, unique = true, length = 64)
    private String itemCode;

    @Column(nullable = false, length = 64)
    private String itemName;

    @Column(length = 128)
    private String itemSpecification;

    @Column(length = 32)
    private String itemUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    private ItemStatus itemStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_act", nullable = false)
    private ItemAct itemAct;

    @Column(nullable = false)
    private Boolean isActive;

    /** 품목 정보 수정 **/
    public void updateItem(String name, String spec, String unit, ItemStatus status) {
        this.itemName = name;
        this.itemSpecification = spec;
        this.itemUnit = unit;
        this.itemStatus = status;
    }

    /** 품목 비활성화 **/
    public void deactivateItem() {
        this.itemAct = ItemAct.INACTIVE;
        this.isActive = false;
    }

    /** 품목 재활성화 **/
    public void activateItem() {
        this.itemAct = ItemAct.ACTIVE;
        this.isActive = true;
    }
}
