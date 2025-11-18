
-- 1. 품목 시리얼 테이블에서 품목 ID 제거
ALTER TABLE item_serial
    DROP COLUMN IF EXISTS item_id;

-- 2. 설비상태 테이블 PK auto_increment 제거
ALTER TABLE equipment_status
    MODIFY COLUMN equipment_status_id BIGINT NOT NULL;  -- 기존 PK 타입 맞춰서 auto_increment 제거

-- 3. user 테이블 emp_no 컬럼에 UNIQUE 제약 추가
ALTER TABLE user
    ADD CONSTRAINT uq_user_emp_no UNIQUE (emp_no);