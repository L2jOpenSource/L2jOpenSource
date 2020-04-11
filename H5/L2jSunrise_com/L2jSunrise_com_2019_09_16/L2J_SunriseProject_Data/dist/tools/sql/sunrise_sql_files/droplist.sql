/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jsunrisegs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-07-25 22:25:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `custom_droplist`
-- ----------------------------
DROP TABLE IF EXISTS `custom_droplist`;
CREATE TABLE `custom_droplist` (
  `mobId` smallint(5) unsigned NOT NULL DEFAULT '0',
  `itemId` smallint(5) unsigned NOT NULL DEFAULT '0',
  `min` int(8) unsigned NOT NULL DEFAULT '0',
  `max` int(8) unsigned NOT NULL DEFAULT '0',
  `category` smallint(3) NOT NULL DEFAULT '0',
  `chance` mediumint(7) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mobId`,`itemId`,`category`),
  KEY `key_mobId` (`mobId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of custom_droplist
-- ----------------------------
INSERT INTO `custom_droplist` VALUES ('565', '6570', '1', '1', '2', '138633');
INSERT INTO `custom_droplist` VALUES ('565', '6578', '1', '1', '2', '55453');
INSERT INTO `custom_droplist` VALUES ('565', '10223', '1', '1', '1', '4963');
INSERT INTO `custom_droplist` VALUES ('565', '15698', '1', '1', '0', '10163');
INSERT INTO `custom_droplist` VALUES ('565', '15701', '1', '1', '0', '16261');
