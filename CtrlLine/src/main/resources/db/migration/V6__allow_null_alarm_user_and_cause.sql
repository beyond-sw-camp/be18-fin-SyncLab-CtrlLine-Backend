ALTER TABLE `alarm`
    MODIFY COLUMN `user_id` BIGINT(20) NULL COMMENT '처리자',
    MODIFY COLUMN `alarm_cause` TEXT NULL;
