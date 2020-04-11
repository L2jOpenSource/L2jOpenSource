/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-07-25 22:20:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `custom_npc_elementals`
-- ----------------------------
DROP TABLE IF EXISTS `custom_npc_elementals`;
CREATE TABLE `custom_npc_elementals` (
  `npc_id` decimal(11,0) NOT NULL DEFAULT '0',
  `elemAtkType` tinyint(1) NOT NULL DEFAULT '-1',
  `elemAtkValue` int(3) NOT NULL DEFAULT '0',
  `fireDefValue` int(3) NOT NULL DEFAULT '0',
  `waterDefValue` int(3) NOT NULL DEFAULT '0',
  `windDefValue` int(3) NOT NULL DEFAULT '0',
  `earthDefValue` int(3) NOT NULL DEFAULT '0',
  `holyDefValue` int(3) NOT NULL DEFAULT '0',
  `darkDefValue` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`npc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of custom_npc_elementals
-- ----------------------------
INSERT INTO `custom_npc_elementals` VALUES ('50007', '0', '0', '20', '20', '20', '20', '20', '20');
INSERT INTO `custom_npc_elementals` VALUES ('70010', '0', '0', '20', '20', '20', '20', '20', '20');
