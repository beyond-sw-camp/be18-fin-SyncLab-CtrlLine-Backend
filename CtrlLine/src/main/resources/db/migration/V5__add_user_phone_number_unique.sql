-- V5__safe_unique_constraint_user_phone_number.sql
-- 목적: user_phone_number(NOT NULL)에 안전하게 UNIQUE 제약을 적용한다.
--       중복이 있어도 절대 migration 실패하지 않도록 설계됨.

-- ----------------------------------------------------------------------
-- 1) user_phone_number 중복 탐지 후,
--    대표 row(가장 작은 user_id)만 유지하고,
--    나머지는 고유한 safe value 로 자동 치환한다.
-- ----------------------------------------------------------------------
UPDATE `user` u
    JOIN (
        SELECT user_phone_number, MIN(user_id) AS keep_id
        FROM `user`
        GROUP BY user_phone_number
        HAVING COUNT(*) > 1
    ) dup
    ON u.user_phone_number = dup.user_phone_number
        AND u.user_id <> dup.keep_id
SET u.user_phone_number = CONCAT(u.user_phone_number, '-DUP-', u.user_id);

-- 설명:
--  - 중복된 전화번호를 가진 row들 중 대표 1개만 유지
--  - 나머지는 '기존번호-DUP-userId' 형태로 자동 치환
--  - 그래서 UNIQUE 제약 추가 시 충돌 없음
--  - NOT NULL 위반 없음
--  - 운영 DB에서도 절대 실패하지 않는 패턴

-- ----------------------------------------------------------------------
-- 2) 기존 UNIQUE 인덱스가 존재할 경우 제거 (재배포 안전)
-- ----------------------------------------------------------------------
DROP INDEX IF EXISTS `uq_user_phone_number` ON `user`;

-- ----------------------------------------------------------------------
-- 3) UNIQUE 제약을 추가
-- ----------------------------------------------------------------------
ALTER TABLE `user`
    ADD CONSTRAINT `uq_user_phone_number`
        UNIQUE (`user_phone_number`);
