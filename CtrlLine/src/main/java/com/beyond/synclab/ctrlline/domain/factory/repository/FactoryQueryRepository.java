package com.beyond.synclab.ctrlline.domain.factory.repository;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FactoryQueryRepository {
    Page<Factories> searchFactoryList(FactorySearchDto searchDto, Pageable pageable);
}
