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
