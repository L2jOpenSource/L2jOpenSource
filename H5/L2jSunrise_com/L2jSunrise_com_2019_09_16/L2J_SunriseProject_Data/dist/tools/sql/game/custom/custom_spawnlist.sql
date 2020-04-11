DROP TABLE IF EXISTS `custom_spawnlist`;
CREATE TABLE `custom_spawnlist` (
  `location` varchar(40) NOT NULL DEFAULT '',
  `spawn_name` varchar(100) NOT NULL DEFAULT '',
  `count` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `npc_templateid` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `locx` mediumint(6) NOT NULL DEFAULT '0',
  `locy` mediumint(6) NOT NULL DEFAULT '0',
  `locz` mediumint(6) NOT NULL DEFAULT '0',
  `randomx` mediumint(6) NOT NULL DEFAULT '0',
  `randomy` mediumint(6) NOT NULL DEFAULT '0',
  `heading` mediumint(6) NOT NULL DEFAULT '0',
  `respawn_delay` mediumint(5) NOT NULL DEFAULT '0',
  `respawn_random` mediumint(5) NOT NULL DEFAULT '0',
  `loc_id` int(9) NOT NULL DEFAULT '0',
  `periodOfDay` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`npc_templateid`,`locx`,`locy`,`locz`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of custom_spawnlist
-- ----------------------------
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-80708', '149814', '-3044', '0', '0', '21871', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-12805', '122815', '-3117', '0', '0', '47093', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '87149', '-143444', '-1293', '0', '0', '11934', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '111329', '219416', '-3546', '0', '0', '44086', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '147905', '-55220', '-2735', '0', '0', '43124', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '83012', '53275', '-1496', '0', '0', '26996', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '43777', '-47666', '-797', '0', '0', '47769', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '15566', '142887', '-2706', '0', '0', '15086', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '146732', '25863', '-2013', '0', '0', '62397', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '10393', '-25111', '-3686', '0', '0', '11232', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-84074', '244561', '-3730', '0', '0', '39662', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '12124', '16731', '-4584', '0', '0', '57343', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '46887', '51506', '-2977', '0', '0', '44471', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-45266', '-112562', '-240', '0', '0', '63303', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '115067', '-178222', '-883', '0', '0', '62980', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-116917', '46627', '367', '0', '0', '40959', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-149591', '255034', '-84', '0', '0', '12641', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-212902', '209534', '4278', '0', '0', '13257', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-248805', '250212', '4335', '0', '0', '60246', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '47945', '186857', '-3486', '0', '0', '40692', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '37830', '-37931', '-3643', '0', '0', '49673', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '87200', '-143443', '-1293', '0', '0', '12554', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '82990', '53112', '-1496', '0', '0', '27689', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '-80665', '149849', '-3044', '0', '0', '22441', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '117109', '76984', '-2695', '0', '0', '35944', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '147852', '-55221', '-2735', '0', '0', '47299', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '15755', '142877', '-2706', '0', '0', '16383', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '146729', '25923', '-2013', '0', '0', '62552', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '43720', '-47677', '-797', '0', '0', '47093', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '111457', '219426', '-3546', '0', '0', '46786', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '-12864', '122817', '-3117', '0', '0', '43515', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '148560', '27985', '-2269', '0', '0', '32431', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '10533', '-25100', '-3685', '0', '0', '12554', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '50007', '-113229', '-245204', '-15537', '0', '0', '17055', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31271', '-113085', '-245205', '-15537', '0', '0', '16383', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '-112980', '-245203', '-15537', '0', '0', '16383', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30627', '-112726', '-245204', '-15537', '0', '0', '15673', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30627', '-112726', '-244031', '-15537', '0', '0', '15673', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30627', '-114197', '-244031', '-15537', '0', '0', '55863', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30627', '-114197', '-245204', '-15537', '0', '0', '11796', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114163', '-245278', '-15537', '0', '0', '39866', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114281', '-245253', '-15537', '0', '0', '30590', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114262', '-245139', '-15537', '0', '0', '14661', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114230', '-244076', '-15537', '0', '0', '17860', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114258', '-243955', '-15537', '0', '0', '11351', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-114149', '-243960', '-15537', '0', '0', '65057', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112775', '-243975', '-15537', '0', '0', '62119', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112677', '-243987', '-15537', '0', '0', '64265', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112645', '-244081', '-15537', '0', '0', '52574', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112692', '-245307', '-15537', '0', '0', '39635', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112787', '-245257', '-15537', '0', '0', '27714', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31340', '-112812', '-245125', '-15537', '0', '0', '24575', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112649', '-245159', '-15537', '0', '0', '43240', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32238', '-112619', '-245251', '-15537', '0', '0', '38901', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '557', '-112723', '-244991', '-15537', '0', '0', '27478', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-112722', '-244863', '-15537', '0', '0', '32767', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31772', '-112722', '-244631', '-15537', '0', '0', '37919', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31688', '-112723', '-244706', '-15537', '0', '0', '34826', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '558', '-112722', '-244446', '-15537', '0', '0', '32767', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30120', '-112723', '-244300', '-15537', '0', '0', '32333', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31341', '-112797', '-244104', '-15537', '0', '0', '41394', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30086', '-112722', '-244193', '-15537', '0', '0', '43018', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30771', '-112986', '-244030', '-15537', '0', '0', '49151', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31340', '-114114', '-244075', '-15537', '0', '0', '58824', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31341', '-114103', '-245144', '-15537', '0', '0', '6673', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '554', '-113494', '-245203', '-15537', '0', '0', '18548', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '560', '-113615', '-245202', '-15537', '0', '0', '16383', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '553', '-113708', '-245203', '-15538', '0', '0', '12859', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '28384', '11110', '-4234', '0', '0', '32767', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '117178', '76783', '-2695', '0', '0', '33221', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '559', '-113358', '-245203', '-15537', '0', '0', '13028', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '46174', '41200', '-3504', '0', '0', '15229', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-71269', '258332', '-3109', '0', '0', '62180', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-56736', '-113613', '-667', '0', '0', '64884', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '108761', '-173918', '-410', '0', '0', '37604', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-125872', '38112', '1237', '0', '0', '1722', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '-90992', '247977', '-3566', '0', '0', '8191', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '10000', '-114108', '-249873', '-2998', '0', '0', '22908', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '10000', '-114981', '-248854', '-2992', '0', '0', '54747', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '541', '-113824', '-245203', '-15537', '0', '0', '13828', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '542', '-114103', '-245023', '-15540', '0', '0', '0', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '540', '-113940', '-245203', '-15537', '0', '0', '14661', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '539', '-114103', '-244929', '-15537', '0', '0', '0', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '558', '-117005', '46694', '367', '0', '0', '44315', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '-116957', '46666', '367', '0', '0', '40486', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '-84042', '244527', '-3729', '0', '0', '39237', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '31126', '83261', '148842', '-3405', '0', '0', '59402', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '4306', '82885', '149385', '-3469', '0', '0', '44995', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30471', '83263', '148395', '-3405', '0', '0', '0', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32615', '83205', '149311', '-3413', '0', '0', '19922', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '32359', '83126', '149315', '-3453', '0', '0', '25270', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '50007', '83296', '149269', '-3408', '0', '0', '39805', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '556', '83687', '147986', '-3405', '0', '0', '32767', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '555', '83068', '148395', '-3472', '0', '0', '32271', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '553', '83685', '148853', '-3408', '0', '0', '30459', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '571', '82148', '148381', '-3467', '0', '0', '54873', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '30080', '83395', '147898', '-3405', '0', '0', '11547', '60', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '572', '82165', '148609', '-3467', '0', '0', '63477', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '570', '83475', '147899', '-3405', '0', '0', '11068', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '558', '81922', '148833', '-3467', '0', '0', '15197', '10', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '573', '82155', '148840', '-3470', '0', '0', '7710', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '9998', '82364', '147546', '-3469', '0', '0', '14761', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '570', '-113322', '-243833', '-15537', '0', '0', '48678', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '571', '-113441', '-243819', '-15537', '0', '0', '50053', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '572', '-113557', '-243819', '-15537', '0', '0', '40216', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '573', '-113690', '-243817', '-15537', '0', '0', '49586', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '573', '150076', '-57759', '-2981', '0', '0', '37904', '0', '0', '0', '0');
INSERT INTO `custom_spawnlist` VALUES ('', '', '1', '571', '150091', '-57696', '-2981', '0', '0', '13945', '0', '0', '0', '0');
