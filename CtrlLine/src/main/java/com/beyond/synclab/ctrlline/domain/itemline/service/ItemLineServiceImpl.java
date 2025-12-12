package com.beyond.synclab.ctrlline.domain.itemline.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.dto.request.ManageItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.dto.response.GetItemLineListResponseDto;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.errorcode.ItemLineErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        final Lines line = findLineOrThrow(lineCode);
        final List<Items> itemsList = itemLineRepository.findActiveFinishedItemsByLine(line);

        return itemsList.stream()
                .map(GetItemLineListResponseDto::fromEntity)
                .toList();
    }

    // 등록 탭 - 특정 라인에 생산 가능 품목 등록
    @Override
    @Transactional
    public void createItemLine(final String lineCode, final ManageItemLineRequestDto requestDto) {
        log.info("라인({}) 생산 가능 품목 등록 요청", lineCode);

        final Lines line = findLineOrThrow(lineCode);
        final List<String> itemCodes = normalizeItemCodes(requestDto, false);
        final Map<String, Items> itemsByCode = findItemsByCodes(itemCodes);
        final Set<Long> existingItemIds = itemLineRepository.findByLine(line).stream()
                .map(ItemsLines::getItemId)
                .collect(Collectors.toSet());

        final List<ItemsLines> newMappings = itemCodes.stream()
                .map(itemsByCode::get)
                .filter(item -> !existingItemIds.contains(item.getId()))
                .map(item -> buildItemLine(line, item))
                .toList();

        if (newMappings.isEmpty()) {
            throw new AppException(ItemLineErrorCode.DUPLICATED_ITEM_LINE);
        }

        itemLineRepository.saveAll(newMappings);
        log.info("라인({}) 생산 가능 품목 {}건 등록 완료", lineCode, newMappings.size());
    }

    // 수정 탭 - 특정 라인의 생산 가능 품목 전체 수정
    @Override
    @Transactional
    public void updateItemLine(final String lineCode, final ManageItemLineRequestDto requestDto) {
        log.info("라인({}) 생산 가능 품목 수정 요청", lineCode);

        final Lines line = findLineOrThrow(lineCode);
        final List<String> itemCodes = normalizeItemCodes(requestDto, true);
        final Map<String, Items> itemsByCode = findItemsByCodes(itemCodes);

        // 기존 매핑 전체 삭제
        final List<ItemsLines> existingMappings = itemLineRepository.findByLine(line);
        if (!existingMappings.isEmpty()) {
            itemLineRepository.deleteAllInBatch(existingMappings);
            log.debug("라인({}) 기존 매핑 {}건 삭제 완료", lineCode, existingMappings.size());
        }

        if (itemCodes.isEmpty()) {
            log.info("라인({})의 생산 가능 품목이 0건으로 수정 완료", lineCode);
            return;
        }

        final List<ItemsLines> newMappings = itemCodes.stream()
                .map(itemsByCode::get)
                .map(item -> buildItemLine(line, item))
                .toList();

        itemLineRepository.saveAll(newMappings);
        log.info("라인({})의 생산 가능 품목이 {}건으로 수정 완료", lineCode, newMappings.size());
    }

    private Lines findLineOrThrow(final String lineCode) {
        return lineRepository.findBylineCode(lineCode)
                .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));
    }

    private List<String> normalizeItemCodes(final ManageItemLineRequestDto requestDto, final boolean allowEmpty) {
        if (requestDto == null || requestDto.getItemCodes() == null) {
            if (allowEmpty) {
                return List.of();
            }
            throw new AppException(ItemLineErrorCode.INVALID_ITEM_LIST);
        }

        final List<String> normalized = requestDto.getItemCodes().stream()
                .map(code -> code == null ? "" : code.trim())
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (normalized.isEmpty()) {
            if (allowEmpty) {
                return List.of();
            }
            throw new AppException(ItemLineErrorCode.INVALID_ITEM_LIST);
        }

        final Set<String> duplicates = new HashSet<>();
        for (String code : normalized) {
            if (!duplicates.add(code)) {
                throw new AppException(ItemLineErrorCode.INVALID_ITEM_LIST);
            }
        }

        return normalized;
    }

    private Map<String, Items> findItemsByCodes(final List<String> itemCodes) {
        if (itemCodes.isEmpty()) {
            return Map.of();
        }

        final List<Items> items = itemRepository.findByItemCodeIn(itemCodes);
        final Map<String, Items> itemsByCode = items.stream()
                .collect(Collectors.toMap(Items::getItemCode, item -> item));

        itemCodes.forEach(code -> {
            if (!itemsByCode.containsKey(code)) {
                throw new AppException(ItemErrorCode.ITEM_NOT_FOUND);
            }
        });

        return itemsByCode;
    }

    private ItemsLines buildItemLine(final Lines line, final Items item) {
        return ItemsLines.builder()
                .lineId(line.getId())
                .itemId(item.getId())
                .build();
    }
}
