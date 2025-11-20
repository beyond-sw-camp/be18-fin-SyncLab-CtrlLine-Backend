package com.beyond.synclab.ctrlline.domain.process.repository;

import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessQueryRepository{
    Page<Processes> searchProcessList(ProcessSearchDto searchDto, Pageable pageable);
}
