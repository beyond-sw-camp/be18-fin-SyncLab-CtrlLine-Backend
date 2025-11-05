DROP TABLE IF EXISTS `production_performance`;
DROP TABLE IF EXISTS `production_plan`;
DROP TABLE IF EXISTS `mes_data`;
DROP TABLE IF EXISTS `line`;
DROP TABLE IF EXISTS `lot_no`;
DROP TABLE IF EXISTS `log`;
DROP TABLE IF EXISTS `item_line_crossed_table`;
DROP TABLE IF EXISTS `equipment`;
DROP TABLE IF EXISTS `equipment_status`;
DROP TABLE IF EXISTS `alarm`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `factory`;
DROP TABLE IF EXISTS `item`;
DROP TABLE IF EXISTS `item_serial`;
DROP TABLE IF EXISTS `defective`;
DROP TABLE IF EXISTS `performance_defective_crossed_table`;
DROP TABLE IF EXISTS `process`;

CREATE TABLE `production_performance` (
  `production_performance_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
  `production_plan_id`	BIGINT(20)	NOT NULL,
  `document_no`	VARCHAR(32)	NOT NULL,
  `total_qty`	DECIMAL(10,2)	NOT NULL,
  `performance_qty`	DECIMAL(10,2)	NOT NULL,
  `start_time`	DATETIME	NOT NULL,
  `end_time`	DATETIME	NOT NULL,
  `remark`	TEXT	NULL,
  `created_at`	TIMESTAMP	NOT NULL,
  `updated_at`	TIMESTAMP	NULL,
  `is_deleted`	BOOLEAN	NOT NULL DEFAULT false
);

CREATE TABLE production_plan (
     production_plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
     sales_manager_id BIGINT NOT NULL,
     production_manager_id BIGINT NOT NULL,
     line_id BIGINT NOT NULL,
     document_no VARCHAR(32) NOT NULL,
     production_plan_status ENUM('PENDING','CONFIRMED','RUNNING','COMPLETED','RETURNED') NOT NULL DEFAULT 'PENDING',
     due_date DATE NOT NULL,
     planned_qty DECIMAL(10,2) NOT NULL,
     start_time DATETIME NOT NULL,
     end_time DATETIME NOT NULL,
     remark TEXT,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `mes_data` (
     mes_data_id	BIGINT AUTO_INCREMENT PRIMARY KEY,
     power_consumption	DECIMAL(10, 2)	NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE `line` (
    `line_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
    `factory_id`	BIGINT(20)	NOT NULL,
    `user_id`	BIGINT(20)	NOT NULL	COMMENT '반장',
    `line_code`	VARCHAR(32)	NOT NULL,
    `line_name`	VARCHAR(32)	NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_active`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE `lot_no` (
      `Lot_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
      `item_id`	BIGINT(20)	NOT NULL,
      `production_performance_id`	BIGINT(20)	NOT NULL	COMMENT '로트수량',
      `production_plan_id`	BIGINT(20)	NOT NULL,
      `lot_no`	VARCHAR(32)	NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE log (
     log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     action_type ENUM('CREATE','UPDATE','DELETE') NOT NULL,
     entity_name VARCHAR(64) NOT NULL,
     entity_id BIGINT NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `item_line_crossed_table` (
   `item_line_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
   `line_id`	BIGINT(20)	NOT NULL,
   `item_id`	BIGINT(20)	NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `equipment` (
     `equipment_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
     `line_id`	BIGINT(20)	NOT NULL,
     `equipment_status_id`	BIGINT(20)	NOT NULL,
     `user_id`	BIGINT(20)	NOT NULL,
     `equipment_code`	VARCHAR(32)	NOT NULL,
     `equipment_name`	VARCHAR(32)	NOT NULL,
     `equipment_type`	VARCHAR(32)	NOT NULL,
     `operating_time`	TIMESTAMP	NOT NULL,
     `maintenance_history`	TIMESTAMP	NULL	COMMENT '제일 최근 날짜',
     `equipment_ppm`	DECIMAL(10,2)	NOT NULL,
     `total_count`	DECIMAL(10,2)	NOT NULL,
     `defective_count`	DECIMAL(10,2)	NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     `is_active`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE `equipment_status` (
    `equipment_status_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
    `equipment_status_code`	VARCHAR(32)	NOT NULL,
    `equipment_status_name`	VARCHAR(32)	NOT NULL,
    `description`	VARCHAR(256)	NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `alarm` (
     `alarm_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
     `equipment_id`	BIGINT(20)	NOT NULL,
     `user_id`	BIGINT(20)	NOT NULL	COMMENT '처리자',
     `alarm_code`	VARCHAR(32)	NOT NULL,
     `alarm_name`	VARCHAR(32)	NOT NULL,
     `alarm_type`	VARCHAR(32)	NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     `alarm_cause`	TEXT	NOT NULL
);



CREATE TABLE user (
      user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
      emp_no VARCHAR(32) NOT NULL COMMENT '입사연도(4)월(2)순번(3)',
      user_name VARCHAR(32) NOT NULL,
      user_email VARCHAR(32) NOT NULL,
      user_password VARCHAR(64) NOT NULL,
      user_phone_number VARCHAR(32) NOT NULL,
      user_hired_date DATE NOT NULL,
      user_termination_date DATE NULL,
      user_role ENUM('USER','MANAGER','ADMIN') NOT NULL DEFAULT 'USER',
      user_status ENUM('ACTIVE','LEAVE','RESIGNED') NOT NULL DEFAULT 'ACTIVE',
      user_department VARCHAR(32) NOT NULL,
      user_position ENUM('ASSISTANT','ASSISTANT_MANAGER','MANAGER','GENERAL_MANAGER','DIRECTOR','CEO') NOT NULL DEFAULT 'ASSISTANT',
      user_address VARCHAR(64) NOT NULL,
      user_extension VARCHAR(32) NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `factory` (
   `factory_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
   `user_id`	BIGINT(20)	NOT NULL	COMMENT '공장장',
   `factory_code`	VARCHAR(32)	NOT NULL,
   `factory_name`	VARCHAR(32)	NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   `is_active`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE item (
      item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
      item_code VARCHAR(32) NOT NULL,
      item_name VARCHAR(32) NOT NULL,
      item_specification VARCHAR(32) NULL,
      item_unit VARCHAR(32) NOT NULL,
      item_status ENUM('RAW_MATERIAL', 'AUXILIARY_MATERIAL', 'SEMI_FINISHED_PRODUCT', 'FINISHED_PRODUCT')
          NOT NULL COMMENT '원재료, 부재료, 반제품, 완제품',
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_active BOOLEAN NOT NULL DEFAULT TRUE
);


CREATE TABLE `item_serial` (
       `item_serial_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
       `item_id`	BIGINT(20)	NOT NULL,
       `lot_id`	BIGINT(20)	NOT NULL,
       `item_serial_no`	VARCHAR(32)	NOT NULL	COMMENT 'UUID 예) E28068940000400A3B2C1D45'
);

CREATE TABLE `defective` (
     `defective_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
     `equipment_id`	BIGINT(20)	NOT NULL,
     `document_no`	VARCHAR(32)	NOT NULL,
     `defective_code`	VARCHAR(32)	NOT NULL,
     `defective_name`	VARCHAR(32)	NOT NULL,
     `defective_qty`	DECIMAL(10,2)	NOT NULL	COMMENT '설비  누적 불량 수량',
     `defective_status`	VARCHAR(32)	NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE `performance_defective_crossed_table` (
   `performance_defective_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
   `production_performance_id`	BIGINT(20)	NOT NULL,
   `defective_id`	BIGINT(20)	NOT NULL,
   `defective_qty`	DECIMAL(10,2)	NOT NULL,
   `defective_rate`	DECIMAL(10, 2)	NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `process` (
       `process_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
       `equipment_id`	BIGINT(20)	NOT NULL,
       `user_id`	BIGINT(20)	NOT NULL,
       `process_code`	VARCHAR(32)	NOT NULL,
       `process_name`	VARCHAR(32)	NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       `is_active`	BOOLEAN	NOT NULL	DEFAULT TRUE
);
