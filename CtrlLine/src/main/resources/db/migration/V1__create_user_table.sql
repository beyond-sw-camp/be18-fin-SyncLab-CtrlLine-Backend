DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `user_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `emp_no` VARCHAR(32) NOT NULL COMMENT '입사연도(4)월(2)순번(3)',
    `user_name` VARCHAR(32) NOT NULL,
    `user_email` VARCHAR(32) NOT NULL,
    `user_password` VARCHAR(64) NOT NULL,
    `user_phone_number` VARCHAR(32) NOT NULL,
    `user_hired_date` DATE NOT NULL,
    `user_termination_date` DATE DEFAULT NULL,
    `user_role` ENUM('USER','MANAGER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '유저 < 담당자 < 관리자',
    `user_status` ENUM('ACTIVE','LEAVE','RESIGNED') NOT NULL DEFAULT 'ACTIVE' COMMENT '재직, 휴직, 퇴사',
    `user_department` VARCHAR(32) NOT NULL,
    `user_position` ENUM('ASSISTANT','ASSISTANT_MANAGER','MANAGER','GENERAL_MANAGER','DIRECTOR','CEO') NOT NULL DEFAULT 'ASSISTANT' COMMENT '직급',
    `user_address` VARCHAR(64) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
