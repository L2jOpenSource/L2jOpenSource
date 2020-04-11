CREATE TABLE IF NOT EXISTS `custom_npc_elementals` (
  `npc_id` mediumint(7) unsigned NOT NULL,
  `elemAtkType` tinyint(1) NOT NULL DEFAULT '-1',
  `elemAtkValue` int(3) NOT NULL DEFAULT '0',
  `fireDefValue` int(3) NOT NULL DEFAULT '0',
  `waterDefValue` int(3) NOT NULL DEFAULT '0',
  `windDefValue` int(3) NOT NULL DEFAULT '0',
  `earthDefValue` int(3) NOT NULL DEFAULT '0',
  `holyDefValue` int(3) NOT NULL DEFAULT '0',
  `darkDefValue` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`npc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `custom_npc_elementals` VALUES
(50007,0,0,20,20,20,20,20,20);