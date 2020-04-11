-- ----------------------------
-- Table structure for ip_banned
-- ----------------------------
DROP TABLE IF EXISTS `ip_banned`;
CREATE TABLE `ip_banned` (
  `ip_address` varchar(25) NOT NULL DEFAULT '',
  PRIMARY KEY (`ip_address`)
);