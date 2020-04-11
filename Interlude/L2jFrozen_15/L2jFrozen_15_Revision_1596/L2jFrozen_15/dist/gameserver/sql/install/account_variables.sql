-- ----------------------------
-- Table structure for account_variables
-- ----------------------------
DROP TABLE IF EXISTS `account_variables`;
CREATE TABLE `account_variables` (
  `account_name` varchar(45) NOT NULL,
  `variable` varchar(50) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`account_name`,`variable`)
);
