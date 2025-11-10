package com.beyond.synclab.ctrlline.domain.factory.repository;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FactoryQueryRepositoryImpl implements FactoryQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Factories> searchFactoryList(FactorySearchDto searchDto, Pageable pageable) {
        // Querydsl 쿼리를 작성할 때 테이블 대신 사용할 별칭으로 선언
        QFactories factory = QFactories.factories;
        QUsers user = QUsers.users;

        // 목록 조회 쿼리
        List<Factories> factoryList = queryFactory.selectFrom(factory)
                .leftJoin(factory.users, user).fetchJoin()
                .where(
                        factoryCodeContains(searchDto.getFactoryCode()),
                        factoryNameContains(searchDto.getFactoryName()),
                        departmentContains(searchDto.getDepartment()),
                        managerNameContains(searchDto.getName()),
                        isActiveEq(searchDto.getIsActive())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(factory.factoryCode.asc())
                .fetch();

        // total count
        JPAQuery<Long> countQuery = queryFactory
                .select(factory.count())
                .from(factory)
                .leftJoin(factory.users, user)
                .where(
                        factoryCodeContains(searchDto.getFactoryCode()),
                        factoryNameContains(searchDto.getFactoryName()),
                        departmentContains(searchDto.getDepartment()),
                        managerNameContains(searchDto.getName()),
                        isActiveEq(searchDto.getIsActive())
                );

        return PageableExecutionUtils.getPage(factoryList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression factoryCodeContains(String factoryCode) {
        return StringUtils.hasText(factoryCode)
                ? QFactories.factories.factoryCode.containsIgnoreCase(factoryCode)
                : null;
    }

    private BooleanExpression factoryNameContains(String factoryName) {
        return StringUtils.hasText(factoryName)
                ? QFactories.factories.factoryName.containsIgnoreCase(factoryName)
                : null;
    }

    private BooleanExpression departmentContains(String department) {
        return StringUtils.hasText(department)
                ? QFactories.factories.users.department.containsIgnoreCase(department)
                : null;
    }

    private BooleanExpression managerNameContains(String manager) {
        return StringUtils.hasText(manager)
                ? QFactories.factories.users.name.containsIgnoreCase(manager)
                : null;
    }

    private BooleanExpression isActiveEq(Boolean isActive) {
        return isActive != null
                ? QFactories.factories.isActive.eq(isActive)
                : null;
    }
}
