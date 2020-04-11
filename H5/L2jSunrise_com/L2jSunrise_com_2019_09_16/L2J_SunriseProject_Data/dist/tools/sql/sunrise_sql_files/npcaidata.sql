-- ----------------------------
-- Table structure for `custom_npcaidata`
-- ----------------------------
DROP TABLE IF EXISTS `custom_npcaidata`;
CREATE TABLE `custom_npcaidata` (
  `npcId` mediumint(7) unsigned NOT NULL,
  `minSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '7',
  `maxSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '15',
  `primarySkillId` smallint(5) unsigned DEFAULT '0',
  `agroRange` smallint(4) unsigned NOT NULL DEFAULT '0',
  `canMove` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `targetable` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `showName` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `minRangeSkill` smallint(5) unsigned DEFAULT '0',
  `minRangeChance` tinyint(3) unsigned DEFAULT '0',
  `maxRangeSkill` smallint(5) unsigned DEFAULT '0',
  `maxRangeChance` tinyint(3) unsigned DEFAULT '0',
  `soulShot` smallint(4) unsigned DEFAULT '0',
  `spiritShot` smallint(4) unsigned DEFAULT '0',
  `spsChance` tinyint(3) unsigned DEFAULT '0',
  `ssChance` tinyint(3) unsigned DEFAULT '0',
  `aggro` smallint(4) unsigned NOT NULL DEFAULT '0',
  `isChaos` smallint(4) unsigned DEFAULT '0',
  `clan` varchar(40) DEFAULT NULL,
  `clanRange` smallint(4) unsigned DEFAULT '0',
  `enemyClan` varchar(40) DEFAULT NULL,
  `enemyRange` smallint(4) unsigned DEFAULT '0',
  `dodge` tinyint(3) unsigned DEFAULT '0',
  `aiType` varchar(8) NOT NULL DEFAULT 'fighter',
  PRIMARY KEY (`npcId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of custom_npcaidata
-- ----------------------------
INSERT INTO `custom_npcaidata` VALUES ('539', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('541', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '', '300', '', '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('542', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('543', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('553', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('554', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('555', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('556', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('558', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('559', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('560', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('570', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('571', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('572', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('565', '7', '15', '0', '300', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '', '300', '', '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('573', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('574', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('575', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', '2', '0', '0', '0', '0', null, '0', null, '0', '0', 'balanced');
INSERT INTO `custom_npcaidata` VALUES ('8983', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8984', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8985', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8986', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8987', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8988', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8989', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8990', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8993', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8994', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8995', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8996', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8997', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('8998', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9997', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9998', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9999', '7', '15', '0', '0', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('10000', '7', '15', '0', '1000', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '1500', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('50007', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('70010', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
