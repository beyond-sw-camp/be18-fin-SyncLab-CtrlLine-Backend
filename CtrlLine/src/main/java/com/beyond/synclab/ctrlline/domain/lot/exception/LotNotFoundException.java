package com.beyond.synclab.ctrlline.domain.lot.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class LotNotFoundException extends AppException {
    public LotNotFoundException() {
        super(LotErrorCode.LOT_NOT_FOUND);
    }
}
