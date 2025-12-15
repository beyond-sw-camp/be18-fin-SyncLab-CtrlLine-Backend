-- 1. 컬럼 추가
ALTER TABLE production_plan
    ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT 'optimistic lock version';

-- 2. 기존 데이터 초기화 (명시적)
UPDATE production_plan
SET `version` = 0
WHERE `version` IS NULL;