-- ----------------------------
-- Table structure for clanhall_siege
-- ----------------------------
DROP TABLE IF EXISTS `clanhall_siege`;
CREATE TABLE IF NOT EXISTS `clanhall_siege` (
  `id` int(11) NOT NULL,
  `name` varchar(40) NOT NULL,
  `siege_data` decimal(20,0) NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `clanhall_siege` VALUES
(35, 'Bandit Stronghold', 0);
