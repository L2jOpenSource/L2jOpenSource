-- --------------------------------------
-- Table structure for auto_announcements
-- --------------------------------------


CREATE TABLE IF NOT EXISTS `auto_announcements` (
  `id` int(11) NOT NULL auto_increment,
  `announcement` varchar(255) NOT NULL,
  `delay` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) DEFAULT CHARSET=utf8;


-- ------------------------------------
-- Records for table auto_announcements
-- ------------------------------------
