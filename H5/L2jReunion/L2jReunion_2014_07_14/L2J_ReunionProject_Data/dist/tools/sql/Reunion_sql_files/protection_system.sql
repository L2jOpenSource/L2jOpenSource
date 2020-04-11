/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-04-29 16:16:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `hwid_bans`
-- ----------------------------
DROP TABLE IF EXISTS `hwid_bans`;
CREATE TABLE `hwid_bans` (
  `HWID` varchar(32) DEFAULT NULL,
  `expiretime` int(11) NOT NULL DEFAULT '0',
  `comments` varchar(255) DEFAULT NULL,
  UNIQUE KEY `HWID` (`HWID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of hwid_bans
-- ----------------------------

-- ----------------------------
-- Table structure for `hwid_info`
-- ----------------------------
DROP TABLE IF EXISTS `hwid_info`;
CREATE TABLE `hwid_info` (
  `HWID` varchar(32) NOT NULL DEFAULT '',
  `WindowsCount` int(10) unsigned NOT NULL DEFAULT '1',
  `Account` varchar(45) NOT NULL DEFAULT '',
  `PlayerID` int(10) unsigned NOT NULL DEFAULT '0',
  `LockType` enum('PLAYER_LOCK','ACCOUNT_LOCK','NONE') NOT NULL DEFAULT 'NONE',
  PRIMARY KEY (`HWID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of hwid_info
-- ----------------------------
INSERT INTO `hwid_info` VALUES ('18A0FC18E64C', '1', 'valfox', '268445404', 'NONE');
