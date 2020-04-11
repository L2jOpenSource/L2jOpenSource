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
INSERT INTO `fake_pcs` VALUES ('539', '3', '0', '114', 'www.l2jsunrise.com', '9CE8A9', 'Achievement Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('541', '4', '0', '118', 'www.l2jsunrise.com', '9CE8A9', 'Castle Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '1301', '0', '0', '0', '2453', '0', '354', '0', '381', '0', '2429', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('542', '0', '1', '8', 'www.l2jsunrise.com', '9CE8A9', 'Premium Manager', 'FFFFFF', '3', '2', '1', '0', '0', '0', '0', '0', '0', '0', '235', '0', '0', '0', '2479', '0', '2394', '0', '0', '0', '2440', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('553', '1', '1', '124', 'www.l2jsunrise.com', '9CE8A9', 'Bug Reporter', 'FFFFFF', '3', '2', '1', '0', '0', '0', '0', '0', '0', '0', '2500', '0', '0', '0', '5774', '0', '2383', '0', '0', '0', '5786', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('554', '0', '0', '5', 'www.l2jsunrise.com', '9CE8A9', 'Points Manager', 'FFFFFF', '4', '2', '1', '0', '0', '0', '0', '0', '0', '0', '6364', '0', '0', '0', '6375', '0', '6373', '0', '6374', '0', '6376', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('555', '5', '1', '132', 'www.l2jsunrise.com', '9CE8A9', 'Buffer', 'FFFFFF', '4', '2', '1', '0', '0', '0', '0', '0', '0', '0', '133', '0', '0', '0', '6380', '0', '6379', '0', '0', '0', '6381', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('556', '3', '0', '114', 'www.l2jsunrise.com', '9CE8A9', 'Black Market', 'FFFFFF', '2', '2', '1', '0', '0', '0', '0', '0', '0', '0', '6371', '0', '0', '0', '5768', '0', '2382', '0', '0', '0', '5780', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('559', '3', '0', '114', 'www.l2jsunrise.com', '9CE8A9', 'Beta Npc', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '15556', '0', '0', '0', '15582', '0', '16169', '0', '15579', '0', '15585', '0', '21717', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('560', '0', '1', '2', 'www.l2jsunrise.com', '9CE8A9', 'Delevel Manager', 'FFFFFF', '2', '1', '1', '0', '0', '0', '0', '0', '0', '0', '2591', '0', '0', '0', '2462', '0', '356', '0', '0', '0', '2438', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `fake_pcs` VALUES ('50007', '0', '1', '93', 'www.l2jsunrise.com', '9CE8A9', 'Wedding Manager', 'FFFFFF', '2', '2', '1', '0', '0', '1', '0', '0', '0', '0', '21163', '1', '0', '0', '0', '0', '6408', '0', '0', '0', '0', '0', '0', '0', '0', '0', '6841', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');




