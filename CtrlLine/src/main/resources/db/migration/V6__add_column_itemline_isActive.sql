-- V6__add_column_itemline_is_active.sql
-- 목적: items_lines 테이블에 is_active 컬럼을 안전하게 추가한다.
-- 특징:
--  - 재실행해도 실패하지 않음
--  - 운영/스테이징/로컬 환경 차이 흡수
--  - 기존 데이터 100% 보존

-- ---------------------------------------------------------------------
-- 1. is_active 컬럼이 없을 때만 추가
-- ---------------------------------------------------------------------
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'item_line'
      AND COLUMN_NAME = 'is_active'
);

SET @sql_add_column := IF(
    @col_exists = 0,
    'ALTER TABLE item_line
        ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1
        COMMENT ''생산 가능 여부 (1: ACTIVE, 0: INACTIVE)''',
    'SELECT ''Column is_active already exists'';'
);

PREPARE stmt FROM @sql_add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 인덱스가 없을 때만 생성 (line_id + is_active)
-- ---------------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'item_line'
      AND INDEX_NAME = 'idx_items_lines_line_active'
);

SET @sql_add_index := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_items_lines_line_active
        ON item_line (line_id, is_active)',
    'SELECT ''Index idx_items_lines_line_active already exists'';'
);

PREPARE stmt FROM @sql_add_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 보장 사항
-- - 기존 items_lines row는 모두 is_active = 1 (ACTIVE)
-- - 과거 생산계획 / 실적 / 이력 참조 안전
-- - 재배포 시 Flyway validation FAIL 없음
-- ---------------------------------------------------------------------
