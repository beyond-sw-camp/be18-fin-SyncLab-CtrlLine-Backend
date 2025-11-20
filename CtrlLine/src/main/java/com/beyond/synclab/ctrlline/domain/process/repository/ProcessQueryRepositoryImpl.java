package com.beyond.synclab.ctrlline.domain.process.repository;

import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Registered
@RequiredArgsConstructor
public class ProcessQueryRepositoryImpl implements ProcessQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Processes> searchProcessList(ProcessSearchDto searchDto, Pageable pageable){


    }

}
