package com.beyond.synclab.ctrlline.domain.item.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class ItemCodeConflictException extends AppException {
    public ItemCodeConflictException(String itemCode) {
        super(ItemErrorCode.ITEMCODE_CONFLICT);
    }
}
