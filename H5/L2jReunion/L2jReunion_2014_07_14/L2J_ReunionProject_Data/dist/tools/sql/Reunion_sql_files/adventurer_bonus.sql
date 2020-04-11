/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-05-01 00:17:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `adventurer_bonus`
-- ----------------------------
DROP TABLE IF EXISTS `adventurer_bonus`;
CREATE TABLE `adventurer_bonus` (
  `charId` int(10) unsigned NOT NULL,
  `advent_time` int(10) unsigned NOT NULL DEFAULT '0',
  `advent_points` int(10) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of adventurer_bonus
-- ----------------------------
