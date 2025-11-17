ALTER TABLE `alarm`
    ADD COLUMN `alarm_level` VARCHAR(32) NULL AFTER `alarm_type`,
    ADD COLUMN `occurred_at` DATETIME NULL AFTER `alarm_level`,
    ADD COLUMN `cleared_at` DATETIME NULL AFTER `occurred_at`;
