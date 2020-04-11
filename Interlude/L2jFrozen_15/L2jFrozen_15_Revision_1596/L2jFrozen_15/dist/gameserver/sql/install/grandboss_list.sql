-- ----------------------------
-- Table structure for grandboss_list
-- ----------------------------
DROP TABLE IF EXISTS `grandboss_list`;
CREATE TABLE `grandboss_list` (
  `player_id` int(11) NOT NULL,
  `zone` int(11) NOT NULL,
  PRIMARY KEY (`player_id`)
);