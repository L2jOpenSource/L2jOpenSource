/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-04-29 16:17:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `aio_scheme_profiles_buffs`
-- ----------------------------
DROP TABLE IF EXISTS `aio_scheme_profiles_buffs`;
CREATE TABLE `aio_scheme_profiles_buffs` (
  `charId` int(10) unsigned NOT NULL,
  `profile` varchar(45) NOT NULL DEFAULT '',
  `buff_id` int(10) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of aio_scheme_profiles_buffs
-- ----------------------------
