DROP TABLE IF EXISTS `server_variables`;
CREATE TABLE `server_variables` (
  `name` varchar(86) NOT NULL default '',
  `value` varchar(255) character set utf8 NOT NULL default '',
  PRIMARY KEY (`name`)
) ENGINE=MyISAM;
