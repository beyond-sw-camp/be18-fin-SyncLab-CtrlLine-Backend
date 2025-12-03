-- --------------------------------------------------------
-- 호스트:                          121.170.161.72
-- 서버 버전:                        12.1.2-MariaDB-ubu2404 - mariadb.org binary distribution
-- 서버 OS:                        debian-linux-gnu
-- HeidiSQL 버전:                  12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- 테이블 ctrlline.alarm 구조 내보내기
CREATE TABLE IF NOT EXISTS `alarm` (
  `alarm_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `equipment_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL COMMENT '처리자',
  `alarm_code` varchar(32) NOT NULL,
  `alarm_name` varchar(32) NOT NULL,
  `alarm_type` varchar(32) NOT NULL,
  `alarm_level` varchar(32) DEFAULT NULL,
  `occurred_at` datetime DEFAULT NULL,
  `cleared_at` datetime DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `alarm_cause` text DEFAULT NULL,
  PRIMARY KEY (`alarm_id`),
  UNIQUE KEY `uq_alarm_code` (`alarm_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.defective 구조 내보내기
CREATE TABLE IF NOT EXISTS `defective` (
  `defective_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `equipment_id` bigint(20) NOT NULL,
  `defective_code` varchar(32) NOT NULL,
  `defective_name` varchar(32) NOT NULL,
  `defective_type` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`defective_id`),
  UNIQUE KEY `uq_defective_code` (`defective_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.equipment 구조 내보내기
CREATE TABLE IF NOT EXISTS `equipment` (
  `equipment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `line_id` bigint(20) NOT NULL,
  `equipment_status_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `equipment_code` varchar(32) NOT NULL,
  `equipment_name` varchar(32) NOT NULL,
  `equipment_type` varchar(32) NOT NULL,
  `operating_time` timestamp NOT NULL,
  `maintenance_history` timestamp NULL DEFAULT NULL COMMENT '제일 최근 날짜',
  `equipment_ppm` decimal(10,2) NOT NULL,
  `total_count` decimal(10,2) NOT NULL,
  `defective_count` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`equipment_id`),
  UNIQUE KEY `uq_equipment_code` (`equipment_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.equipment_status 구조 내보내기
CREATE TABLE IF NOT EXISTS `equipment_status` (
  `equipment_status_id` bigint(20) NOT NULL,
  `equipment_status_code` varchar(32) NOT NULL,
  `equipment_status_name` varchar(32) NOT NULL,
  `description` varchar(256) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`equipment_status_id`),
  UNIQUE KEY `uq_equipment_status_code` (`equipment_status_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.factory 구조 내보내기
CREATE TABLE IF NOT EXISTS `factory` (
  `factory_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '공장장',
  `factory_code` varchar(32) NOT NULL,
  `factory_name` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`factory_id`),
  UNIQUE KEY `uq_factory_code` (`factory_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.item 구조 내보내기
CREATE TABLE IF NOT EXISTS `item` (
  `item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_code` varchar(32) NOT NULL,
  `item_name` varchar(32) NOT NULL,
  `item_specification` varchar(128) DEFAULT NULL,
  `item_unit` varchar(32) NOT NULL,
  `item_status` enum('RAW_MATERIAL','AUXILIARY_MATERIAL','SEMI_FINISHED_PRODUCT','FINISHED_PRODUCT') NOT NULL COMMENT '원재료, 부재료, 반제품, 완제품',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uq_item_code` (`item_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.item_line 구조 내보내기
CREATE TABLE IF NOT EXISTS `item_line` (
  `item_line_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `line_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`item_line_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.item_serial 구조 내보내기
CREATE TABLE IF NOT EXISTS `item_serial` (
  `item_serial_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lot_id` bigint(20) NOT NULL,
  `serial_file_path` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`item_serial_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.line 구조 내보내기
CREATE TABLE IF NOT EXISTS `line` (
  `line_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `factory_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL COMMENT '반장',
  `line_code` varchar(32) NOT NULL,
  `line_name` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`line_id`),
  UNIQUE KEY `uq_line_code` (`line_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.log 구조 내보내기
CREATE TABLE IF NOT EXISTS `log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `action_type` enum('CREATE','UPDATE','DELETE') NOT NULL,
  `entity_name` varchar(64) NOT NULL,
  `entity_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.lot 구조 내보내기
CREATE TABLE IF NOT EXISTS `lot` (
  `Lot_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) NOT NULL,
  `production_plan_id` bigint(20) NOT NULL,
  `lot_no` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`Lot_id`),
  UNIQUE KEY `uq_lot_no` (`lot_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.mes_data 구조 내보내기
CREATE TABLE IF NOT EXISTS `mes_data` (
  `mes_data_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `factory_id` bigint(20) DEFAULT NULL,
  `power_consumption` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`mes_data_id`)

) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.plan_defective 구조 내보내기
CREATE TABLE IF NOT EXISTS `plan_defective` (
  `plan_defective_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `production_plan_id` bigint(20) NOT NULL,
  `defective_document_no` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`plan_defective_id`),
  UNIQUE KEY `uq_defective_document_no` (`defective_document_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.plan_defective_xref 구조 내보내기
CREATE TABLE IF NOT EXISTS `plan_defective_xref` (
  `defective_xref_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `defective_id` bigint(20) NOT NULL,
  `plan_defective_id` bigint(20) NOT NULL,
  `defective_qty` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `last_reported_qty` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`defective_xref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.process 구조 내보내기
CREATE TABLE IF NOT EXISTS `process` (
  `process_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `equipment_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `process_code` varchar(32) NOT NULL,
  `process_name` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`process_id`),
  UNIQUE KEY `uq_process_code` (`process_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.production_performance 구조 내보내기
CREATE TABLE IF NOT EXISTS `production_performance` (
  `production_performance_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `production_plan_id` bigint(20) NOT NULL,
  `performance_document_no` varchar(32) NOT NULL,
  `total_qty` decimal(10,2) NOT NULL,
  `performance_qty` decimal(10,2) NOT NULL,
  `performance_defective_rate` decimal(10,2) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `remark` text DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`production_performance_id`),
  UNIQUE KEY `uq_performance_document_no` (`performance_document_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.production_plan 구조 내보내기
CREATE TABLE IF NOT EXISTS `production_plan` (
  `production_plan_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sales_manager_id` bigint(20) NOT NULL,
  `production_manager_id` bigint(20) NOT NULL,
  `item_line_id` bigint(20) NOT NULL,
  `plan_document_no` varchar(32) NOT NULL,
  `production_plan_status` enum('PENDING','CONFIRMED','RUNNING','COMPLETED','RETURNED') NOT NULL DEFAULT 'PENDING',
  `due_date` date NOT NULL,
  `planned_qty` decimal(10,2) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `remark` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`production_plan_id`),
  UNIQUE KEY `uq_plan_document_no` (`plan_document_no`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

-- 테이블 ctrlline.user 구조 내보내기
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `emp_no` varchar(32) NOT NULL COMMENT '입사연도(4)월(2)순번(3)',
  `user_name` varchar(32) NOT NULL,
  `user_email` varchar(32) NOT NULL,
  `user_password` varchar(64) NOT NULL,
  `user_phone_number` varchar(32) NOT NULL,
  `user_hired_date` date NOT NULL,
  `user_termination_date` date DEFAULT NULL,
  `user_role` enum('USER','MANAGER','ADMIN') NOT NULL DEFAULT 'USER',
  `user_status` enum('ACTIVE','LEAVE','RESIGNED') NOT NULL DEFAULT 'ACTIVE',
  `user_department` varchar(32) NOT NULL,
  `user_position` enum('ASSISTANT','ASSISTANT_MANAGER','MANAGER','GENERAL_MANAGER','DIRECTOR','CEO') NOT NULL DEFAULT 'ASSISTANT',
  `user_address` varchar(64) NOT NULL,
  `user_extension` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_user_emp_no` (`emp_no`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 내보낼 데이터가 선택되어 있지 않습니다.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
