DROP TABLE IF EXISTS `engine`;
CREATE TABLE `engine`
(
  `id` int(10) NOT NULL auto_increment,
  `charId` int(10) unsigned NOT NULL default '0',
  `modName` varchar(255) NOT NULL,
  `event` varchar(255) NOT NULL default '',
  `val` varchar(1000) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;