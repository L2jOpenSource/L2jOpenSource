CREATE TABLE IF NOT EXISTS `announcements` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `type` int(11) NOT NULL,
  `initial` bigint(20) NOT NULL DEFAULT 0,
  `delay` bigint(20) NOT NULL DEFAULT 0,
  `repeat` int(11) NOT NULL DEFAULT 0,
  `author` text NOT NULL,
  `content` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO announcements (`type`, `author`, `content`) VALUES 
(1, 'L2JSunrise', '------------------------------------'),
(1, 'L2JSunrise', 'Welcome to L2JSunrise.'),
(1, 'L2JSunrise', 'Report us bugs and problems to fix.'),
(1, 'L2JSunrise', 'them (at forum).'),
(1, 'L2JSunrise', 'Thanks for using L2JSunrise.'),
(1, 'L2JSunrise', '------------------------------------');
