-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: pet_medical_mall
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_consultation_message`
--
create database if not exists `pet_medical_mall` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `pet_medical_mall`;
DROP TABLE IF EXISTS `ai_consultation_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_consultation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `role` enum('USER','ASSISTANT','SYSTEM') NOT NULL,
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_msg_session` (`session_id`),
  CONSTRAINT `fk_ai_msg_session` FOREIGN KEY (`session_id`) REFERENCES `ai_consultation_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_consultation_message`
--

LOCK TABLES `ai_consultation_message` WRITE;
/*!40000 ALTER TABLE `ai_consultation_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_consultation_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_consultation_session`
--

DROP TABLE IF EXISTS `ai_consultation_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_consultation_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `model_name` varchar(50) NOT NULL,
  `emergency_level` enum('LOW','MEDIUM','HIGH') DEFAULT NULL,
  `summary` text,
  `disclaimer` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_user` (`user_id`),
  KEY `idx_ai_pet` (`pet_id`),
  CONSTRAINT `fk_ai_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_ai_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_consultation_session`
--

LOCK TABLES `ai_consultation_session` WRITE;
/*!40000 ALTER TABLE `ai_consultation_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_consultation_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug`
--

DROP TABLE IF EXISTS `drug`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drug` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `drug_code` varchar(50) NOT NULL,
  `name` varchar(120) NOT NULL,
  `category` varchar(60) DEFAULT NULL,
  `is_rx` tinyint(1) NOT NULL DEFAULT '0',
  `indication` text,
  `dosage_instruction` text,
  `contraindication` text,
  `price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` enum('ON_SALE','OFF_SHELF') NOT NULL DEFAULT 'ON_SALE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `drug_code` (`drug_code`),
  KEY `idx_drug_name` (`name`),
  KEY `idx_drug_rx` (`is_rx`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug`
--

LOCK TABLES `drug` WRITE;
/*!40000 ALTER TABLE `drug` DISABLE KEYS */;
INSERT INTO `drug` VALUES (1,'OTC-PROBIO-001','宠物益生菌','肠胃调理',0,'轻度腹泻、肠胃不适','每日一次，随餐','严重脱水需及时就医',45.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(2,'RX-AMOX-001','阿莫西林宠物专用片','抗感染',1,'细菌感染','遵兽医处方','对青霉素过敏禁用',88.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(3,'RX-SKIN-003','皮肤炎症治疗喷剂','皮肤病',1,'皮肤炎症','每日2次，外用','破损严重皮肤慎用',125.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(4,'OTC-EYE-002','宠物亮目滴眼液','眼科护理',0,'眼睛红肿、分泌物多','每日2-3次，每次1-2滴','角膜溃疡慎用',28.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(5,'OTC-EAR-003','宠物净耳液','耳道护理',0,'耳垢多、耳部异味','每周1-2次','耳膜穿孔禁用',35.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(6,'OTC-JOINT-004','氨基葡萄糖强骨片','关节保健',0,'关节磨损、行动不便','按体重服用','无',120.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(7,'RX-FLEA-005','大宠爱体内外驱虫滴剂','驱虫药',1,'跳蚤、蜱虫、心丝虫','每月一次，外用','生病或体弱宠物慎用',168.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(8,'RX-WORM-006','海乐妙体内驱虫片','驱虫药',1,'蛔虫、钩虫、绦虫','每季度一次','严重肝肾功能不全慎用',65.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(9,'OTC-CAL-007','宠物高钙补钙片','营养补剂',0,'成长期补钙、幼宠发育','随餐服用','结石体质慎用',42.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(10,'OTC-VIT-008','复合维生素营养颗粒','营养补剂',0,'缺乏维生素引起的掉毛、皮屑','每日一次','无',48.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(11,'OTC-COUGH-009','宠物止咳平喘糖浆','呼吸道',0,'咳嗽、气喘','每日3次','严重肺炎需就医',32.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(12,'OTC-WOUND-010','聚维酮碘消毒液','外用药',0,'伤口消毒、抗真菌','外用喷涂','避免舔食',15.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(13,'OTC-GEL-011','高能营养营养膏','营养补剂',0,'术后恢复、体弱多病','直接喂食或拌食','糖尿病宠物慎用',55.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(14,'OTC-URI-012','泌尿系统养护粉','泌尿系统',0,'尿频、尿路不畅','每日两次','结石手术后恢复期',78.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(15,'RX-INFLA-013','美洛昔康消炎止痛药','消炎止痛',1,'术后镇痛、关节炎痛','严格遵医嘱','胃溃疡宠物禁用',95.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(16,'OTC-DIAR-014','蒙脱石散宠物版','肠胃调理',0,'急性腹泻、拉稀','兑水服用','便秘期间停用',22.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(17,'OTC-RECO-015','术后高能恢复罐头','营养食品',0,'手术后体力恢复','全价干粮替代','无',25.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(18,'OTC-HAIR-016','宠物化毛膏','肠胃调理',0,'毛球症、排毛不畅','每周2-3次','无',38.00,'ON_SALE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(19,'0001','益生菌','抗感染',0,'益生菌','test','test',10.00,'ON_SALE','2026-03-27 20:51:10','2026-03-27 20:51:10');
/*!40000 ALTER TABLE `drug` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug_inventory`
--

DROP TABLE IF EXISTS `drug_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drug_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `drug_id` bigint NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `safety_stock` int NOT NULL DEFAULT '10',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `drug_id` (`drug_id`),
  CONSTRAINT `fk_inventory_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug_inventory`
--

LOCK TABLES `drug_inventory` WRITE;
/*!40000 ALTER TABLE `drug_inventory` DISABLE KEYS */;
INSERT INTO `drug_inventory` VALUES (1,4,144,30,'2026-03-25 21:10:13'),(2,5,194,40,'2026-03-25 21:10:13'),(3,6,97,20,'2026-03-25 21:10:13'),(4,7,50,10,'2026-03-25 21:10:13'),(5,8,88,15,'2026-03-25 21:10:13'),(6,9,297,50,'2026-03-25 21:10:13'),(7,10,400,60,'2026-03-25 21:10:13'),(8,11,70,10,'2026-03-25 21:10:13'),(9,12,120,20,'2026-03-25 21:10:13'),(10,13,250,40,'2026-03-25 21:10:13'),(11,14,180,30,'2026-03-25 21:10:13'),(12,15,58,10,'2026-03-25 21:10:13'),(13,16,500,100,'2026-03-25 21:10:13'),(14,17,100,20,'2026-03-25 21:10:13'),(15,18,300,50,'2026-03-25 21:10:13'),(16,19,0,10,'2026-03-27 20:51:09');
/*!40000 ALTER TABLE `drug_inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logistics_record`
--

DROP TABLE IF EXISTS `logistics_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `tracking_no` varchar(100) NOT NULL,
  `logistics_status` varchar(50) NOT NULL DEFAULT 'PENDING',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  CONSTRAINT `fk_logistics_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logistics_record`
--

LOCK TABLES `logistics_record` WRITE;
/*!40000 ALTER TABLE `logistics_record` DISABLE KEYS */;
INSERT INTO `logistics_record` VALUES (1,5,'顺丰速运','SF1774620154356','IN_TRANSIT','2026-03-27 22:02:34');
/*!40000 ALTER TABLE `logistics_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `online_consultation`
--

DROP TABLE IF EXISTS `online_consultation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `online_consultation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `consultation_no` varchar(40) NOT NULL,
  `user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `vet_user_id` bigint DEFAULT NULL,
  `ai_session_id` bigint DEFAULT NULL,
  `chief_complaint` text NOT NULL,
  `status` enum('WAITING_VET','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'WAITING_VET',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `consultation_no` (`consultation_no`),
  KEY `idx_consult_user` (`user_id`),
  KEY `idx_consult_pet` (`pet_id`),
  KEY `idx_consult_vet` (`vet_user_id`),
  KEY `fk_consult_ai` (`ai_session_id`),
  CONSTRAINT `fk_consult_ai` FOREIGN KEY (`ai_session_id`) REFERENCES `ai_consultation_session` (`id`),
  CONSTRAINT `fk_consult_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_consult_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_consult_vet` FOREIGN KEY (`vet_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `online_consultation`
--

LOCK TABLES `online_consultation` WRITE;
/*!40000 ALTER TABLE `online_consultation` DISABLE KEYS */;
INSERT INTO `online_consultation` VALUES (1,'CONS1774446145170',3,1,NULL,NULL,'皮肤大面积红疹，伴随剧烈瘙痒，食欲略有下降','WAITING_VET','2026-03-25 21:42:25','2026-03-25 21:42:25'),(2,'CONS1774446966349',3,1,2,NULL,'皮肤大面积红疹，伴随剧烈瘙痒，食欲略有下降','COMPLETED','2026-03-25 21:56:06','2026-03-25 21:56:06'),(3,'CONS1774603116324',3,1,NULL,NULL,'皮肤大面积红疹，伴随剧烈瘙痒，食欲略有下降','WAITING_VET','2026-03-27 17:18:36','2026-03-27 17:18:36'),(4,'CONS1774616004186',4,4,2,NULL,'皮肤大面积红疹，伴随剧烈瘙痒，食欲略有下降','COMPLETED','2026-03-27 20:53:24','2026-03-27 20:53:24'),(5,'CONS1774757466517',1,5,2,NULL,'过敏了','COMPLETED','2026-03-29 12:11:06','2026-03-29 12:11:06'),(6,'CONS1774759924978',6,6,2,NULL,'轻微感染','COMPLETED','2026-03-29 12:52:04','2026-03-29 12:52:04'),(7,'CONS1774834878318',4,5,9,NULL,'test','COMPLETED','2026-03-30 09:41:18','2026-03-30 09:41:18');
/*!40000 ALTER TABLE `online_consultation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(40) NOT NULL,
  `user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `prescription_id` bigint DEFAULT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `order_status` enum('PENDING_PAY','PAID','SHIPPED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING_PAY',
  `payment_status` enum('UNPAID','PAID','REFUNDED') NOT NULL DEFAULT 'UNPAID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `fk_order_pet` (`pet_id`),
  KEY `fk_order_pre` (`prescription_id`),
  CONSTRAINT `fk_order_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_order_pre` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'ORD1774447093125',3,1,2,594.00,'COMPLETED','PAID','2026-03-25 21:58:13','2026-03-25 21:58:13'),(2,'ORD1774450212754',3,1,2,616.00,'COMPLETED','PAID','2026-03-25 22:50:12','2026-03-25 22:50:12'),(3,'ORD1774450792839',3,1,2,358.00,'COMPLETED','PAID','2026-03-25 22:59:52','2026-03-25 22:59:52'),(4,'ORD1774450866666',3,1,1,258.00,'COMPLETED','PAID','2026-03-25 23:01:06','2026-03-25 23:01:06'),(5,'ORD1774620137837',4,4,3,336.00,'SHIPPED','PAID','2026-03-27 22:02:17','2026-03-27 22:02:17'),(6,'ORD1774622791125',4,4,3,248.00,'COMPLETED','PAID','2026-03-27 22:46:31','2026-03-27 22:46:31'),(7,'ORD1774757739417',1,5,4,88.00,'PAID','PAID','2026-03-29 12:15:39','2026-03-29 12:15:39'),(8,'ORD1774759985501',6,6,5,213.00,'PAID','PAID','2026-03-29 12:53:05','2026-03-29 12:53:05');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `drug_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `is_rx` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_order_item_order` (`order_id`),
  KEY `fk_order_item_drug` (`drug_id`),
  CONSTRAINT `fk_order_item_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,3,45.00,0,'2026-03-25 21:58:13'),(2,1,2,1,88.00,1,'2026-03-25 21:58:13'),(3,1,3,1,125.00,1,'2026-03-25 21:58:13'),(4,1,4,2,28.00,0,'2026-03-25 21:58:13'),(5,1,5,2,35.00,0,'2026-03-25 21:58:13'),(6,1,6,1,120.00,0,'2026-03-25 21:58:13'),(7,2,1,3,45.00,0,'2026-03-25 22:50:12'),(8,2,2,1,88.00,1,'2026-03-25 22:50:12'),(9,2,4,3,28.00,0,'2026-03-25 22:50:12'),(10,2,5,3,35.00,0,'2026-03-25 22:50:12'),(11,2,6,1,120.00,0,'2026-03-25 22:50:12'),(12,2,9,2,42.00,0,'2026-03-25 22:50:12'),(13,3,1,1,45.00,0,'2026-03-25 22:59:52'),(14,3,2,1,88.00,1,'2026-03-25 22:59:52'),(15,3,4,1,28.00,0,'2026-03-25 22:59:52'),(16,3,5,1,35.00,0,'2026-03-25 22:59:52'),(17,3,6,1,120.00,0,'2026-03-25 22:59:52'),(18,3,9,1,42.00,0,'2026-03-25 22:59:52'),(19,4,1,1,45.00,0,'2026-03-25 23:01:06'),(20,4,2,1,88.00,1,'2026-03-25 23:01:06'),(21,4,3,1,125.00,1,'2026-03-25 23:01:06'),(22,5,2,2,88.00,1,'2026-03-27 22:02:17'),(23,5,8,1,65.00,1,'2026-03-27 22:02:17'),(24,5,15,1,95.00,1,'2026-03-27 22:02:17'),(25,6,2,1,88.00,1,'2026-03-27 22:46:31'),(26,6,8,1,65.00,1,'2026-03-27 22:46:31'),(27,6,15,1,95.00,1,'2026-03-27 22:46:31'),(28,7,2,1,88.00,1,'2026-03-29 12:15:39'),(29,8,2,1,88.00,1,'2026-03-29 12:53:05'),(30,8,3,1,125.00,1,'2026-03-29 12:53:05');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet`
--

DROP TABLE IF EXISTS `pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_user_id` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `species` varchar(30) NOT NULL,
  `breed` varchar(50) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','UNKNOWN') DEFAULT 'UNKNOWN',
  `birth_date` date DEFAULT NULL,
  `weight_kg` decimal(6,2) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_owner` (`owner_user_id`),
  CONSTRAINT `fk_pet_owner` FOREIGN KEY (`owner_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet`
--

LOCK TABLES `pet` WRITE;
/*!40000 ALTER TABLE `pet` DISABLE KEYS */;
INSERT INTO `pet` VALUES (1,3,'可可','猫','英短','FEMALE','2022-06-01',4.20,'2026-03-25 21:10:13','2026-03-25 21:10:13'),(2,4,'小白','狗','柯基','MALE','2023-01-01',8.20,'2026-03-25 22:26:04','2026-03-29 12:48:44'),(3,4,'小白','狗','柯基','MALE','2023-01-01',8.20,'2026-03-27 17:17:20','2026-03-27 17:17:20'),(4,4,'小白','狗','边牧','MALE','2024-03-01',10.00,'2026-03-27 20:53:03','2026-03-29 12:48:44'),(5,4,'小红','猫','橘猫','MALE','2025-03-01',10.00,'2026-03-29 12:09:09','2026-03-29 12:48:28'),(6,6,'咪咪','猫','橘猫','MALE','2025-03-01',10.00,'2026-03-29 12:50:41','2026-03-29 12:50:41'),(7,4,'','','','MALE',NULL,0.00,'2026-03-30 09:40:58','2026-03-30 09:40:58');
/*!40000 ALTER TABLE `pet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_health_record`
--

DROP TABLE IF EXISTS `pet_health_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_health_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pet_id` bigint NOT NULL,
  `allergies` text,
  `chronic_diseases` text,
  `vaccine_notes` text,
  `medication_notes` text,
  `updated_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pet_id` (`pet_id`),
  KEY `fk_health_user` (`updated_by`),
  CONSTRAINT `fk_health_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_health_user` FOREIGN KEY (`updated_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_health_record`
--

LOCK TABLES `pet_health_record` WRITE;
/*!40000 ALTER TABLE `pet_health_record` DISABLE KEYS */;
INSERT INTO `pet_health_record` VALUES (1,1,'青霉素轻微过敏','无','猫三联已完成','曾使用益生菌调理肠胃',3,'2026-03-25 21:10:13','2026-03-25 21:10:13'),(2,2,'海鲜','心脏病','3针','消炎',1,'2026-03-27 17:31:16','2026-03-27 17:31:16'),(3,4,'海鲜','没有','三针','没有',1,'2026-03-27 20:53:03','2026-03-27 20:53:03'),(4,5,'海鲜','猫藓','3针','没有',1,'2026-03-29 12:09:09','2026-03-29 12:09:09'),(5,6,'海鲜','没有病史','4针','没有治疗',6,'2026-03-29 12:50:41','2026-03-29 12:50:41'),(6,7,'test','test','3针','test',4,'2026-03-30 09:40:58','2026-03-30 09:40:58');
/*!40000 ALTER TABLE `pet_health_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription`
--

DROP TABLE IF EXISTS `prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_no` varchar(40) NOT NULL,
  `consultation_id` bigint NOT NULL,
  `vet_user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `diagnosis` text NOT NULL,
  `status` enum('ISSUED','APPROVED','REJECTED','EXPIRED','USED') NOT NULL DEFAULT 'ISSUED',
  `valid_from` datetime NOT NULL,
  `valid_until` datetime NOT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `prescription_no` (`prescription_no`),
  KEY `idx_pre_consult` (`consultation_id`),
  KEY `idx_pre_pet` (`pet_id`),
  KEY `fk_pre_vet` (`vet_user_id`),
  KEY `fk_pre_reviewer` (`reviewed_by`),
  CONSTRAINT `fk_pre_consult` FOREIGN KEY (`consultation_id`) REFERENCES `online_consultation` (`id`),
  CONSTRAINT `fk_pre_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_pre_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_pre_vet` FOREIGN KEY (`vet_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription`
--

LOCK TABLES `prescription` WRITE;
/*!40000 ALTER TABLE `prescription` DISABLE KEYS */;
INSERT INTO `prescription` VALUES (1,'PREa57af823',2,2,1,'真菌性皮肤病伴细菌二次感染','ISSUED','2026-03-25 21:57:06','2026-03-28 21:57:06',NULL,NULL,'2026-03-25 21:57:05','2026-03-25 21:57:05'),(2,'PREb77170b8',2,2,1,'真菌性皮肤病伴细菌二次感染','ISSUED','2026-03-25 21:57:30','2026-03-28 21:57:30',NULL,NULL,'2026-03-25 21:57:30','2026-03-25 21:57:30'),(3,'PRE0a30d3c3',4,2,4,'真菌性皮肤病伴细菌二次感染，可能后续会XXXX','ISSUED','2026-03-27 20:54:35','2026-03-30 20:54:35',NULL,NULL,'2026-03-27 20:54:35','2026-03-27 20:54:35'),(4,'PRE4f032fbd',5,2,5,'XXXX','ISSUED','2026-03-29 12:12:48','2026-04-01 12:12:48',NULL,NULL,'2026-03-29 12:12:48','2026-03-29 12:12:48'),(5,'PRE9b89462c',6,2,6,'轻微感染应该是XXXX引起的','ISSUED','2026-03-29 12:52:36','2026-04-01 12:52:36',NULL,NULL,'2026-03-29 12:52:36','2026-03-29 12:52:36'),(6,'PRE892282de',7,9,5,'test','ISSUED','2026-03-30 09:46:06','2026-04-02 09:46:06',NULL,NULL,'2026-03-30 09:46:06','2026-03-30 09:46:06');
/*!40000 ALTER TABLE `prescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription_audit_log`
--

DROP TABLE IF EXISTS `prescription_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint NOT NULL,
  `action_type` enum('AUTO_CHECK','MANUAL_REVIEW','STATUS_CHANGE') NOT NULL,
  `action_result` enum('PASS','REJECT') NOT NULL,
  `detail` varchar(500) DEFAULT NULL,
  `operator_user_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_audit_pre` (`prescription_id`),
  KEY `fk_audit_user` (`operator_user_id`),
  CONSTRAINT `fk_audit_pre` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`operator_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription_audit_log`
--

LOCK TABLES `prescription_audit_log` WRITE;
/*!40000 ALTER TABLE `prescription_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `prescription_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription_item`
--

DROP TABLE IF EXISTS `prescription_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint NOT NULL,
  `drug_id` bigint NOT NULL,
  `dosage` varchar(100) NOT NULL,
  `frequency` varchar(100) DEFAULT NULL,
  `duration_days` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pre_item_pre` (`prescription_id`),
  KEY `idx_pre_item_drug` (`drug_id`),
  CONSTRAINT `fk_pre_item_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`),
  CONSTRAINT `fk_pre_item_pre` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription_item`
--

LOCK TABLES `prescription_item` WRITE;
/*!40000 ALTER TABLE `prescription_item` DISABLE KEYS */;
INSERT INTO `prescription_item` VALUES (1,1,2,'遵兽医处方',NULL,NULL,1,'2026-03-25 21:57:06'),(2,1,3,'每日2次，外用',NULL,NULL,1,'2026-03-25 21:57:06'),(3,1,8,'每季度一次',NULL,NULL,1,'2026-03-25 21:57:06'),(4,2,2,'遵兽医处方',NULL,NULL,1,'2026-03-25 21:57:30'),(5,2,3,'每日2次，外用',NULL,NULL,1,'2026-03-25 21:57:30'),(6,2,8,'每季度一次',NULL,NULL,1,'2026-03-25 21:57:30'),(7,3,2,'遵兽医处方',NULL,NULL,1,'2026-03-27 20:54:35'),(8,3,8,'每季度一次',NULL,NULL,1,'2026-03-27 20:54:35'),(9,3,15,'看着来吃',NULL,NULL,1,'2026-03-27 20:54:35'),(10,4,2,'遵兽医处方',NULL,NULL,1,'2026-03-29 12:12:48'),(11,5,2,'遵兽医处方',NULL,NULL,1,'2026-03-29 12:52:36'),(12,5,3,'每日2次，外用',NULL,NULL,1,'2026-03-29 12:52:36'),(13,6,8,'每季度一次',NULL,NULL,1,'2026-03-30 09:46:06');
/*!40000 ALTER TABLE `prescription_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart_item`
--

DROP TABLE IF EXISTS `shopping_cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `drug_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_pet_drug` (`user_id`,`pet_id`,`drug_id`),
  KEY `fk_cart_pet` (`pet_id`),
  KEY `fk_cart_drug` (`drug_id`),
  CONSTRAINT `fk_cart_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`),
  CONSTRAINT `fk_cart_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart_item`
--

LOCK TABLES `shopping_cart_item` WRITE;
/*!40000 ALTER TABLE `shopping_cart_item` DISABLE KEYS */;
INSERT INTO `shopping_cart_item` VALUES (39,3,1,2,1,'2026-03-27 21:55:28','2026-03-27 21:55:28'),(40,3,1,8,1,'2026-03-27 21:55:30','2026-03-27 21:55:30'),(41,3,1,15,1,'2026-03-27 21:55:31','2026-03-27 21:55:31'),(47,1,4,2,1,'2026-03-29 12:14:52','2026-03-29 12:14:52');
/*!40000 ALTER TABLE `shopping_cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('USER','VET','PHARMACIST','ADMIN') NOT NULL DEFAULT 'USER',
  `status` enum('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$examplehash_admin','13800000000','ADMIN','ACTIVE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(2,'vet_zhang','$2a$10$examplehash_vet','13800000001','VET','ACTIVE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(3,'user_li','$2a$10$examplehash_user','13800000002','USER','ACTIVE','2026-03-25 21:10:13','2026-03-25 21:10:13'),(4,'user1','123456','1811111111','USER','ACTIVE','2026-03-25 21:38:03','2026-03-25 21:38:03'),(5,'user2','123456','18111111111','PHARMACIST','DISABLED','2026-03-27 20:32:51','2026-03-27 20:32:51'),(6,'用户1','123456','17811111111','USER','ACTIVE','2026-03-29 12:16:07','2026-03-29 12:16:07'),(8,'医生1','123456','181111111122','PHARMACIST','ACTIVE','2026-03-30 09:37:30','2026-03-30 09:37:30'),(9,'医生2','123456','1811111111222','VET','ACTIVE','2026-03-30 09:40:10','2026-03-30 09:40:10');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vet_profile`
--

DROP TABLE IF EXISTS `vet_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vet_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `real_name` varchar(50) NOT NULL,
  `license_no` varchar(100) NOT NULL,
  `hospital_name` varchar(120) DEFAULT NULL,
  `qualification_status` enum('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `approved_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `license_no` (`license_no`),
  CONSTRAINT `fk_vet_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vet_profile`
--

LOCK TABLES `vet_profile` WRITE;
/*!40000 ALTER TABLE `vet_profile` DISABLE KEYS */;
INSERT INTO `vet_profile` VALUES (1,2,'张医生','VET-LIC-2026001','安心宠物医院','APPROVED','2026-03-27 17:15:21','2026-03-25 21:10:13','2026-03-25 21:10:13');
/*!40000 ALTER TABLE `vet_profile` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-30  9:52:54
