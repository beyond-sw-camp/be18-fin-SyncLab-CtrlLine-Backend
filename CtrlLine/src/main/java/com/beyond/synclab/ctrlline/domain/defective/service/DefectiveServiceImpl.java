package com.beyond.synclab.ctrlline.domain.defective.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.errorcode.DefectiveErrorCode;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXrefs;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveXrefRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectiveServiceImpl implements DefectiveService {

    private final PlanDefectiveXrefRepository planDefectiveXrefRepository;
    private final PlanDefectiveRepository planDefectiveRepository;
    private final ProductionPerformanceRepository productionPerformanceRepository;

    @Override
    @Transactional(readOnly = true)
    public GetDefectiveDetailResponseDto getDefective(Long id) {
        PlanDefectives planDefectives = planDefectiveRepository.findById(id)
            .orElseThrow(() -> new AppException(DefectiveErrorCode.PLAN_DEFECTIVE_NOT_FOUND));

        List<PlanDefectiveXrefs> planDefectiveXrefList = planDefectiveXrefRepository.findAllByPlanDefectiveId(planDefectives.getId());

        return GetDefectiveDetailResponseDto.fromEntity(planDefectives, planDefectiveXrefList);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetDefectiveListResponseDto> getDefectiveList(
        SearchDefectiveListRequestDto requestDto,
        Pageable pageable
    ) {
        return planDefectiveRepository.findDefectiveList(requestDto, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetDefectiveAllResponseDto> getAllDefective(
        SearchDefectiveAllRequestDto requestDto
    ) {
        return planDefectiveRepository.findAllDefective(requestDto);
    }
}
