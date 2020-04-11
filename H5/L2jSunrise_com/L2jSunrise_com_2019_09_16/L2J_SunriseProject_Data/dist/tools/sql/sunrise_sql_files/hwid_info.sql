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
