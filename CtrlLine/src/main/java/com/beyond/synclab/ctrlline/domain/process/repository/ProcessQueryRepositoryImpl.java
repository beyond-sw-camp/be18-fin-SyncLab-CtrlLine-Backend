package com.beyond.synclab.ctrlline.domain.process.repository;

import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.beyond.synclab.ctrlline.domain.process.entity.QProcesses;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProcessQueryRepositoryImpl implements ProcessQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Processes> searchProcessList(ProcessSearchDto searchDto, Pageable pageable) {
        QProcesses process = QProcesses.processes;
        QUsers user = QUsers.users;

        // 목록 조회
        List<Processes> processList = queryFactory
                .selectFrom(process)
                .leftJoin(process.user, user).fetchJoin()
                .where(
                        processCodeContains(searchDto.getProcessCode()),
                        processNameContains(searchDto.getProcessName()),
                        userNameContains(searchDto.getUserName()),
                        userDepartmentContains(searchDto.getUserDepartment()),
                        isActiveEq(searchDto.getIsActive())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                // 코드로 정렬
                .orderBy(process.processCode.asc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(process.count())
                .from(process)
                .leftJoin(process.user, user)
                .where(
                        processCodeContains(searchDto.getProcessCode()),
                        processNameContains(searchDto.getProcessName()),
                        userNameContains(searchDto.getUserName()),
                        userDepartmentContains(searchDto.getUserDepartment()),
                        isActiveEq(searchDto.getIsActive())
                );
        return PageableExcecutionUtils.getPage(processList, pageable, countQuery::fetchOne);
    }

    // 검색 조건
    // 1. 공정 코드
    private BooleanExpression processCodeContains(String processCode) {
        return StringUtils.hasText(processCode)
                ? QProcesses.processes.processCode.containsIgnoreCase(processCode)
                : null;
    }

    // 2. 공정명
    private BooleanExpression processNameContains(String processName) {
        return StringUtils.hasText(processName)
                ? QProcesses.users.name.containsIgnoreCase(processName)
                :null;
    }

    // 3. 담당자명
    private BooleanExpression userNameContains(String userName) {
        return StringUtils.hasText(userName)
                ? QUsers.users.name.containsIgnoreCase(userName)
                : null;
    }

    // 4. 담당부서
    private BooleanExpression userDepartmentContains(String userDeptartment) {
        return StringUtils.hasText(userDeptartment)
                ? QUsers.users.department.containsIgnoreCase(userDeptartment)
                : null;
    }

    // 5. 공정 사용여부
    private BooleanExpression isActiveEq(Boolean isActive) {
        return isActive != null
                ? QProcesses.processes.isActive.eq(isActive)
                : null;
    }

}
