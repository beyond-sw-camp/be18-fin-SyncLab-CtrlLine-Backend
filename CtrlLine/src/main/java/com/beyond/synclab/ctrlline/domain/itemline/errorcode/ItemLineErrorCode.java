package com.beyond.synclab.ctrlline.domain.itemline.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ItemLineErrorCode implements ErrorCode {
    ITEM_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM_LINE_NOT_FOUND", "라인 생산 가능 품목이 존재하지 않습니다."),
    INVALID_ITEM_LIST(HttpStatus.BAD_REQUEST, "INVALID_ITEM_LIST", "라인 생산 가능 품목 목록이 비어있거나 중복되었습니다."),
    DUPLICATED_ITEM_LINE(HttpStatus.CONFLICT, "DUPLICATED_ITEM_LINE", "이미 라인에 등록된 품목입니다."),
    ITEM_LINE_INACTIVE(HttpStatus.BAD_REQUEST, "ITEM_LINE_INACTIVE" , "라인 생산 가능 품목이 비활성화 상태입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
