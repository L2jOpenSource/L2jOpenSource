/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-05-01 00:16:11
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `characters_premium`
-- ----------------------------
DROP TABLE IF EXISTS `characters_premium`;
CREATE TABLE `characters_premium` (
  `account_name` varchar(45) NOT NULL DEFAULT '',
  `premium_service` int(1) NOT NULL DEFAULT '0',
  `enddate` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of characters_premium
-- ----------------------------
