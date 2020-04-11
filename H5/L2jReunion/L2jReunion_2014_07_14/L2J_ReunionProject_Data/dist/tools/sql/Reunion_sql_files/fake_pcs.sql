/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-04-29 16:16:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `fake_pcs`
-- ----------------------------
DROP TABLE IF EXISTS `fake_pcs`;
CREATE TABLE `fake_pcs` (
  `npc_id` int(11) NOT NULL,
  `race` int(11) NOT NULL DEFAULT '0',
  `sex` int(11) NOT NULL DEFAULT '0',
  `class` int(11) NOT NULL DEFAULT '0',
  `title` varchar(255) NOT NULL,
  `title_color` varchar(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `name_color` varchar(11) DEFAULT NULL,
  `hair_style` int(11) NOT NULL DEFAULT '0',
  `hair_color` int(11) NOT NULL DEFAULT '0',
  `face` int(11) NOT NULL DEFAULT '0',
  `mount` tinyint(4) NOT NULL DEFAULT '0',
  `team` tinyint(4) NOT NULL DEFAULT '0',
  `hero` tinyint(4) NOT NULL DEFAULT '0',
  `pd_under` int(11) NOT NULL DEFAULT '0',
  `pd_under_aug` int(11) NOT NULL DEFAULT '0',
  `pd_head` int(11) NOT NULL DEFAULT '0',
  `pd_head_aug` int(11) NOT NULL DEFAULT '0',
  `pd_rhand` int(11) NOT NULL DEFAULT '0',
  `pd_rhand_aug` int(11) NOT NULL DEFAULT '0',
  `pd_lhand` int(11) NOT NULL DEFAULT '0',
  `pd_lhand_aug` int(11) NOT NULL DEFAULT '0',
  `pd_gloves` int(11) NOT NULL DEFAULT '0',
  `pd_gloves_aug` int(11) NOT NULL DEFAULT '0',
  `pd_chest` int(11) NOT NULL DEFAULT '0',
  `pd_chest_aug` int(11) NOT NULL DEFAULT '0',
  `pd_legs` int(11) NOT NULL DEFAULT '0',
  `pd_legs_aug` int(11) NOT NULL DEFAULT '0',
  `pd_feet` int(11) NOT NULL DEFAULT '0',
  `pd_feet_aug` int(11) NOT NULL DEFAULT '0',
  `pd_back` int(11) NOT NULL DEFAULT '0',
  `pd_back_aug` int(11) NOT NULL DEFAULT '0',
  `pd_lrhand` int(11) NOT NULL DEFAULT '0',
  `pd_lrhand_aug` int(11) NOT NULL DEFAULT '0',
  `pd_hair` int(11) NOT NULL DEFAULT '0',
  `pd_hair_aug` int(11) NOT NULL DEFAULT '0',
  `pd_hair2` int(11) NOT NULL DEFAULT '0',
  `pd_hair2_aug` int(11) NOT NULL DEFAULT '0',
  `pd_rbracelet` int(11) NOT NULL DEFAULT '0',
  `pd_rbracelet_aug` int(11) NOT NULL DEFAULT '0',
  `pd_lbracelet` int(11) NOT NULL DEFAULT '0',
  `pd_lbracelet_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco1` int(11) NOT NULL DEFAULT '0',
  `pd_deco1_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco2` int(11) NOT NULL DEFAULT '0',
  `pd_deco2_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco3` int(11) NOT NULL DEFAULT '0',
  `pd_deco3_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco4` int(11) NOT NULL DEFAULT '0',
  `pd_deco4_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco5` int(11) NOT NULL DEFAULT '0',
  `pd_deco5_aug` int(11) NOT NULL DEFAULT '0',
  `pd_deco6` int(11) NOT NULL DEFAULT '0',
  `pd_deco6_aug` int(11) NOT NULL DEFAULT '0',
  `enchant_effect` tinyint(4) NOT NULL DEFAULT '0',
  `pvp_flag` int(11) NOT NULL DEFAULT '0',
  `karma` int(11) NOT NULL DEFAULT '0',
  `fishing` tinyint(4) NOT NULL DEFAULT '0',
  `fishing_x` int(11) NOT NULL DEFAULT '0',
  `fishing_y` int(11) NOT NULL DEFAULT '0',
  `fishing_z` int(11) NOT NULL DEFAULT '0',
  `invisible` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`npc_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of fake_pcs
-- ----------------------------
INSERT INTO `fake_pcs` VALUES ('539', '3', '0', '114', 'L2JReunion', '9CE8A9', 'Achievement Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('540', '3', '0', '114', 'L2JReunion', '9CE8A9', 'Elven Ruin Mod', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('541', '3', '0', '114', 'L2JReunion', '9CE8A9', 'Castle Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('542', '3', '0', '114', 'L2JReunion', '9CE8A9', 'Premium Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('553', '0', '1', '95', 'L2JReunion', '8BFFA8', 'Bug Reporter', 'FFFFFF', '3', '2', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('554', '5', '1', '124', 'L2JReunion', '9CE8A9', 'Points Manager', 'FFFFFF', '3', '2', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('555', '4', '1', '53', 'L2JReunion', '9CE8A9', 'Buffer', 'FFFFFF', '5', '3', '1', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('556', '4', '0', '53', 'L2JReunion', '9CE8A9', 'Black Market', 'FFFFFF', '2', '3', '1', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('559', '0', '0', '93', 'L2JReunion', '9CE8A9', 'Clan Searcher', 'FFFFFF', '4', '2', '1', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('560', '0', '0', '93', 'L2JReunion', '9CE8A9', 'Delevel Manager', 'FFFFFF', '4', '2', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('50007', '0', '1', '93', 'L2JReunion', '9CE8A9', 'Wedding Manager', 'FFFFFF', '2', '2', '1', '0', '0', '1', '0', '0', '0', '0', '21163', '1', '0', '0', '0', '0', '6408', '0', '0', '0', '0', '0', '0', '0', '0', '0', '6841', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
