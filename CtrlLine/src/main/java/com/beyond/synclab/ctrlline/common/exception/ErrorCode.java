package com.beyond.synclab.ctrlline.common.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
