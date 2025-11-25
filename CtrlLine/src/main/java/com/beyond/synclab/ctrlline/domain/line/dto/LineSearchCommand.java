package com.beyond.synclab.ctrlline.domain.line.dto;


public record LineSearchCommand(
    Long factoryId,
    Long itemId,
    String lineCode,
    String lineName,
    String userName,
    String userDepartment,
    Boolean isActive
) {

}
