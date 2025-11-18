package com.beyond.synclab.ctrlline.domain.line.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.line.dto.LineResponseDto;
import com.beyond.synclab.ctrlline.domain.line.dto.LineSearchCommand;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.line.spec.LineSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {
    private final LineRepository lineRepository;

    @Override
    @Transactional(readOnly = true)
    public LineResponseDto getLine(String lineCode) {
        Lines line = lineRepository.findBylineCode(lineCode).orElseThrow(() -> new AppException(
                LineErrorCode.LINE_NOT_FOUND));

        return LineResponseDto.fromEntity(line, line.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LineResponseDto> getLineList(LineSearchCommand command,
                                             Pageable pageable) {
        Specification<Lines> spec = Specification.allOf(
                LineSpecification.lineNameContains(command.lineName()),
                LineSpecification.lineCodeEquals(command.lineCode()),
                LineSpecification.withUserConditions(command.userName(), command.userDepartment()),
                LineSpecification.activeEquals(command.isActive())
        );

        return lineRepository.findAll(spec, pageable)
                             .map(line -> LineResponseDto.fromEntity(line, line.getUser()));

    }
}
