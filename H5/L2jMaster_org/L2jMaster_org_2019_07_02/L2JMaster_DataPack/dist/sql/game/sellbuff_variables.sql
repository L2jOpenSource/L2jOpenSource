/*
Navicat MySQL Data Transfer

Source Server         : L2Dev
Source Server Version : 50715
Source Host           : localhost:3306
Source Database       : l2jmaster

Target Server Type    : MYSQL
Target Server Version : 50715
File Encoding         : 65001

Date: 2017-03-27 20:45:29
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `sellbuff_variables`
-- ----------------------------
DROP TABLE IF EXISTS `sellbuff_variables`;
CREATE TABLE `sellbuff_variables` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `original_name_color` int(10) unsigned DEFAULT NULL,
  `original_title` varchar(21) DEFAULT NULL,
  `original_title_color` int(10) unsigned DEFAULT NULL,
  `price` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sellbuff_variables
-- ----------------------------
