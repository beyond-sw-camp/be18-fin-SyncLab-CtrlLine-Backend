package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.entity.QEquipments;
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
public class EquipmentQueryRepositoryImpl implements EquipmentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Equipments> searchEquipmentList(EquipmentSearchDto searchDto, Pageable pageable) {

        QEquipments equipment = QEquipments.equipments;
        QUsers user = QUsers.users;

        // === 목록 조회 쿼리 ===
        List<Equipments> equipmentList = queryFactory
                .selectFrom(equipment)
                .leftJoin(equipment.users, user).fetchJoin()
                .where(
                        equipmentCodeContains(searchDto.getEquipmentCode()),
                        equipmentNameContains(searchDto.getEquipmentName()),
                        equipmentTypeContains(searchDto.getEquipmentType()),
                        userNameContains(searchDto.getUserName()),
                        departmentContains(searchDto.getUserDepartment()),
                        isActiveEq(searchDto.getIsActive())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(equipment.equipmentCode.asc())
                .fetch();

        // === count query ===
        JPAQuery<Long> countQuery = queryFactory
                .select(equipment.count())
                .from(equipment)
                .leftJoin(equipment.users, user)
                .where(
                        equipmentCodeContains(searchDto.getEquipmentCode()),
                        equipmentNameContains(searchDto.getEquipmentName()),
                        equipmentTypeContains(searchDto.getEquipmentType()),
                        userNameContains(searchDto.getUserName()),
                        departmentContains(searchDto.getUserDepartment()),
                        isActiveEq(searchDto.getIsActive())
                );

        return PageableExecutionUtils.getPage(equipmentList, pageable, countQuery::fetchOne);
    }

    // ==========================
    //   검색 조건 메서드
    // ==========================

    private BooleanExpression equipmentCodeContains(String code) {
        return StringUtils.hasText(code)
                ? QEquipments.equipments.equipmentCode.containsIgnoreCase(code)
                : null;
    }

    private BooleanExpression equipmentNameContains(String name) {
        return StringUtils.hasText(name)
                ? QEquipments.equipments.equipmentName.containsIgnoreCase(name)
                : null;
    }

    private BooleanExpression equipmentTypeContains(String type) {
        return StringUtils.hasText(type)
                ? QEquipments.equipments.equipmentType.containsIgnoreCase(type)
                : null;
    }

    private BooleanExpression userNameContains(String userName) {
        return StringUtils.hasText(userName)
                ? QUsers.users.name.containsIgnoreCase(userName)
                : null;
    }

    private BooleanExpression departmentContains(String dept) {
        return StringUtils.hasText(dept)
                ? QUsers.users.department.containsIgnoreCase(dept)
                : null;
    }

    private BooleanExpression isActiveEq(Boolean isActive) {
        return isActive != null
                ? QEquipments.equipments.isActive.eq(isActive)
                : null;
    }
}
