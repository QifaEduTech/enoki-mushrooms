/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : damai_ai

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 04/08/2025 22:07:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for d_chat_type_history
-- ----------------------------
DROP TABLE IF EXISTS `d_chat_type_history`;
CREATE TABLE `d_chat_type_history`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                        `type` int NOT NULL COMMENT '会话类型，详见ChatType枚举',
                                        `chat_id` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话id',
                                        `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
                                        `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                        `edit_time` datetime NULL DEFAULT NULL COMMENT '编辑时间',
                                        `status` tinyint NULL DEFAULT 1 COMMENT '1:正常 0:删除',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of d_chat_type_history
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                   `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                   `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                   `user_id` bigint NULL DEFAULT NULL,
                                   `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                   `status` tinyint NULL DEFAULT 1,
                                   `top_k` int NULL DEFAULT NULL COMMENT '推荐召回topK',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_base
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_chunk
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_chunk`;
CREATE TABLE `knowledge_chunk`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `kb_id` bigint NOT NULL,
                                    `chunk_index` int NULL DEFAULT NULL,
                                    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                                    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `kb_id`(`kb_id`) USING BTREE,
                                    CONSTRAINT `knowledge_chunk_ibfk_1` FOREIGN KEY (`kb_id`) REFERENCES `knowledge_base` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 184 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_chunk
-- ----------------------------

-- ----------------------------
-- Table structure for spring_ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `spring_ai_chat_memory`;
CREATE TABLE `spring_ai_chat_memory`  (
                                          `conversation_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                          `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                          `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                          `timestamp` timestamp NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spring_ai_chat_memory
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
