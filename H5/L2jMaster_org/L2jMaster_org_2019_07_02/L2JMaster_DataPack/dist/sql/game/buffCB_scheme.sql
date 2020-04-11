DROP TABLE IF EXISTS `buff_CB_schemes`;
CREATE TABLE `buff_CB_schemes` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `scheme_name` varchar(35) NOT NULL DEFAULT '',
  `buff_id` int(8) NOT NULL DEFAULT '0',
  `buff_lvl` int(8) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`scheme_name`,`buff_id`,`buff_lvl`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of buff_CB_schemes
-- ----------------------------
