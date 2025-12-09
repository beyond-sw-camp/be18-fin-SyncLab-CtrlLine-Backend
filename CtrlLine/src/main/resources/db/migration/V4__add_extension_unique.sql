-- V4__safe_unique_constraint_user_extension.sql
-- 목적: user_extension (NOT NULL)에 안전하게 UNIQUE 제약을 추가한다.

-- ------------------------------------------------------------------------------
-- 1) 중복 extension 값 탐지 후, 대표 row만 남기고
--    나머지는 UNIQUE 충돌을 피하기 위해 안전한 대체값으로 자동 치환한다.
-- ------------------------------------------------------------------------------
UPDATE `user` u
    JOIN (
        SELECT user_extension, MIN(user_id) AS keep_id
        FROM `user`
        GROUP BY user_extension
        HAVING COUNT(*) > 1
    ) dup ON u.user_extension = dup.user_extension AND u.user_id <> dup.keep_id
SET u.user_extension = CONCAT(u.user_extension, '-DUP-', u.user_id);

-- 위 방식은 다음 조건을 만족:
--   - NOT NULL 위반 없음
--   - UNIQUE 인덱스 적용 전 모든 값이 고유해짐
--   - 중복이 있어도 migration NEVER FAILS

-- ------------------------------------------------------------------------------
-- 2) 기존 인덱스가 있으면 제거 (재배포 안전)
-- ------------------------------------------------------------------------------
DROP INDEX IF EXISTS `uq_user_extension` ON `user`;

-- ------------------------------------------------------------------------------
-- 3) UNIQUE 제약 추가
-- ------------------------------------------------------------------------------
ALTER TABLE `user`
    ADD CONSTRAINT `uq_user_extension`
        UNIQUE (`user_extension`);
