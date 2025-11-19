package com.beyond.synclab.ctrlline.domain.process.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProcessErrorCode implements ErrorCode {
    // 400, 401, 500은 CommonErrorCode에 있음.

    // 404 NOT FOUND
    PROCESS_NOT_FOUND(HttpStatus.NOT_FOUND, "PROCESS_NOT_FOUND", "해당 공정을 찾을 수 없습니다."),
    PROCESS_EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROCESS_EQUIPMENT_NOT_FOUND", "해당 공정과 연결된 설비를 찾을 수 없습니다."),
    // USER_NOT_FOUND는 CommonErrorCode에 있음.

    // 409
    PROCESS_CONFICT(HttpStatus.CONFLICT, "PROCESS_CONFLICT", "이미 존재하는 공정 코드입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ProcessErrorCode(HttpStatus status, String code, String message)
            {
                this.status = status;
                this.code = code;
                this.message = message;
            }
}
