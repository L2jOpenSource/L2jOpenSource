/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-04-29 16:17:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `aio_teleports`
-- ----------------------------
DROP TABLE IF EXISTS `aio_teleports`;
CREATE TABLE `aio_teleports` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `tpname` varchar(45) DEFAULT NULL,
  `x` int(10) DEFAULT NULL,
  `y` int(10) DEFAULT NULL,
  `z` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=230 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of aio_teleports
-- ----------------------------
INSERT INTO `aio_teleports` VALUES ('1', 'Giran', '82344', '148945', '-3469');
INSERT INTO `aio_teleports` VALUES ('2', 'Aden', '147974', '26883', '-2205');
INSERT INTO `aio_teleports` VALUES ('3', 'Dion', '15592', '143151', '-2707');
INSERT INTO `aio_teleports` VALUES ('4', 'Rune', '43769', '-48133', '-797');
INSERT INTO `aio_teleports` VALUES ('5', 'Oren', '82760', '53578', '-1496');
INSERT INTO `aio_teleports` VALUES ('6', 'Hunters', '116607', '76271', '-2730');
INSERT INTO `aio_teleports` VALUES ('7', 'Goddard', '147725', '-56649', '-2781');
INSERT INTO `aio_teleports` VALUES ('8', 'Heine', '111381', '218981', '-3543');
INSERT INTO `aio_teleports` VALUES ('9', 'Schuttgart', '87352', '-142069', '-1341');
INSERT INTO `aio_teleports` VALUES ('10', 'Gludio', '-12730', '122683', '-3117');
INSERT INTO `aio_teleports` VALUES ('11', 'Gludin', '-80912', '149786', '-3044');
INSERT INTO `aio_teleports` VALUES ('12', 'Harbor', '47938', '186864', '-3420');
INSERT INTO `aio_teleports` VALUES ('13', 'DragonValley', '100764', '111846', '-3680');
INSERT INTO `aio_teleports` VALUES ('14', 'AntharasLair', '131557', '114509', '-3712');
INSERT INTO `aio_teleports` VALUES ('15', 'Deathpass', '72903', '118387', '-3701');
INSERT INTO `aio_teleports` VALUES ('16', 'Cemetery', '172150', '20343', '-3331');
INSERT INTO `aio_teleports` VALUES ('17', 'BlazingSwamp', '147517', '-10640', '-4397');
INSERT INTO `aio_teleports` VALUES ('18', 'Toi', '119316', '16081', '-5128');
INSERT INTO `aio_teleports` VALUES ('19', 'Coliseum', '151400', '46722', '-3408');
INSERT INTO `aio_teleports` VALUES ('20', 'ExecutionGround', '42175', '148219', '-3702');
INSERT INTO `aio_teleports` VALUES ('21', 'CrumaTower', '17225', '114173', '-3440');
INSERT INTO `aio_teleports` VALUES ('22', 'PlainsOfDion', '667', '179223', '-3715');
INSERT INTO `aio_teleports` VALUES ('23', 'HarborRune', '38023', '-38432', '-3609');
INSERT INTO `aio_teleports` VALUES ('24', 'SwampScreams', '92895', '-58343', '-2853');
INSERT INTO `aio_teleports` VALUES ('25', 'Stakato', '89658', '-44685', '-2142');
INSERT INTO `aio_teleports` VALUES ('26', 'VallySaints', '68302', '-72135', '-3751');
INSERT INTO `aio_teleports` VALUES ('27', 'BeastFarm', '52287', '-81417', '-2775');
INSERT INTO `aio_teleports` VALUES ('28', 'SeaSpores', '62450', '30831', '-3768');
INSERT INTO `aio_teleports` VALUES ('29', 'IvoryTower', '85369', '16142', '-3674');
INSERT INTO `aio_teleports` VALUES ('30', 'NorthernWaterfall', '68808', '10157', '-2154');
INSERT INTO `aio_teleports` VALUES ('31', 'PlainsLizardmen', '87256', '85423', '-3082');
INSERT INTO `aio_teleports` VALUES ('32', 'Hardin Academy', '105751', '113370', '-3194');
INSERT INTO `aio_teleports` VALUES ('33', 'South Valley', '124876', '61940', '-3955');
INSERT INTO `aio_teleports` VALUES ('34', 'NorthValley', '104424', '33777', '-3805');
INSERT INTO `aio_teleports` VALUES ('35', 'ForestMirrors', '142325', '81472', '-3001');
INSERT INTO `aio_teleports` VALUES ('36', 'HotSpring', '149612', '-112478', '-2065');
INSERT INTO `aio_teleports` VALUES ('37', 'KetraOutpost', '146954', '-67252', '-3656');
INSERT INTO `aio_teleports` VALUES ('38', 'Monastery', '107574', '-87893', '-2928');
INSERT INTO `aio_teleports` VALUES ('39', 'ForgeGods', '170387', '-116252', '-2040');
INSERT INTO `aio_teleports` VALUES ('40', 'AlligatorIsland', '115583', '192261', '-3488');
INSERT INTO `aio_teleports` VALUES ('41', 'FieldWhispers', '86482', '211953', '-3765');
INSERT INTO `aio_teleports` VALUES ('42', 'FieldSilence', '85066', '182500', '-3653');
INSERT INTO `aio_teleports` VALUES ('43', 'IslePrayer', '159191', '183724', '-3709');
INSERT INTO `aio_teleports` VALUES ('44', 'Parnassus', '149377', '172388', '-946');
INSERT INTO `aio_teleports` VALUES ('45', 'ChromaticHighlands', '152932', '148952', '-3271');
INSERT INTO `aio_teleports` VALUES ('46', 'CryptsDisgrace', '56095', '-118952', '-3285');
INSERT INTO `aio_teleports` VALUES ('47', 'PavelRuin', '88770', '-125555', '-3814');
INSERT INTO `aio_teleports` VALUES ('48', 'frozenvalley', '112971', '-174924', '-608');
INSERT INTO `aio_teleports` VALUES ('49', 'ArchaicLaboratory', '87515', '-109926', '-3335');
INSERT INTO `aio_teleports` VALUES ('50', 'Human', '-83995', '243435', '-3730');
INSERT INTO `aio_teleports` VALUES ('51', 'Dark', '12413', '16621', '-4585');
INSERT INTO `aio_teleports` VALUES ('52', 'Elf', '45855', '49220', '-3060');
INSERT INTO `aio_teleports` VALUES ('53', 'Orc', '-44133', '-113911', '-239');
INSERT INTO `aio_teleports` VALUES ('54', 'Dwarven', '116551', '-182493', '-1520');
INSERT INTO `aio_teleports` VALUES ('55', 'Kamael', '-116934', '46616', '373');
INSERT INTO `aio_teleports` VALUES ('56', 'gludioairship', '-149365', '255340', '-84');
INSERT INTO `aio_teleports` VALUES ('57', 'infinity', '-213669', '210713', '4408');
INSERT INTO `aio_teleports` VALUES ('58', 'destruction', '-246930', '251838', '4347');
INSERT INTO `aio_teleports` VALUES ('59', 'annihilation', '-175520', '154505', '2717');
INSERT INTO `aio_teleports` VALUES ('60', 'PrimevalIsle', '10468', '-24569', '-3645');
INSERT INTO `aio_teleports` VALUES ('61', 'LostNest', '26174', '-17134', '-2742');
INSERT INTO `aio_teleports` VALUES ('62', 'PrimevalPlains', '8041', '-13587', '-3703');
INSERT INTO `aio_teleports` VALUES ('63', 'entrance', '-11670', '236309', '-3273');
INSERT INTO `aio_teleports` VALUES ('64', 'HiddenOasis', '-23806', '249132', '-3171');
INSERT INTO `aio_teleports` VALUES ('65', 'Floor1', '117062', '16058', '-5105');
INSERT INTO `aio_teleports` VALUES ('66', 'Floor2', '114676', '19439', '-3614');
INSERT INTO `aio_teleports` VALUES ('67', 'Floor3', '111250', '16043', '-2132');
INSERT INTO `aio_teleports` VALUES ('68', 'Floor4', '114627', '12696', '-650');
INSERT INTO `aio_teleports` VALUES ('69', 'Floor5', '117951', '16090', '852');
INSERT INTO `aio_teleports` VALUES ('70', 'Floor6', '114280', '19922', '1942');
INSERT INTO `aio_teleports` VALUES ('71', 'Floor7', '115043', '12251', '2952');
INSERT INTO `aio_teleports` VALUES ('72', 'floor8', '110861', '15700', '3962');
INSERT INTO `aio_teleports` VALUES ('73', 'Floor9', '117120', '18964', '4972');
INSERT INTO `aio_teleports` VALUES ('74', 'Floor10', '118456', '16522', '5982');
INSERT INTO `aio_teleports` VALUES ('75', 'floor11', '115832', '17439', '6760');
INSERT INTO `aio_teleports` VALUES ('76', 'westland', '-16487', '207957', '-3665');
INSERT INTO `aio_teleports` VALUES ('77', 'ironcastle', '4593', '243883', '-1930');
INSERT INTO `aio_teleports` VALUES ('78', 'Caravan', '-4579', '255871', '-3134');
INSERT INTO `aio_teleports` VALUES ('82', 'agony', '-41285', '122128', '-2899');
INSERT INTO `aio_teleports` VALUES ('83', 'despair', '-19120', '136816', '-3757');
INSERT INTO `aio_teleports` VALUES ('84', 'antnest', '-9972', '176127', '-4161');
INSERT INTO `aio_teleports` VALUES ('85', 'windawood', '-28327', '155125', '-3496');
INSERT INTO `aio_teleports` VALUES ('79', 'forgotten', '-52977', '191421', '-3563');
INSERT INTO `aio_teleports` VALUES ('80', 'orcbarack', '-89749', '105366', '-3571');
INSERT INTO `aio_teleports` VALUES ('81', 'abandoncamp', '-49824', '147085', '-2779');
INSERT INTO `aio_teleports` VALUES ('86', 'fellmere', '-63708', '101463', '-3557');
INSERT INTO `aio_teleports` VALUES ('87', 'antharas', '174741', '114968', '-7708');
INSERT INTO `aio_teleports` VALUES ('88', 'valakas', '211526', '-113742', '-1636');
INSERT INTO `aio_teleports` VALUES ('89', 'baium', '113203', '14623', '10077');
INSERT INTO `aio_teleports` VALUES ('90', 'baylor', '152638', '142070', '-12738');
INSERT INTO `aio_teleports` VALUES ('91', 'freya', '114716', '-113638', '-11200');
INSERT INTO `aio_teleports` VALUES ('92', 'beleth', '16322', '213116', '-9357');
INSERT INTO `aio_teleports` VALUES ('93', 'tezza', '-87754', '-152137', '-9176');
INSERT INTO `aio_teleports` VALUES ('94', 'dayzaken', '56592', '218382', '-2953');
INSERT INTO `aio_teleports` VALUES ('95', 'nightzaken', '54972', '218780', '-3225');
INSERT INTO `aio_teleports` VALUES ('96', 'queenant', '-21576', '184349', '-5722');
INSERT INTO `aio_teleports` VALUES ('97', 'orfen', '54210', '17069', '-5536');
INSERT INTO `aio_teleports` VALUES ('98', 'core', '17735', '111600', '-6584');
INSERT INTO `aio_teleports` VALUES ('99', 'chaotic', '-81906', '-53428', '-10732');
INSERT INTO `aio_teleports` VALUES ('100', 'farmzone', '-175539', '154503', '2716');
INSERT INTO `aio_teleports` VALUES ('101', 'varka', '125744', '-40864', '-3750');
INSERT INTO `aio_teleports` VALUES ('102', 'argos', '165091', '-47837', '-3568');
INSERT INTO `aio_teleports` VALUES ('103', 'imptomb', '187292', '-75504', '-2794');
INSERT INTO `aio_teleports` VALUES ('105', 'brekas', '85506', '131201', '-3677');
INSERT INTO `aio_teleports` VALUES ('104', 'flower', '113930', '135394', '-3638');
INSERT INTO `aio_teleports` VALUES ('107', 'forsplain', '168224', '37962', '-4062');
INSERT INTO `aio_teleports` VALUES ('108', 'giantcave', '181745', '46508', '-4358');
INSERT INTO `aio_teleports` VALUES ('109', 'partizan', '50394', '121802', '-2410');
INSERT INTO `aio_teleports` VALUES ('110', 'mandragora', '38349', '148079', '-3709');
INSERT INTO `aio_teleports` VALUES ('111', 'tanor', '58349', '163882', '-2833');
INSERT INTO `aio_teleports` VALUES ('112', 'floran', '17107', '170183', '-3492');
INSERT INTO `aio_teleports` VALUES ('113', 'forbitengate', '188611', '20588', '-3696');
INSERT INTO `aio_teleports` VALUES ('114', 'enchantvalley', '124904', '61992', '-3973');
INSERT INTO `aio_teleports` VALUES ('115', 'silentvalley', '170838', '55776', '-5280');
INSERT INTO `aio_teleports` VALUES ('116', 'huntervalley', '114306', '86573', '-3112');
INSERT INTO `aio_teleports` VALUES ('117', 'fieldmassacre', '135580', '19467', '-3424');
INSERT INTO `aio_teleports` VALUES ('118', 'devastated', '178358', '-14192', '-2256');
INSERT INTO `aio_teleports` VALUES ('119', 'anghelwater', '166182', '91560', '-3168');
INSERT INTO `aio_teleports` VALUES ('120', 'cavesouls', '-123842', '38117', '1176');
INSERT INTO `aio_teleports` VALUES ('121', 'ancientbattle', '106517', '-2871', '-3454');
INSERT INTO `aio_teleports` VALUES ('122', 'plainsglory', '135580', '19467', '-3424');
INSERT INTO `aio_teleports` VALUES ('123', 'wartorplains', '156898', '11217', '-4032');
INSERT INTO `aio_teleports` VALUES ('124', 'isleofsoul', '-121436', '56288', '-1586');
INSERT INTO `aio_teleports` VALUES ('125', 'mimirforest', '-103032', '46457', '-1136');
INSERT INTO `aio_teleports` VALUES ('126', 'hillgold', '-116114', '87005', '-3544');
INSERT INTO `aio_teleports` VALUES ('127', 'norlingarden', '-84728', '60089', '-2576');
INSERT INTO `aio_teleports` VALUES ('128', 'ebordenout', '158141', '-24543', '-1288');
INSERT INTO `aio_teleports` VALUES ('129', 'narselake', '146440', '46723', '-3400');
INSERT INTO `aio_teleports` VALUES ('130', 'wbordenout', '112405', '-16607', '-1864');
INSERT INTO `aio_teleports` VALUES ('131', 'catadarkomens', '-19176', '13504', '-4899');
INSERT INTO `aio_teleports` VALUES ('132', 'cataforbidpath', '12521', '-248481', '-9585');
INSERT INTO `aio_teleports` VALUES ('133', 'catawitch', '140690', '79679', '-5429');
INSERT INTO `aio_teleports` VALUES ('134', 'cataapostate', '-20195', '-250764', '-8193');
INSERT INTO `aio_teleports` VALUES ('135', 'catabraded', '46542', '170305', '-4979');
INSERT INTO `aio_teleports` VALUES ('136', 'cataheretics', '-53174', '-250275', '-7911');
INSERT INTO `aio_teleports` VALUES ('137', 'cruma1', '17724', '114004', '-11672');
INSERT INTO `aio_teleports` VALUES ('138', 'cruma2', '17730', '108301', '-9057');
INSERT INTO `aio_teleports` VALUES ('139', 'cruma3', '17719', '115430', '-6582');
INSERT INTO `aio_teleports` VALUES ('140', 'dionarena', '12443', '183467', '-3560');
INSERT INTO `aio_teleports` VALUES ('141', 'crumamars', '5106', '126916', '-3664');
INSERT INTO `aio_teleports` VALUES ('142', 'floranagricul', '10610', '156332', '-2472');
INSERT INTO `aio_teleports` VALUES ('143', 'dionhills', '29928', '151415', '2392');
INSERT INTO `aio_teleports` VALUES ('144', 'beehive', '34475', '188095', null);
INSERT INTO `aio_teleports` VALUES ('145', 'plainsdion', '630', '179184', '-3720');
INSERT INTO `aio_teleports` VALUES ('146', 'necrosaint', '83357', '209207', '-5437');
INSERT INTO `aio_teleports` VALUES ('147', 'necrodisciple', '172600', '-17599', '-4899');
INSERT INTO `aio_teleports` VALUES ('148', 'necromartyrdom', '118576', '132800', '-4832');
INSERT INTO `aio_teleports` VALUES ('149', 'necrodevotion', '-51942', '79096', '-4739');
INSERT INTO `aio_teleports` VALUES ('150', 'necropatriot', '-21423', '77375', '-5171');
INSERT INTO `aio_teleports` VALUES ('151', 'necroworshipper', '111552', '174014', '-5440');
INSERT INTO `aio_teleports` VALUES ('152', 'necropilgrim', '45249', '123548', '-5411');
INSERT INTO `aio_teleports` VALUES ('153', 'necrosavrifice', '-41569', '210082', '-5085');
INSERT INTO `aio_teleports` VALUES ('154', 'antharaheart', '154623', '121134', '-3809');
INSERT INTO `aio_teleports` VALUES ('155', 'devilisle', '43408', '206881', '-3752');
INSERT INTO `aio_teleports` VALUES ('156', 'hardinacademy', '105918', '109759', '-3170');
INSERT INTO `aio_teleports` VALUES ('157', 'giranarena', '73579', '142709', '-3762');
INSERT INTO `aio_teleports` VALUES ('158', 'piratetunnel', '41528', '198358', '-4648');
INSERT INTO `aio_teleports` VALUES ('159', 'wasteland', '-16526', '208032', '-3664');
INSERT INTO `aio_teleports` VALUES ('160', 'abandoncamp', '-49853', '147089', '-2784');
INSERT INTO `aio_teleports` VALUES ('161', 'orcbarrack', '-89763', '105359', '-3576');
INSERT INTO `aio_teleports` VALUES ('162', 'windyhill', '-88539', '83389', '-2864');
INSERT INTO `aio_teleports` VALUES ('163', 'redrockridge', '-44829', '188171', '-3256');
INSERT INTO `aio_teleports` VALUES ('164', 'felemerharvest', '-63736', '101522', '-3552');
INSERT INTO `aio_teleports` VALUES ('165', 'windmilhill', '-75437', '168800', '-3632');
INSERT INTO `aio_teleports` VALUES ('166', 'ruinsagonybend', '-50174', '129303', '-2912');
INSERT INTO `aio_teleports` VALUES ('167', 'evilhunting', '-6989', '109503', '-3040');
INSERT INTO `aio_teleports` VALUES ('168', 'entrruindespair', '-36652', '135591', '-3160');
INSERT INTO `aio_teleports` VALUES ('169', 'olmahum', '-6661', '201880', '-3632');
INSERT INTO `aio_teleports` VALUES ('170', 'neutralzone', '-10612', '75881', '-3592');
INSERT INTO `aio_teleports` VALUES ('171', 'frgottemple', '-53001', '191425', '-3568');
INSERT INTO `aio_teleports` VALUES ('172', 'elvenruin', '-112367', '234703', '-3688');
INSERT INTO `aio_teleports` VALUES ('173', 'antincubator', '-26489', '195307', '-3928');
INSERT INTO `aio_teleports` VALUES ('174', 'gludinharbor', '-91101', '150344', '-3624');
INSERT INTO `aio_teleports` VALUES ('175', 'gludinarena', '-87328', '142266', '-3640');
INSERT INTO `aio_teleports` VALUES ('176', 'gardenbeasts', '132997', '-60608', '-2960');
INSERT INTO `aio_teleports` VALUES ('177', 'shrineofloyalty', '190112', '-61776', '-2944');
INSERT INTO `aio_teleports` VALUES ('178', 'devilpass', '106349', '-61870', '-2904');
INSERT INTO `aio_teleports` VALUES ('179', 'hallofflames', '189964', '-116820', '-1624');
INSERT INTO `aio_teleports` VALUES ('180', 'ketravillage', '149548', '-82014', '-5592');
INSERT INTO `aio_teleports` VALUES ('181', 'varkavillage', '108155', '-53670', '-2472');
INSERT INTO `aio_teleports` VALUES ('182', 'rainbowsprings', '139997', '-124860', '-1896');
INSERT INTO `aio_teleports` VALUES ('183', 'hotspringarena', '152180', '-126093', '-2282');
INSERT INTO `aio_teleports` VALUES ('184', 'aligatorbeach', '116267', '201177', '-3432');
INSERT INTO `aio_teleports` VALUES ('185', 'coralreef', '160074', '167893', '-3538');
INSERT INTO `aio_teleports` VALUES ('186', 'gardeneva', '84413', '234334', '-3656');
INSERT INTO `aio_teleports` VALUES ('187', 'tourboatdock', '111418', '225960', '-3624');
INSERT INTO `aio_teleports` VALUES ('188', 'gardeneva1', '80688', '245566', '-8926');
INSERT INTO `aio_teleports` VALUES ('189', 'gardeneva2', '80629', '246620', '-9331');
INSERT INTO `aio_teleports` VALUES ('190', 'gardeneva3', '87750', '252422', '-9851');
INSERT INTO `aio_teleports` VALUES ('191', 'gardeneva4', '82506', '255978', '-10363');
INSERT INTO `aio_teleports` VALUES ('192', 'gardeneva5', '82158', '252376', '-10592');
INSERT INTO `aio_teleports` VALUES ('193', 'forestdead', '52107', '-54328', '-3158');
INSERT INTO `aio_teleports` VALUES ('194', 'pagantemple', '35630', '-49748', '-760');
INSERT INTO `aio_teleports` VALUES ('195', 'cursedvillage', '57670', '-41672', '-3154');
INSERT INTO `aio_teleports` VALUES ('196', 'windtailwaterfall', '40723', '-94881', '-2096');
INSERT INTO `aio_teleports` VALUES ('197', 'westminezone', '128527', '-204036', '-3408');
INSERT INTO `aio_teleports` VALUES ('198', 'eastminezone', '175836', '-205837', '-3384');
INSERT INTO `aio_teleports` VALUES ('199', 'pluderousplain', '111965', '-154972', '-1528');
INSERT INTO `aio_teleports` VALUES ('200', 'frozenlabyrith', '113903', '-108752', '-884');
INSERT INTO `aio_teleports` VALUES ('201', 'freyagarden', '102728', '-126242', '-2840');
INSERT INTO `aio_teleports` VALUES ('202', 'denofeveil', '68693', '-110438', '-1946');
INSERT INTO `aio_teleports` VALUES ('203', 'skywagon', '121618', '-141554', '-1496');
INSERT INTO `aio_teleports` VALUES ('204', 'caveoftrials', '9340', '-112509', '-2536');
INSERT INTO `aio_teleports` VALUES ('205', 'abandoncoalmine', '139714', '-177456', '-1536');
INSERT INTO `aio_teleports` VALUES ('206', 'mithrilmine', '171946', '-173352', '3440');
INSERT INTO `aio_teleports` VALUES ('207', 'icemerchantcabin', '113750', '-109163', '-832');
INSERT INTO `aio_teleports` VALUES ('208', 'brigandstronghold', '126272', '-159336', '-1232');
INSERT INTO `aio_teleports` VALUES ('209', 'elvenforest', '21362', '51122', '-3688');
INSERT INTO `aio_teleports` VALUES ('210', 'shadowmothertree', '50953', '42105', '-3480');
INSERT INTO `aio_teleports` VALUES ('211', 'darkforest', '-22224', '14168', '-3232');
INSERT INTO `aio_teleports` VALUES ('212', 'swampland', '-21966', '40544', '-3192');
INSERT INTO `aio_teleports` VALUES ('213', 'selmahum', '84517', '62538', '-3480');
INSERT INTO `aio_teleports` VALUES ('214', 'shilengarden', '23863', '11068', '-3720');
INSERT INTO `aio_teleports` VALUES ('215', 'blackrockhill', '-29466', '66678', '-3496');
INSERT INTO `aio_teleports` VALUES ('216', 'spidernest', '-61095', '75104', '-3383');
INSERT INTO `aio_teleports` VALUES ('217', 'timakoutpost', '67097', '68815', '-3648');
INSERT INTO `aio_teleports` VALUES ('218', 'forestofevil', '93218', '16969', '-3904');
INSERT INTO `aio_teleports` VALUES ('219', 'outlawforest', '91539', '-12204', '-2440');
INSERT INTO `aio_teleports` VALUES ('220', 'schooldarkarts', '-47543', '58478', '-3336');
INSERT INTO `aio_teleports` VALUES ('221', 'shilentemple', '25934', '11037', '-3720');
INSERT INTO `aio_teleports` VALUES ('222', 'banditstronghold', '87091', '-20354', '-2072');
INSERT INTO `aio_teleports` VALUES ('223', 'irislake', '51469', '82600', '-3312');
INSERT INTO `aio_teleports` VALUES ('224', 'altarofrites', '-44566', '77508', '-3736');
INSERT INTO `aio_teleports` VALUES ('225', 'mistymountains', '61740', '94946', '-1488');
INSERT INTO `aio_teleports` VALUES ('226', 'starlightwaterfall', '58502', '53453', '-3624');
INSERT INTO `aio_teleports` VALUES ('227', 'undinewaterfall', '-7233', '57006', '-3520');
INSERT INTO `aio_teleports` VALUES ('228', 'godsfalls', '70456', '6591', '-3632');
INSERT INTO `aio_teleports` VALUES ('229', 'Floor12', '117275', '16073', '7996');
