-- ----------------------------
-- Table structure for augmentations
-- ----------------------------
DROP TABLE IF EXISTS `augmentations`;
CREATE TABLE `augmentations` (
  `item_object_id` int(11) NOT NULL DEFAULT '0',
  `attributes` int(11) DEFAULT '0',
  `skill` int(11) DEFAULT '0',
  `level` int(11) DEFAULT '0',
  PRIMARY KEY (`item_object_id`)
);