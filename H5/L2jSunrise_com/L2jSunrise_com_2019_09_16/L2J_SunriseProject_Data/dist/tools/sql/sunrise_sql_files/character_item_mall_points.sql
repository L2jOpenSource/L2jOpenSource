-- ----------------------------
-- Table structure for `character_item_mall_points`
-- ----------------------------
DROP TABLE IF EXISTS `character_item_mall_points`;
CREATE TABLE `character_item_mall_points` (
  `account_name` varchar(45) NOT NULL,
  `game_points` bigint(13) NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of character_item_mall_points
-- ----------------------------
