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
