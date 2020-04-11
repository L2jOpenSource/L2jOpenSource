DROP TABLE IF EXISTS `random_spawn`;
CREATE TABLE `random_spawn` (
  `groupId` int(3) unsigned NOT NULL,
  `npcId` smallint(5) unsigned NOT NULL,
  `count` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `initialDelay` int(8) NOT NULL DEFAULT '-1',
  `respawnDelay` int(8) NOT NULL DEFAULT '-1',
  `despawnDelay` int(8) NOT NULL DEFAULT '-1',
  `broadcastSpawn` enum('true','false') NOT NULL DEFAULT 'false',
  `randomSpawn` enum('true','false') NOT NULL DEFAULT 'true',
  PRIMARY KEY (`groupId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO `random_spawn` VALUES
(11,31113,1,-1,-1,-1,'true','true'), -- Merchant of Mammon
(12,31126,1,-1,-1,-1,'true','true'), -- Blacksmith of Mammon
(13,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(14,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(15,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(16,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(17,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(18,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(19,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(20,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(21,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(22,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(23,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(24,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(25,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(26,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(27,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(28,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(29,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(30,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(31,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(32,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(33,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(34,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(35,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(36,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(37,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(38,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(39,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(40,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(41,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(42,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(43,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(44,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(45,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(46,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(47,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(48,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(49,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(50,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(51,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(52,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(53,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(54,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(55,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(56,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(57,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(58,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(59,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(60,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(61,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(62,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(63,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(64,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(65,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(66,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(67,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(68,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(69,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(70,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(71,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(72,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(73,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(74,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(75,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(76,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(77,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(78,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(79,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(80,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(81,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(82,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(83,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(84,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(85,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(86,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(87,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(88,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(89,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(90,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(91,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(92,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(93,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(94,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(95,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(96,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(97,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(98,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(99,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(100,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(101,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(102,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(103,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(104,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(105,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(106,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(107,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(108,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(109,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(110,25283,1,-1,86400,0,'false','false'), -- Lilith (80)
(111,25286,1,-1,86400,0,'false','false'), -- Anakim (80)
(113,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(114,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(115,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(116,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(117,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(118,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(119,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(120,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(121,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(122,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(123,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(124,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(125,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(126,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(127,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(128,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(129,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(130,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(131,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(132,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(133,32014,1,-1,3600000,0,'false','true'), -- Ivan (Runaway Youth quest)
(134,32013,1,-1,3600000,0,'false','true'), -- Suki (Wild Maiden quest)
(136,32012,1,-1,3600000,0,'false','true'), -- Tantan (Aged ExAdventurer quest)
(137,31032,1,-1,300000,300000,'false','true'), -- Guard
(138,31032,1,-1,300000,300000,'false','true'), -- Guard
(139,31032,1,-1,300000,300000,'false','true'), -- Guard
(140,31032,1,-1,300000,300000,'false','true'), -- Guard
(141,31032,1,-1,300000,300000,'false','true'), -- Guard
(142,31032,1,-1,300000,300000,'false','true'), -- Guard
(143,31032,1,-1,300000,300000,'false','true'), -- Guard
(144,31032,1,-1,300000,300000,'false','true'), -- Guard
(145,31032,1,-1,300000,300000,'false','true'), -- Guard
(146,31032,1,-1,300000,300000,'false','true'), -- Guard
(147,31032,1,-1,300000,300000,'false','true'), -- Guard
(148,31032,1,-1,300000,300000,'false','true'), -- Guard
(149,31032,1,-1,300000,300000,'false','true'), -- Guard
(150,31032,1,-1,300000,300000,'false','true'), -- Guard
(151,31032,1,-1,300000,300000,'false','true'), -- Guard
(152,31032,1,-1,300000,300000,'false','true'), -- Guard
(153,31032,1,-1,300000,300000,'false','true'), -- Guard
(154,31032,1,-1,300000,300000,'false','true'), -- Guard
(155,31032,1,-1,300000,300000,'false','true'), -- Guard
(156,31032,1,-1,300000,300000,'false','true'), -- Guard
(157,31032,1,-1,300000,300000,'false','true'), -- Guard
(158,31032,1,-1,300000,300000,'false','true'), -- Guard
(159,31032,1,-1,300000,300000,'false','true'), -- Guard
(160,31032,1,-1,300000,300000,'false','true'), -- Guard
(161,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(162,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(163,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(164,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(165,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(166,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(167,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(168,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(169,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(170,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(171,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(172,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(173,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(174,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(175,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(176,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(177,31033,1,-1,300000,300000,'false','true'), -- Sentinel
(178,31034,1,-1,300000,300000,'false','true'), -- Sentry
(179,31034,1,-1,300000,300000,'false','true'), -- Sentry
(180,31034,1,-1,300000,300000,'false','true'), -- Sentry
(181,31034,1,-1,300000,300000,'false','true'), -- Sentry
(182,31034,1,-1,300000,300000,'false','true'), -- Sentry
(183,31034,1,-1,300000,300000,'false','true'), -- Sentry
(184,31034,1,-1,300000,300000,'false','true'), -- Sentry
(185,31034,1,-1,300000,300000,'false','true'), -- Sentry
(186,31034,1,-1,300000,300000,'false','true'), -- Sentry
(187,31034,1,-1,300000,300000,'false','true'), -- Sentry
(188,31034,1,-1,300000,300000,'false','true'), -- Sentry
(189,31034,1,-1,300000,300000,'false','true'), -- Sentry
(190,31034,1,-1,300000,300000,'false','true'), -- Sentry
(191,31034,1,-1,300000,300000,'false','true'), -- Sentry
(192,31034,1,-1,300000,300000,'false','true'), -- Sentry
(193,31034,1,-1,300000,300000,'false','true'), -- Sentry
(194,31034,1,-1,300000,300000,'false','true'), -- Sentry
(195,31034,1,-1,300000,300000,'false','true'), -- Sentry
(196,31034,1,-1,300000,300000,'false','true'), -- Sentry
(197,31034,1,-1,300000,300000,'false','true'), -- Sentry
(198,31034,1,-1,300000,300000,'false','true'), -- Sentry
(199,31035,1,-1,300000,300000,'false','true'), -- Defender
(200,31035,1,-1,300000,300000,'false','true'), -- Defender
(201,31035,1,-1,300000,300000,'false','true'), -- Defender
(202,31035,1,-1,300000,300000,'false','true'), -- Defender
(203,31035,1,-1,300000,300000,'false','true'), -- Defender
(204,31035,1,-1,300000,300000,'false','true'), -- Defender
(205,31035,1,-1,300000,300000,'false','true'), -- Defender
(206,31035,1,-1,300000,300000,'false','true'), -- Defender
(207,31035,1,-1,300000,300000,'false','true'), -- Defender
(208,31035,1,-1,300000,300000,'false','true'), -- Defender
(209,31035,1,-1,300000,300000,'false','true'), -- Defender
(210,31035,1,-1,300000,300000,'false','true'), -- Defender
(211,31035,1,-1,300000,300000,'false','true'), -- Defender
(212,31035,1,-1,300000,300000,'false','true'), -- Defender
(213,31035,1,-1,300000,300000,'false','true'), -- Defender
(214,31035,1,-1,300000,300000,'false','true'), -- Defender
(215,31035,1,-1,300000,300000,'false','true'), -- Defender
(216,31035,1,-1,300000,300000,'false','true'), -- Defender
(217,31035,1,-1,300000,300000,'false','true'), -- Defender
(218,31036,1,-1,300000,300000,'false','true'), -- Centurion
(219,31036,1,-1,300000,300000,'false','true'), -- Centurion
(220,31036,1,-1,300000,300000,'false','true'), -- Centurion
(221,31036,1,-1,300000,300000,'false','true'), -- Centurion
(222,31036,1,-1,300000,300000,'false','true'), -- Centurion
(223,31036,1,-1,300000,300000,'false','true'), -- Centurion
(224,31036,1,-1,300000,300000,'false','true'), -- Centurion
(225,31036,1,-1,300000,300000,'false','true'), -- Centurion
(226,31036,1,-1,300000,300000,'false','true'), -- Centurion
(227,31036,1,-1,300000,300000,'false','true'), -- Centurion
(228,31036,1,-1,300000,300000,'false','true'), -- Centurion
(229,31036,1,-1,300000,300000,'false','true'), -- Centurion
(230,31036,1,-1,300000,300000,'false','true'), -- Centurion
(231,31036,1,-1,300000,300000,'false','true'), -- Centurion
(232,31036,1,-1,300000,300000,'false','true'), -- Centurion
(233,31036,1,-1,300000,300000,'false','true'), -- Centurion
(234,31036,1,-1,300000,300000,'false','true'), -- Centurion
(235,31036,1,-1,300000,300000,'false','true'), -- Centurion
(236,31036,1,-1,300000,300000,'false','true'), -- Centurion
(237,31036,1,-1,300000,300000,'false','true'), -- Centurion
(238,31036,1,-1,300000,300000,'false','true'), -- Centurion
(239,31036,1,-1,300000,300000,'false','true'), -- Centurion
(240,31036,1,-1,300000,300000,'false','true'), -- Centurion
(241,31036,1,-1,300000,300000,'false','true'), -- Centurion
(242,31036,1,-1,300000,300000,'false','true'), -- Centurion
(243,32335,1,-1,300000,300000,'false','true'), -- Marksman
(244,32335,1,-1,300000,300000,'false','true'), -- Marksman
(245,32335,1,-1,300000,300000,'false','true'), -- Marksman
(246,32335,1,-1,300000,300000,'false','true'), -- Marksman
(247,32335,1,-1,300000,300000,'false','true'), -- Marksman
(248,32335,1,-1,300000,300000,'false','true'), -- Marksman
(249,32335,1,-1,300000,300000,'false','true'), -- Marksman
(250,32335,1,-1,300000,300000,'false','true'), -- Marksman
(251,32335,1,-1,300000,300000,'false','true'), -- Marksman
(252,32335,1,-1,300000,300000,'false','true'), -- Marksman
(253,32335,1,-1,300000,300000,'false','true'), -- Marksman
(254,32335,1,-1,300000,300000,'false','true'), -- Marksman
(255,32335,1,-1,300000,300000,'false','true'), -- Marksman
(256,32335,1,-1,300000,300000,'false','true'), -- Marksman
(257,32335,1,-1,300000,300000,'false','true'), -- Marksman
(258,32335,1,-1,300000,300000,'false','true'), -- Marksman
(259,32335,1,-1,300000,300000,'false','true'), -- Marksman
(260,32335,1,-1,300000,300000,'false','true'), -- Marksman
(261,32335,1,-1,300000,300000,'false','true'), -- Marksman
(262,32335,1,-1,300000,300000,'false','true'), -- Marksman
(263,32335,1,-1,300000,300000,'false','true'), -- Marksman
(264,32335,1,-1,300000,300000,'false','true'), -- Marksman
(265,32335,1,-1,300000,300000,'false','true'), -- Marksman
(266,32335,1,-1,300000,300000,'false','true'), -- Marksman
(267,32335,1,-1,300000,300000,'false','true'), -- Marksman
(268,32335,1,-1,300000,300000,'false','true'), -- Marksman
(269,32335,1,-1,300000,300000,'false','true'), -- Marksman
(270,32335,1,-1,300000,300000,'false','true'), -- Marksman
(271,32335,1,-1,300000,300000,'false','true'), -- Marksman
(272,32335,1,-1,300000,300000,'false','true'), -- Marksman
(273,32335,1,-1,300000,300000,'false','true'), -- Marksman
(274,32335,1,-1,300000,300000,'false','true'), -- Marksman
(275,32335,1,-1,300000,300000,'false','true'), -- Marksman
(276,32335,1,-1,300000,300000,'false','true'); -- Marksman