-- item_specification 컬럼 길이 확장
ALTER TABLE `item`
    MODIFY COLUMN `item_specification` VARCHAR(64) NULL;
