package com.beyond.synclab.ctrlline.domain.validator;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.errorcode.ItemLineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainActivationValidator {

    public void validateFactoryActive(Factories factory) {
        if (factory == null || !factory.isActivated()) {
            throw new AppException(FactoryErrorCode.FACTORY_INACTIVE);
        }
    }

    public void validateLineActive(Lines line) {
        validateFactoryActive(line.getFactory());
        if (!line.isActivated()) {
            throw new AppException(LineErrorCode.LINE_INACTIVE);
        }
    }

    public void validateItemActive(Items item) {
        if (!item.isActivated()) {
            throw new AppException(ItemErrorCode.ITEM_INACTIVE);
        }
    }

    public void validateEquipmentActive(Equipments equipment) {
        validateLineActive(equipment.getLine());
        if (!equipment.isActivated()) {
            throw new AppException(LineErrorCode.EQUIPMENT_INACTIVE);
        }
    }

    public void validateItemLineActive(ItemsLines itemLine) {
        validateLineActive(itemLine.getLine());
        validateItemActive(itemLine.getItem());

        if (!itemLine.isActivated()) {
            throw new AppException(ItemLineErrorCode.ITEM_LINE_INACTIVE);
        }
    }
}
