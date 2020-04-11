-- ----------------------------
-- Table structure for accounts
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `login` varchar(45) NOT NULL DEFAULT '',
  `password` varchar(45) DEFAULT NULL,
  `lastactive` decimal(20,0) DEFAULT NULL,
  `access_level` int(11) DEFAULT '0',
  `lastIP` varchar(20) DEFAULT NULL,
  `lastServer` int(4) DEFAULT '1',
  `email` varchar(100) DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'use SELECT date(created_date) AS created_date FROM accounts, to get only the date (YYYY-MM-DD)',
  PRIMARY KEY (`login`)
) ;
