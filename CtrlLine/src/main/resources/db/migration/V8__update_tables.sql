-- ===========================================
-- VXXX__update_mes_tables.sql
-- MES 구조 변경 - 결함, 실적, 교차 테이블 정비
-- ===========================================

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. defective (불량) 테이블 구조 변경
-- ----------------------------------------------------------

-- 1-1. 불량 타입 컬럼 추가 이미 존재하면 skip
ALTER TABLE defective
    ADD COLUMN defective_type VARCHAR(255) NOT NULL AFTER defective_qty;

-- 1-2. 전표번호(document_no) 제거 (존재하는 경우에만)
ALTER TABLE defective
    DROP COLUMN IF EXISTS document_no;

-- 1-3. 처리상태(status) 제거 (존재하는 경우에만)
ALTER TABLE defective
    DROP COLUMN IF EXISTS defective_status;

-- ----------------------------------------------------------
-- 2. performance_defective (교차 테이블) 변경
-- ----------------------------------------------------------

-- 2-1. 불량률(defective_rate / defect_rate?) 제거
ALTER TABLE `performance_defective`
    DROP COLUMN IF EXISTS defective_rate;

-- 2-2. 전표번호(document_no) 추가
ALTER TABLE `performance_defective`
    ADD COLUMN defective_document_no VARCHAR(32) NOT NULL AFTER defective_id;


-- ----------------------------------------------------------
-- 3. production_performance (생산실적) 변경
-- ----------------------------------------------------------

-- 3-1. 불량률 추가
ALTER TABLE production_performance
    ADD COLUMN performance_defective_rate DECIMAL(10,2) NULL
        AFTER performance_qty;

-- ----------------------------------------------------------
-- 4. lot_no 테이블 변경
-- ----------------------------------------------------------

-- 4-1. 삭제여부 컬럼 추가 (이미 있으면 skip)
ALTER TABLE lot_no
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER updated_at;

-- ----------------------------------------------------------
-- 5. production_plan 칼럼 접두사 변경
-- ----------------------------------------------------------
-- 안전하게 RENAME COLUMN 처리


ALTER TABLE production_plan
    RENAME COLUMN document_no TO plan_document_no;

-- ----------------------------------------------------------
-- 6. production_performance 칼럼 접두사 변경
-- ----------------------------------------------------------

ALTER TABLE production_performance
    RENAME COLUMN document_no TO performance_document_no;


SET FOREIGN_KEY_CHECKS = 1;
