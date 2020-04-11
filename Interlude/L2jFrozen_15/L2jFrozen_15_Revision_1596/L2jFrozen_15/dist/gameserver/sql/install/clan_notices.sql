DROP TABLE IF EXISTS `clan_notices`;
CREATE TABLE `clan_notices` (
  `clanID` int(32) NOT NULL,
  `notice` varchar(512) NOT NULL,
  `enabled` varchar(5) NOT NULL,
  PRIMARY KEY  (`clanID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `clan_notices`
CHANGE `clanID` `clan_id` int(32) NOT NULL FIRST,
COMMENT=''