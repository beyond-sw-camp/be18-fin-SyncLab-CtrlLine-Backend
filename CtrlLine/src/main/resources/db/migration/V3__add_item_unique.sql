-- 기존에 동일 인덱스가 존재할 경우 제거
ALTER TABLE `item`
    DROP INDEX IF EXISTS uq_item_code;

-- item_code에 유니크 제약 추가
ALTER TABLE `item`
    ADD CONSTRAINT uq_item_code UNIQUE (`item_code`);
