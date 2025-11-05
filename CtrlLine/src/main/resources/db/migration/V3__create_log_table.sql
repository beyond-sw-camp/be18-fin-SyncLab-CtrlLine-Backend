-- V3__create_user_log_table.sql
-- 엔티티의 CUD(Create, Update, Delete) 로그를 기록하는 테이블 생성

CREATE TABLE `log` (
        `log_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
        `user_id` BIGINT(20) NOT NULL,
        `action_type` ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL COMMENT 'CUD 중 어떤 동작인지',
        `entity_name` VARCHAR(32) NOT NULL COMMENT '엔티티 클래스명 또는 테이블명',
        `entity_id` BIGINT(20) NOT NULL COMMENT '대상 엔티티의 PK',
        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (`log_id`),
        INDEX `IDX_USER_LOG_USER_ID` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
