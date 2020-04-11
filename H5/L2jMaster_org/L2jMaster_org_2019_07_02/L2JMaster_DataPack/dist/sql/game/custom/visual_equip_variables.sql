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
-- Table structure for `visual_equip_variables`
-- ----------------------------
DROP TABLE IF EXISTS `visual_equip_variables`;
CREATE TABLE `visual_equip_variables` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `type_ve` varchar(35) NOT NULL DEFAULT 'type',
  `dressme_command` varchar(35) NOT NULL DEFAULT 'comand',
  PRIMARY KEY (`charId`,`type_ve`,`dressme_command`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of visual_equip_variables
-- ----------------------------
