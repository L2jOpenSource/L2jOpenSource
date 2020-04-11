-- ---------------------------
-- Table structure for castle
-- ---------------------------
CREATE TABLE IF NOT EXISTS castle (
  id INT NOT NULL default 0,
  name varchar(25) NOT NULL,
  taxPercent INT NOT NULL default 15,
  treasury INT NOT NULL default 0,
  siegeDate bigint(20) NOT NULL default 0,
  siegeDayOfWeek INT NOT NULL default 7,
  siegeHourOfDay INT NOT NULL default 20,
  PRIMARY KEY  (name),
  KEY id (id)
);

INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('1', 'Gludio', '0', '0', '0', '7', '20');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('2', 'Dion', '0', '0', '0', '7', '20');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('3', 'Giran', '0', '0', '0', '1', '16');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('4', 'Oren', '0', '0', '0', '1', '16');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('5', 'Aden', '0', '0', '0', '7', '20');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('6', 'Innadril', '0', '0', '0', '1', '16');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('7', 'Goddard', '0', '0', '0', '1', '16');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('8', 'Rune', '0', '0', '0', '7', '20');
INSERT INTO `castle` (`id`, `name`, `taxPercent`, `treasury`, `siegeDate`, `siegeDayOfWeek`, `siegeHourOfDay`) VALUES ('9', 'Schuttgart', '0', '0', '0', '7', '20');
