CREATE DATABASE IF NOT EXISTS pet_medical_mall DEFAULT CHARACTER SET utf8mb4;
USE pet_medical_mall;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(20) UNIQUE,
  role ENUM('USER','VET','PHARMACIST','ADMIN') NOT NULL DEFAULT 'USER',
  status ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

DROP TABLE IF EXISTS vet_profile;
CREATE TABLE vet_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  real_name VARCHAR(50) NOT NULL,
  license_no VARCHAR(100) NOT NULL UNIQUE,
  hospital_name VARCHAR(120),
  qualification_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  approved_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_vet_profile_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS pet;
CREATE TABLE pet (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  species VARCHAR(30) NOT NULL,
  breed VARCHAR(50),
  gender ENUM('MALE','FEMALE','UNKNOWN') DEFAULT 'UNKNOWN',
  birth_date DATE,
  weight_kg DECIMAL(6,2),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_pet_owner (owner_user_id),
  CONSTRAINT fk_pet_owner FOREIGN KEY (owner_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS pet_health_record;
CREATE TABLE pet_health_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL UNIQUE,
  allergies TEXT,
  chronic_diseases TEXT,
  vaccine_notes TEXT,
  medication_notes TEXT,
  updated_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_health_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
  CONSTRAINT fk_health_user FOREIGN KEY (updated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS ai_consultation_session;
CREATE TABLE ai_consultation_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  model_name VARCHAR(50) NOT NULL,
  emergency_level ENUM('LOW','MEDIUM','HIGH') DEFAULT NULL,
  summary TEXT,
  disclaimer TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ai_user (user_id),
  INDEX idx_ai_pet (pet_id),
  CONSTRAINT fk_ai_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_ai_pet FOREIGN KEY (pet_id) REFERENCES pet(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS ai_consultation_message;
CREATE TABLE ai_consultation_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  role ENUM('USER','ASSISTANT','SYSTEM') NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ai_msg_session (session_id),
  CONSTRAINT fk_ai_msg_session FOREIGN KEY (session_id) REFERENCES ai_consultation_session(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS online_consultation;
CREATE TABLE online_consultation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  consultation_no VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  vet_user_id BIGINT NULL,
  ai_session_id BIGINT NULL,
  chief_complaint TEXT NOT NULL,
  status ENUM('WAITING_VET','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'WAITING_VET',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_consult_user (user_id),
  INDEX idx_consult_pet (pet_id),
  INDEX idx_consult_vet (vet_user_id),
  CONSTRAINT fk_consult_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_consult_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
  CONSTRAINT fk_consult_vet FOREIGN KEY (vet_user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_consult_ai FOREIGN KEY (ai_session_id) REFERENCES ai_consultation_session(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS drug;
CREATE TABLE drug (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  drug_code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  category VARCHAR(60),
  is_rx TINYINT(1) NOT NULL DEFAULT 0,
  indication TEXT,
  dosage_instruction TEXT,
  contraindication TEXT,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status ENUM('ON_SALE','OFF_SHELF') NOT NULL DEFAULT 'ON_SALE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_drug_name (name),
  INDEX idx_drug_rx (is_rx)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS drug_inventory;
CREATE TABLE drug_inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  drug_id BIGINT NOT NULL UNIQUE,
  stock INT NOT NULL DEFAULT 0,
  safety_stock INT NOT NULL DEFAULT 10,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inventory_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS prescription;
CREATE TABLE prescription (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_no VARCHAR(40) NOT NULL UNIQUE,
  consultation_id BIGINT NOT NULL,
  vet_user_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  diagnosis TEXT NOT NULL,
  status ENUM('ISSUED','APPROVED','REJECTED','EXPIRED','USED') NOT NULL DEFAULT 'ISSUED',
  valid_from DATETIME NOT NULL,
  valid_until DATETIME NOT NULL,
  reviewed_by BIGINT,
  reviewed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_pre_consult (consultation_id),
  INDEX idx_pre_pet (pet_id),
  CONSTRAINT fk_pre_consult FOREIGN KEY (consultation_id) REFERENCES online_consultation(id),
  CONSTRAINT fk_pre_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
  CONSTRAINT fk_pre_vet FOREIGN KEY (vet_user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_pre_reviewer FOREIGN KEY (reviewed_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS prescription_item;
CREATE TABLE prescription_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  dosage VARCHAR(100) NOT NULL,
  frequency VARCHAR(100),
  duration_days INT,
  quantity INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_pre_item_pre (prescription_id),
  INDEX idx_pre_item_drug (drug_id),
  CONSTRAINT fk_pre_item_pre FOREIGN KEY (prescription_id) REFERENCES prescription(id),
  CONSTRAINT fk_pre_item_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS shopping_cart_item;
CREATE TABLE shopping_cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cart_user_pet_drug (user_id, pet_id, drug_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_cart_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
  CONSTRAINT fk_cart_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  prescription_id BIGINT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  order_status ENUM('PENDING_PAY','PAID','SHIPPED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING_PAY',
  payment_status ENUM('UNPAID','PAID','REFUNDED') NOT NULL DEFAULT 'UNPAID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_order_user (user_id),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_order_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
  CONSTRAINT fk_order_pre FOREIGN KEY (prescription_id) REFERENCES prescription(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  is_rx TINYINT(1) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order`(id),
  CONSTRAINT fk_order_item_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS logistics_record;
CREATE TABLE logistics_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  company_name VARCHAR(100) NOT NULL,
  tracking_no VARCHAR(100) NOT NULL,
  logistics_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_logistics_order FOREIGN KEY (order_id) REFERENCES `order`(id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS prescription_audit_log;
CREATE TABLE prescription_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  action_type ENUM('AUTO_CHECK','MANUAL_REVIEW','STATUS_CHANGE') NOT NULL,
  action_result ENUM('PASS','REJECT') NOT NULL,
  detail VARCHAR(500),
  operator_user_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_pre FOREIGN KEY (prescription_id) REFERENCES prescription(id),
  CONSTRAINT fk_audit_user FOREIGN KEY (operator_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

INSERT INTO sys_user (username, password_hash, phone, role, status) VALUES
('admin', '$2a$10$examplehash_admin', '13800000000', 'ADMIN', 'ACTIVE'),
('vet_zhang', '$2a$10$examplehash_vet', '13800000001', 'VET', 'ACTIVE'),
('user_li', '$2a$10$examplehash_user', '13800000002', 'USER', 'ACTIVE');

INSERT INTO vet_profile (user_id, real_name, license_no, hospital_name, qualification_status, approved_at)
VALUES (2, '张医生', 'VET-LIC-2026001', '安心宠物医院', 'APPROVED', NOW());

INSERT INTO pet (owner_user_id, name, species, breed, gender, birth_date, weight_kg)
VALUES (3, '可可', '猫', '英短', 'FEMALE', '2022-06-01', 4.20);

INSERT INTO pet_health_record (pet_id, allergies, chronic_diseases, vaccine_notes, medication_notes, updated_by)
VALUES (1, '青霉素轻微过敏', '无', '猫三联已完成', '曾使用益生菌调理肠胃', 3);

INSERT INTO drug (drug_code, name, category, is_rx, indication, dosage_instruction, contraindication, price, status) VALUES
('OTC-PROBIO-001', '宠物益生菌', '肠胃调理', 0, '轻度腹泻、肠胃不适', '每日一次，随餐', '严重脱水需及时就医', 45.00, 'ON_SALE'),
('RX-AMOX-001', '阿莫西林宠物专用片', '抗感染', 1, '细菌感染', '遵兽医处方', '对青霉素过敏禁用', 88.00, 'ON_SALE'),
('RX-SKIN-003', '皮肤炎症治疗喷剂', '皮肤病', 1, '皮肤炎症', '每日2次，外用', '破损严重皮肤慎用', 125.00, 'ON_SALE'),
('OTC-EYE-002', '宠物亮目滴眼液', '眼科护理', 0, '眼睛红肿、分泌物多', '每日2-3次，每次1-2滴', '角膜溃疡慎用', 28.00, 'ON_SALE'),
('OTC-EAR-003', '宠物净耳液', '耳道护理', 0, '耳垢多、耳部异味', '每周1-2次', '耳膜穿孔禁用', 35.00, 'ON_SALE'),
('OTC-JOINT-004', '氨基葡萄糖强骨片', '关节保健', 0, '关节磨损、行动不便', '按体重服用', '无', 120.00, 'ON_SALE'),
('RX-FLEA-005', '大宠爱体内外驱虫滴剂', '驱虫药', 1, '跳蚤、蜱虫、心丝虫', '每月一次，外用', '生病或体弱宠物慎用', 168.00, 'ON_SALE'),
('RX-WORM-006', '海乐妙体内驱虫片', '驱虫药', 1, '蛔虫、钩虫、绦虫', '每季度一次', '严重肝肾功能不全慎用', 65.00, 'ON_SALE'),
('OTC-CAL-007', '宠物高钙补钙片', '营养补剂', 0, '成长期补钙、幼宠发育', '随餐服用', '结石体质慎用', 42.00, 'ON_SALE'),
('OTC-VIT-008', '复合维生素营养颗粒', '营养补剂', 0, '缺乏维生素引起的掉毛、皮屑', '每日一次', '无', 48.00, 'ON_SALE'),
('OTC-COUGH-009', '宠物止咳平喘糖浆', '呼吸道', 0, '咳嗽、气喘', '每日3次', '严重肺炎需就医', 32.00, 'ON_SALE'),
('OTC-WOUND-010', '聚维酮碘消毒液', '外用药', 0, '伤口消毒、抗真菌', '外用喷涂', '避免舔食', 15.00, 'ON_SALE'),
('OTC-GEL-011', '高能营养营养膏', '营养补剂', 0, '术后恢复、体弱多病', '直接喂食或拌食', '糖尿病宠物慎用', 55.00, 'ON_SALE'),
('OTC-URI-012', '泌尿系统养护粉', '泌尿系统', 0, '尿频、尿路不畅', '每日两次', '结石手术后恢复期', 78.00, 'ON_SALE'),
('RX-INFLA-013', '美洛昔康消炎止痛药', '消炎止痛', 1, '术后镇痛、关节炎痛', '严格遵医嘱', '胃溃疡宠物禁用', 95.00, 'ON_SALE'),
('OTC-DIAR-014', '蒙脱石散宠物版', '肠胃调理', 0, '急性腹泻、拉稀', '兑水服用', '便秘期间停用', 22.00, 'ON_SALE'),
('OTC-RECO-015', '术后高能恢复罐头', '营养食品', 0, '手术后体力恢复', '全价干粮替代', '无', 25.00, 'ON_SALE'),
('OTC-HAIR-016', '宠物化毛膏', '肠胃调理', 0, '毛球症、排毛不畅', '每周2-3次', '无', 38.00, 'ON_SALE');

INSERT INTO drug_inventory (drug_id, stock, safety_stock) VALUES
 (4, 150, 30), (5, 200, 40),
(6, 100, 20), (7, 50, 10), (8, 90, 15), (9, 300, 50), (10, 400, 60),
(11, 70, 10), (12, 120, 20), (13, 250, 40), (14, 180, 30), (15, 60, 10),
(16, 500, 100), (17, 100, 20), (18, 300, 50);

SET FOREIGN_KEY_CHECKS = 1;
