-- 
-- Table structure for table `weapon`
-- 

DROP TABLE IF EXISTS `custom_weapon`;
CREATE TABLE `custom_weapon` (
  `item_id` decimal(11,0) NOT NULL default '0',
  `name` varchar(70) default NULL,
  `bodypart` varchar(15) default NULL,
  `crystallizable` varchar(5) default NULL,
  `weight` decimal(4,0) default NULL,
  `soulshots` decimal(2,0) default NULL,
  `spiritshots` decimal(1,0) default NULL,
  `crystal_type` ENUM("none","d","c","b","a","s") NOT NULL default 'none',
  `p_dam` decimal(5,0) default NULL,
  `rnd_dam` decimal(2,0) default NULL,
  `weaponType` varchar(8) default NULL,
  `critical` decimal(2,0) default NULL,
  `hit_modify` decimal(6,5) default NULL,
  `avoid_modify` decimal(2,0) default NULL,
  `shield_def` decimal(3,0) default NULL,
  `shield_def_rate` decimal(2,0) default NULL,
  `atk_speed` decimal(3,0) default NULL,
  `mp_consume` decimal(2,0) default NULL,
  `m_dam` decimal(3,0) default NULL,
  `duration` decimal(3,0) default NULL,
  `price` decimal(11,0) default NULL,
  `crystal_count` int(4) default NULL,
  `sellable` varchar(5) default NULL,
  `dropable` varchar(5) default NULL,
  `destroyable` varchar(5) default NULL,
  `tradeable` varchar(5) default NULL,
  `item_skill_id` decimal(11,0) NOT NULL default '0',
  `item_skill_lvl` decimal(11,0) NOT NULL default '0',
  `enchant4_skill_id` decimal(11,0) NOT NULL default '0',
  `enchant4_skill_lvl` decimal(11,0) NOT NULL default '0',
  `onCast_skill_id` decimal(11,0) NOT NULL default '0',
  `onCast_skill_lvl` decimal(11,0) NOT NULL default '0',
  `onCast_skill_chance` decimal(11,0) NOT NULL default '0',
  `onCrit_skill_id` decimal(11,0) NOT NULL default '0',
  `onCrit_skill_lvl` decimal(11,0) NOT NULL default '0',
  `onCrit_skill_chance` decimal(11,0) NOT NULL default '0',
  PRIMARY KEY  (`item_id`)
) ;

-- 
-- Dumping data for table `weapon`
-- 
