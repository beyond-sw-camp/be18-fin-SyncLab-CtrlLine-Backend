-- ===========================================
-- V11__drop_defective_qty.sql
-- defective 테이블에서 defective_qty 컬럼 제거
-- defective_xref 테이블 생성
-- ===========================================

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE defective
    DROP COLUMN IF EXISTS defective_qty;

ALTER TABLE performance_defective
    DROP COLUMN IF EXISTS production_performance_id,
    DROP COLUMN IF EXISTS defective_id,
    DROP COLUMN IF EXISTS defective_qty;

ALTER TABLE IF EXISTS lot_no
 RENAME TO lot;

ALTER TABLE lot
    DROP COLUMN IF EXISTS production_performance_id;
    
CREATE TABLE IF NOT EXISTS plan_defective_xref
(
    defective_xref_id       BIGINT(20) NOT NULL AUTO_INCREMENT,
    defective_id            BIGINT(20) NOT NULL,
    plan_defective_id BIGINT(20) NOT NULL,
    defective_qty           DECIMAL(10, 2) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (defective_xref_id)
);

SET FOREIGN_KEY_CHECKS = 1;
