CREATE TABLE IF NOT EXISTS `character_reco_bonus` (
  `charId` int(10) unsigned NOT NULL,
  `rec_have` int(3) NOT NULL DEFAULT '0',
  `rec_left` int(3) NOT NULL DEFAULT '0',
  `time_left` int(13) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;