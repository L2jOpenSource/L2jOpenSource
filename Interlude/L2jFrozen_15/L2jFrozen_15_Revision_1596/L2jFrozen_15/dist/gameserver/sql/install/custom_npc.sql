-- ----------------------------
-- Table structure for custom_npc
-- ----------------------------
DROP TABLE IF EXISTS `custom_npc`;
CREATE TABLE `custom_npc` (
  `id` int(11) NOT NULL DEFAULT '0',
  `idTemplate` int(11) NOT NULL DEFAULT '0',
  `name` varchar(200) DEFAULT NULL,
  `serverSideName` tinyint(1) DEFAULT '0',
  `title` varchar(45) DEFAULT '',
  `serverSideTitle` tinyint(1) DEFAULT '0',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(5,2) DEFAULT NULL,
  `collision_height` decimal(5,2) DEFAULT NULL,
  `level` tinyint(2) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `attackrange` int(11) DEFAULT NULL,
  `hp` decimal(8,0) DEFAULT NULL,
  `mp` decimal(5,0) DEFAULT NULL,
  `hpreg` decimal(8,2) DEFAULT NULL,
  `mpreg` decimal(5,2) DEFAULT NULL,
  `str` decimal(7,0) DEFAULT NULL,
  `con` decimal(7,0) DEFAULT NULL,
  `dex` decimal(7,0) DEFAULT NULL,
  `int` decimal(7,0) DEFAULT NULL,
  `wit` decimal(7,0) DEFAULT NULL,
  `men` decimal(7,0) DEFAULT NULL,
  `exp` decimal(9,0) DEFAULT NULL,
  `sp` decimal(8,0) DEFAULT NULL,
  `patk` decimal(5,0) DEFAULT NULL,
  `pdef` decimal(5,0) DEFAULT NULL,
  `matk` decimal(5,0) DEFAULT NULL,
  `mdef` decimal(5,0) DEFAULT NULL,
  `atkspd` decimal(3,0) DEFAULT NULL,
  `aggro` decimal(6,0) DEFAULT NULL,
  `matkspd` decimal(4,0) DEFAULT NULL,
  `rhand` decimal(4,0) DEFAULT NULL,
  `lhand` decimal(4,0) DEFAULT NULL,
  `armor` decimal(1,0) DEFAULT NULL,
  `walkspd` decimal(3,0) DEFAULT NULL,
  `runspd` decimal(3,0) DEFAULT NULL,
  `faction_id` varchar(40) DEFAULT NULL,
  `faction_range` decimal(4,0) DEFAULT NULL,
  `isUndead` int(11) DEFAULT '0',
  `absorb_level` tinyint(2) DEFAULT '0',
  `absorb_type` enum('FULL_PARTY','LAST_HIT','PARTY_ONE_RANDOM') NOT NULL DEFAULT 'LAST_HIT',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of custom_npc
-- ----------------------------
INSERT INTO `custom_npc` VALUES ('31228', '31228', 'Roy the Cat', '1', 'Classes Trader', '1', 'Monster.cat_the_cat', '9.00', '16.00', '70', 'male', 'L2ClassMaster', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '490', '10', '1335', '470', '780', '382', '278', '0', '333', '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('53', '31113', 'Bernardo', '1', 'Merchant', '1', 'NPC.black_market_trader_MDwarf_set', '22.00', '18.00', '70', 'male', 'L2Merchant', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('54', '21654', 'Necromancer of Destruction', '1', 'Equip Manager', '1', 'Monster.vale_master_20_bi', '14.50', '48.00', '80', 'male', 'L2Merchant', '40', '4608', '1896', '67.15', '3.09', '40', '43', '30', '21', '20', '10', '8042', '913', '1863', '587', '1182', '477', '278', '150', '333', '0', '0', '0', '77', '154', 'fire_clan', '300', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('7077', '31862', 'Dimensional Stone', '1', 'Global Gatekeeper', '1', 'NPC.broadcasting_tower', '7.00', '35.00', '70', 'etc', 'L2Teleporter', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '490', '10', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('50007', '31324', 'Andromeda', '1', 'Wedding Manager', '1', 'NPC.a_casino_FDarkElf', '8.00', '23.00', '70', 'female', 'L2WeddingManager', '40', '3862', '1493', '500.00', '500.00', '40', '43', '30', '21', '20', '10', '0', '0', '9999', '9999', '999', '999', '278', '0', '333', '316', '0', '0', '55', '132', null, '0', '1', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('50017', '31854', 'Protector', '1', 'PVP/PK Manager', '1', 'NPC.a_maidA_FHuman', '8.00', '20.50', '80', 'female', 'L2Protector', '40', '99999', '9999', null, null, '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '10000', '382', '278', '0', '3000', '0', '0', '0', '55', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('50019', '20621', 'Domain', '1', 'Buffer', '1', 'Monster.will_o_wisp', '13.00', '22.00', '62', 'male', 'L2Buffer', '40', '3219', '1217', '35.55', '2.78', '40', '43', '30', '21', '20', '10', '4639', '413', '1038', '353', '532', '315', '278', '500', '333', '0', '0', '0', '88', '191', '', '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('50020', '30298', 'Hefesto', '1', 'Augmenter', '1', 'NPC.a_smith_MDwarf', '7.00', '16.50', '70', 'male', 'L2Trainer', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '89', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('70010', '31606', 'Catrina', '1', 'TvT Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('70011', '31606', 'Catretta', '1', 'CTF Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('70014', '31606', 'Catrieta', '1', 'DM Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `custom_npc` VALUES ('93000', '30705', 'Relune', '1', 'Boss Info', '1', 'NPC.e_fighterguild_teacher_MOrc', '8.00', '28.50', '70', 'male', 'L2RaidBossInfo', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT');
