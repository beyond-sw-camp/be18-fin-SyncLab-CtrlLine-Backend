package com.beyond.synclab.ctrlline.domain.item.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class ItemNotFoundException extends AppException {
    public ItemNotFoundException(String itemCode) {
        super(ItemErrorCode.ITEM_NOT_FOUND);
    }
}
