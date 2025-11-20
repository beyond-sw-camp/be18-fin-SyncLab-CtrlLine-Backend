# -- 1) 새 테이블 생성
# CREATE TABLE plan_defective LIKE performance_defective;
#
# -- 2) 데이터 복사
# INSERT INTO plan_defective SELECT * FROM performance_defective;
#
# -- 3) 기존 테이블 삭제
# DROP TABLE performance_defective;

-- 2) 컬럼 이름 변경 (타입 명시 필요)
ALTER TABLE plan_defective
    CHANGE COLUMN performance_defective_id plan_defective_id BIGINT;
