-- ----------------------------
-- Table structure for `characters_premium`
-- ----------------------------
DROP TABLE IF EXISTS `characters_premium`;
CREATE TABLE `characters_premium` (
  `account_name` varchar(45) NOT NULL DEFAULT '',
  `premium_service` int(1) NOT NULL DEFAULT '0',
  `enddate` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of characters_premium
-- ----------------------------
