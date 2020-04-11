DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes` (
  `value` char(100) NOT NULL DEFAULT '1',
  `value_type` tinyint(4) NOT NULL DEFAULT '1',
  `date_voted_website` bigint(10) DEFAULT '1',
  `date_take_reward_in_game` bigint(10) DEFAULT '1',
  `vote_count` int(3) DEFAULT '1',
  PRIMARY KEY (`value`,`value_type`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
