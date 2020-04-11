SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `communitybuff`
-- ----------------------------
DROP TABLE IF EXISTS `communitybuff`;
CREATE TABLE `communitybuff` (
  `key` int(11) DEFAULT NULL,
  `skillID` int(11) DEFAULT NULL,
  `buff_id` int(11) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `itemid` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `communitybuff` VALUES 
-- Basic Skills
('1', '1035', '3', '10000', '57'), -- Mental Shield
('2', '1036', '3', '10000', '57'), -- Magic Barrier
('3', '1040', '3', '10000', '57'), -- Shield
('4', '1044', '3', '10000', '57'), -- Regeneration
('5', '1045', '3', '10000', '57'), -- Blessed Body
('6', '1048', '3', '10000', '57'), -- Blessed Soul
('7', '1059', '2', '10000', '57'), -- Empower
('8', '1047', '0', '10000', '57'), -- Mana Regeneration
('9', '1062', '2', '10000', '57'), -- Berserker Spirit
('10', '1068', '1', '10000', '57'), -- Might
('11', '1077', '1', '10000', '57'), -- Focus
('12', '1078', '2', '10000', '57'), -- Concentration
('13', '1087', '1', '10000', '57'), -- Agility
('14', '1085', '2', '10000', '57'), -- Acumen
('15', '1086', '0', '10000', '57'), -- Haste
('16', '1182', '0', '10000', '57'), -- Resist Aqua
('17', '1189', '0', '10000', '57'), -- Resist Wind
('18', '1191', '0', '10000', '57'), -- Resist Fire
('19', '1204', '3', '10000', '57'), -- Wind Walk
('20', '1240', '1', '10000', '57'), -- Guidance
('21', '1242', '1', '10000', '57'), -- Death Whisper
('22', '1243', '1', '10000', '57'), -- Bless Shield
('23', '1257', '3', '10000', '57'), -- Decrease Weight
('24', '1259', '3', '10000', '57'), -- Resist Shock
('25', '1268', '1', '10000', '57'), -- Vampiric Rage
('26', '1303', '2', '10000', '57'), -- Wild Magic
('27', '1304', '0', '10000', '57'), -- Advanced Block
('28', '1307', '0', '10000', '57'), -- Prayer
('29', '1311', '0', '10000', '57'), -- Body of Avatar
('30', '1460', '0', '10000', '57'), -- Mana Gain
('31', '1392', '3', '10000', '57'), -- Holy Resistance
('32', '1393', '3', '10000', '57'), -- Unholy Resistance
('33', '1352', '3', '10000', '57'), -- Elemental Protection
('34', '1354', '3', '10000', '57'), -- Arcane Protection
('35', '1353', '3', '10000', '57'), -- Divine Protection
('36', '1043', '0', '10000', '57'), -- Holy Weapon
('37', '1397', '0', '10000', '57'), -- Clarity
-- Chants
('38', '1007', '0', '10000', '57'), -- Chant of Battle
('39', '1010', '0', '10000', '57'), -- Soul Shield
('40', '1002', '0', '10000', '57'), -- Flame Chant
('41', '1006', '0', '10000', '57'), -- Chant of Fire
('42', '1009', '0', '10000', '57'), -- Chant of Shielding
('43', '1308', '0', '10000', '57'), -- Chant of Predator
('44', '1309', '0', '10000', '57'), -- Chant of Eagle
('45', '1252', '0', '10000', '57'), -- Chant of Evasion
('46', '1251', '0', '10000', '57'), -- Chant of Fury
('47', '1284', '0', '10000', '57'), -- Chant of Revenge
-- Special Skills
('48', '1388', '1', '10000', '57'), -- Greater Might
('49', '1389', '2', '10000', '57'), -- Greater Shield
('50', '1391', '0', '10000', '57'), -- Earth Chant
('51', '1390', '0', '10000', '57'), -- War Chant
('52', '1461', '0', '10000', '57'), -- Chant of Protection
('53', '1323', '3', '10000', '57'), -- Noblesse Blessing
('54', '1355', '0', '10000', '57'), -- Prophecy of Water
('55', '1356', '0', '10000', '57'), -- Prophecy of Fire
('56', '1357', '0', '10000', '57'), -- Prophecy of Wind
('57', '1363', '3', '10000', '57'), -- Chant of Victory
('58', '1414', '0', '10000', '57'), -- Victory of Pa'agrio
('59', '1413', '0', '10000', '57'), -- Magnus' Chan
('60', '4699', '1', '10000', '57'), -- Blessing of Queen
('61', '4700', '0', '10000', '57'), -- Gift of Queen
('62', '4702', '0', '10000', '57'), -- Blessing of Seraphim
('63', '4703', '3', '10000', '57'), -- Gift of Seraphim
('64', '826', '0', '10000', '57'), -- Spike
('65', '825', '0', '10000', '57'), -- Sharp Edge
('66', '827', '0', '10000', '57'), -- Restring
('67', '828', '0', '10000', '57'), -- Case Harden
('68', '829', '0', '10000', '57'), -- Hard Tanning
('69', '830', '0', '10000', '57'), -- Embroider
-- Dances
('70', '273', '2', '10000', '57'), -- Dance of the Mystic
('71', '276', '2', '10000', '57'), -- Dance of Concentration
('72', '272', '1', '10000', '57'), -- Dance of Inspiration
('73', '271', '1', '10000', '57'), -- Dance of the Warrior
('74', '275', '1', '10000', '57'), -- Dance of Fury
('75', '274', '1', '10000', '57'), -- Dance of Fire
('76', '310', '1', '10000', '57'), -- Dance of the Vampire
('77', '277', '0', '10000', '57'), -- Dance of Light
('78', '307', '3', '10000', '57'), -- Dance of Aqua Guard
('79', '309', '3', '10000', '57'), -- Dance of Earth Guard
('80', '311', '3', '10000', '57'), -- Dance of Protection
('81', '365', '2', '10000', '57'), -- Siren's Dance
('82', '530', '3', '10000', '57'), -- Dance of Alignment
('83', '915', '3', '10000', '57'), -- Dance of Berserker
-- Songs
('84', '264', '3', '10000', '57'), -- Song of Earth
('85', '267', '3', '10000', '57'), -- Song of Warding
('86', '268', '3', '10000', '57'), -- Song of Wind
('87', '269', '3', '10000', '57'), -- Song of Hunter
('88', '304', '1', '10000', '57'), -- Song of Vitality
('89', '266', '1', '10000', '57'), -- Song of Water
('90', '265', '1', '10000', '57'), -- Song of Life
('91', '270', '3', '10000', '57'), -- Song of Invocation
('92', '305', '3', '10000', '57'), -- Song of Vengeance
('93', '306', '3', '10000', '57'), -- Song of Flame Guard
('94', '308', '3', '10000', '57'), -- Song of Storm Guard
('95', '349', '2', '10000', '57'), -- Song of Renewal
('96', '363', '2', '10000', '57'), -- Song of Meditation
('97', '364', '2', '10000', '57'), -- Song of Champion
('98', '529', '2', '10000', '57'); -- Song of Elemental