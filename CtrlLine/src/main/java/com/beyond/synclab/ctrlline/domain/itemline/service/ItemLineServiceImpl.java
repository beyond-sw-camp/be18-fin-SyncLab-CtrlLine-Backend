package com.beyond.synclab.ctrlline.domain.itemline.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.itemline.dto.request.UpdateItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.dto.response.GetItemLineListResponseDto;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemLineServiceImpl implements ItemLineService {

    private final ItemLineRepository itemLineRepository;
    private final ItemRepository itemRepository;
    private final LineRepository lineRepository;

    // 조회 탭 - 특정 라인에서 생산 가능한 품목 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetItemLineListResponseDto> getItemLineList(final String lineCode) {
        final Lines line = lineRepository.getReferenceByLineCode(lineCode);
        final List<Items> itemsList = itemLineRepository.findActiveFinishedItemsByLine(line);

        return itemsList.stream()
                .map(GetItemLineListResponseDto::fromEntity)
                .toList();
    }

    // 수정 탭 - 특정 라인의 생산 가능 품목 전체 수정
    @Override
    @Transactional
    public void updateItemLine(final String lineCode, final UpdateItemLineRequestDto requestDto) {
        log.info("라인({}) 생산 가능 품목 수정 요청", lineCode);

        final Lines line = lineRepository.getReferenceByLineCode(lineCode);

        // 기존 매핑 전체 삭제
        final List<ItemsLines> existingMappings = itemLineRepository.findByLine(line);
        if (!existingMappings.isEmpty()) {
            itemLineRepository.deleteAllInBatch(existingMappings);
            log.debug("라인({}) 기존 매핑 {}건 삭제 완료", lineCode, existingMappings.size());
        }

        // 신규 매핑 생성
        if (requestDto.getItemCodes() == null || requestDto.getItemCodes().isEmpty()) {
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        final List<ItemsLines> newMappings = requestDto.getItemCodes().stream()
                .map(code -> {
                    Items item = Items.builder()
                            .itemCode(code)
                            .build();
                    return ItemsLines.builder()
                            .item(item)
                            .line(line)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                })
                .toList();

        itemLineRepository.saveAll(newMappings);
        log.info("라인({})의 생산 가능 품목 {}건 신규 등록 완료", lineCode, newMappings.size());
    }
}

