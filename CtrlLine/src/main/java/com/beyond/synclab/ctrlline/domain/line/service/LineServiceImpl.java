package com.beyond.synclab.ctrlline.domain.line.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.line.dto.LineResponseDto;
import com.beyond.synclab.ctrlline.domain.line.dto.LineSearchCommand;
import com.beyond.synclab.ctrlline.domain.line.dto.UpdateLineActRequestDto;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.line.spec.LineSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {
    private final LineRepository lineRepository;

    @Override
    @Transactional(readOnly = true)
    public LineResponseDto getLine(String lineCode) {
        Lines line = lineRepository.findBylineCode(lineCode).orElseThrow(() -> new AppException(
                LineErrorCode.LINE_NOT_FOUND));

        return LineResponseDto.fromEntity(line, line.getUser(), line.getFactory());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LineResponseDto> getLineList(LineSearchCommand command,
                                             Pageable pageable) {
        Specification<Lines> spec = Specification.allOf(
                LineSpecification.factoryEquals(command.factoryId()),
                LineSpecification.itemEquals(command.itemId()),
                LineSpecification.lineNameContains(command.lineName()),
                LineSpecification.lineCodeEquals(command.lineCode()),
                LineSpecification.withUserConditions(command.userName(), command.userDepartment()),
                LineSpecification.activeEquals(command.isActive())
        );

        log.debug(">>>> 라인 필터링: {}",command.factoryId());

        return lineRepository.findAll(spec, pageable)
                             .map(line -> LineResponseDto.fromEntity(line, line.getUser(), line.getFactory()));

    }

    @Override
    @Transactional
    public Boolean updateLineAct(UpdateLineActRequestDto request) {
        if (request.getLineIds() == null || request.getLineIds().isEmpty()) {
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        request.getLineIds().forEach(id -> {
            Lines line = lineRepository.findById(id)
                    .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));
            line.updateActive(request.getIsActive());
        });

        log.info("[LINE-ACT] {}건 isActive 변경 완료 (isActive={})",
                request.getLineIds().size(), request.getIsActive());

        return request.getIsActive();
    }
}
