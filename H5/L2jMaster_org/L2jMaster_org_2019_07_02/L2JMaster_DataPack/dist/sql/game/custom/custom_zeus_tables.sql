/*
Navicat MySQL Data Transfer

Source Server         : L2Dev
Source Server Version : 50715
Source Host           : localhost:3306
Source Database       : l2jmaster

Target Server Type    : MYSQL
Target Server Version : 50715
File Encoding         : 65001

Date: 2018-05-19 13:10:06
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `zeus_annoucement`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_annoucement`;
CREATE TABLE `zeus_annoucement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `strtitle` varchar(80) DEFAULT '',
  `strmensaje` text CHARACTER SET utf8 COLLATE utf8_spanish_ci,
  `strgmnombre` varchar(80) DEFAULT '',
  `fecha` datetime DEFAULT NULL,
  `tipo` enum('ANNOUCEMENT','CHANGELOG','RULES','EVENTS','FEATURES','PLAYGAME') DEFAULT 'ANNOUCEMENT',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_annoucement
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_antibot`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_antibot`;
CREATE TABLE `zeus_antibot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ask` varchar(300) DEFAULT NULL,
  `answer` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_antibot
-- ----------------------------
INSERT INTO `zeus_antibot` VALUES ('1', '1+1', '2');
INSERT INTO `zeus_antibot` VALUES ('2', '2+2', '4');
INSERT INTO `zeus_antibot` VALUES ('3', '2*2', '4');
INSERT INTO `zeus_antibot` VALUES ('4', '10+20', '30');

-- ----------------------------
-- Table structure for `zeus_auctions_house`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_auctions_house`;
CREATE TABLE `zeus_auctions_house` (
  `idObjeto` int(11) DEFAULT NULL,
  `idOwner` int(11) DEFAULT NULL,
  `idItemRequest` int(11) DEFAULT NULL,
  `ItemRequestQuantity` int(11) DEFAULT NULL,
  `ItemQuantityToSell` bigint(11) DEFAULT NULL,
  `startUnix` bigint(11) DEFAULT NULL,
  `feed` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_auctions_house
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_auctions_house_offline`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_auctions_house_offline`;
CREATE TABLE `zeus_auctions_house_offline` (
  `idChar` int(11) NOT NULL,
  `idItemVendido` int(11) NOT NULL DEFAULT '0',
  `idItemSolicitado` int(11) DEFAULT NULL,
  `totalItemEntregar` bigint(20) DEFAULT NULL,
  `totalItemVendidos` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idChar`,`idItemVendido`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_auctions_house_offline
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_augment_data`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_augment_data`;
CREATE TABLE `zeus_augment_data` (
  `id` int(11) DEFAULT NULL,
  `idaugmentGame` int(11) DEFAULT NULL,
  `tipo` varchar(10) DEFAULT NULL,
  `aug_descrip` varchar(255) DEFAULT NULL,
  `aug_skill` int(11) DEFAULT NULL,
  `skill_descrip` varchar(255) DEFAULT NULL,
  `skill_level` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_augment_data
-- ----------------------------
INSERT INTO `zeus_augment_data` VALUES ('14561', '954269697', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14562', '954335233', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14563', '954400769', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14564', '954466305', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14565', '954531841', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14566', '954597377', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14567', '954662913', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14568', '954728449', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 219.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14569', '954793985', 'Chance', ' Temporarily provokes a target to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 438.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14570', '954859521', 'Chance', ' Temporarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14571', '954925057', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 219.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14572', '954990593', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 219.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14573', '955056129', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14574', '955121665', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14575', '955187201', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14576', '955252737', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14577', '955318273', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14578', '955383809', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14579', '955449345', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14580', '955514881', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14581', '955580417', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 264.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14582', '955645953', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14583', '955711489', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14584', '955777025', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14585', '955842561', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 219.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14586', '955908097', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 438.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14587', '955973633', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 438.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14588', '956039169', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14589', '956104705', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14590', '956170241', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14591', '956235777', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14592', '956301313', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14593', '956366849', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 308.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14594', '956432385', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14595', '956497921', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 30 temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14596', '956563457', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14597', '956628993', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 30 temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14598', '956694529', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 20 temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14599', '956760065', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14600', '956825601', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 68.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14601', '956891137', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14602', '956956673', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 41.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14603', '957022209', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '1');
INSERT INTO `zeus_augment_data` VALUES ('14604', '957087745', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14605', '957153281', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14606', '957218817', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14607', '957284353', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14608', '957349889', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14609', '957415425', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14610', '957480961', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14611', '957546497', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14612', '957612033', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14613', '957677569', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14614', '957743105', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14615', '957808641', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14616', '957874177', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14617', '957939713', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14618', '958005249', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 31.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14619', '958070785', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14620', '958136321', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14621', '958201857', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14622', '958267393', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14623', '958332929', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14624', '958398465', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14625', '958464001', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14626', '958529537', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14627', '958595073', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14628', '958660609', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14629', '958726145', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14630', '958791681', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14631', '958857217', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14632', '958922753', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 1 doors with 100% probability, level 2 doors with 30% probability, and chests below level 36 with 90% probability. Requires 4 Keys of a Thief.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14633', '958988289', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 49.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14634', '959053825', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14635', '959119361', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 61.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14636', '959184897', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14637', '959250433', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14638', '959315969', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14639', '959381505', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14640', '959447041', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14641', '959512577', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14642', '959578113', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14643', '959643649', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14644', '959709185', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14645', '959774721', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14646', '959840257', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14647', '959905793', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14648', '959971329', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14649', '960036865', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14650', '960102401', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14651', '960167937', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14652', '960233473', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14653', '960299009', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14654', '960364545', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14655', '960430081', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14656', '960495617', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14657', '960561153', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14658', '960626689', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14659', '960692225', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14660', '960757761', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14661', '960823297', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14662', '960888833', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14663', '960954369', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14664', '961019905', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14665', '961085441', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14666', '961150977', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14667', '961216513', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14668', '961282049', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14669', '961347585', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14670', '961413121', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14671', '961478657', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14672', '961544193', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14673', '961609729', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14674', '961675265', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14675', '961740801', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 5.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14676', '961806337', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14677', '961871873', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14678', '961937409', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14679', '962002945', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14680', '962068481', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14681', '962134017', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped', '1');
INSERT INTO `zeus_augment_data` VALUES ('14682', '962199553', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14683', '962265089', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14684', '962330625', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14685', '962396161', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14686', '962461697', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14687', '962527233', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14688', '962592769', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14689', '962658305', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14690', '962723841', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14691', '962789377', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14692', '962854913', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14693', '962920449', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14694', '962985985', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14695', '963051521', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14696', '963117057', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14697', '963182593', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14698', '963248129', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14699', '963313665', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14700', '963379201', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14701', '963444737', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14702', '963510273', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14703', '963575809', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14704', '963641345', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 49.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14705', '963706881', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14706', '963772417', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14707', '963837953', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14708', '963903489', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14709', '963969025', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14710', '964034561', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 49.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14711', '964100097', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14712', '964165633', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14713', '964231169', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14714', '964296705', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14715', '964362241', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14716', '964427777', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14717', '964493313', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14718', '964558849', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14719', '964624385', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14720', '964689921', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14721', '964755457', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14722', '964820993', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14723', '964886529', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14724', '964952065', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 49.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14725', '965017601', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14726', '965083137', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14727', '965148673', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14728', '965214209', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14729', '965279745', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14730', '965345281', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14731', '965410817', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14732', '965476353', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14733', '965541889', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14734', '965607425', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14735', '965672961', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14736', '965738497', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14737', '965804033', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14738', '965869569', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14739', '965935105', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14740', '966000641', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14741', '966066177', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14742', '966131713', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14743', '966197249', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14744', '966262785', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14745', '966328321', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14746', '966393857', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 234.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14747', '966459393', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 467.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14748', '966524929', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14749', '966590465', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 234.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14750', '966656001', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 467.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14751', '966721537', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14752', '966787073', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14753', '966852609', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14754', '966918145', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14755', '966983681', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14756', '967049217', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14757', '967114753', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14758', '967180289', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14759', '967245825', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 289.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14760', '967311361', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14761', '967376897', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14762', '967442433', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14763', '967507969', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 234.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14764', '967573505', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 234.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14765', '967639041', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 467.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14766', '967704577', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14767', '967770113', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14768', '967835649', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14769', '967901185', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14770', '967966721', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14771', '968032257', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 337.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14772', '968097793', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14773', '968163329', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 60 temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14774', '968228865', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14775', '968294401', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 60 temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14776', '968359937', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 40 temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14777', '968425473', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14778', '968491009', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 73.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14779', '968556545', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14780', '968622081', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 44.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14781', '968687617', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '2');
INSERT INTO `zeus_augment_data` VALUES ('14782', '968753153', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14783', '968818689', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14784', '968884225', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14785', '968949761', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14786', '969015297', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14787', '969080833', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14788', '969146369', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14789', '969211905', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14790', '969277441', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14791', '969342977', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14792', '969408513', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14793', '969474049', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14794', '969539585', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14795', '969605121', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14796', '969670657', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 33.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14797', '969736193', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14798', '969801729', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14799', '969867265', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14800', '969932801', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14801', '969998337', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14802', '970063873', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14803', '970129409', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14804', '970194945', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14805', '970260481', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14806', '970326017', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14807', '970391553', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14808', '970457089', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14809', '970522625', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14810', '970588161', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 1 doors with 100% probability, level 2 doors with 75% probability, and chests below level 40 with 90% probability. Requires 5 Keys of a Thief.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14811', '970653697', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 53.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14812', '970719233', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14813', '970784769', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 66.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14814', '970850305', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14815', '970915841', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14816', '970981377', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14817', '971046913', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14818', '971112449', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14819', '971177985', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14820', '971243521', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14821', '971309057', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14822', '971374593', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14823', '971440129', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14824', '971505665', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14825', '971571201', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14826', '971636737', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14827', '971702273', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14828', '971767809', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14829', '971833345', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14830', '971898881', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14831', '971964417', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14832', '972029953', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14833', '972095489', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14834', '972161025', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14835', '972226561', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14836', '972292097', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14837', '972357633', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14838', '972423169', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14839', '972488705', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14840', '972554241', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14841', '972619777', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14842', '972685313', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14843', '972750849', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14844', '972816385', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14845', '972881921', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14846', '972947457', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14847', '973012993', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14848', '973078529', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14849', '973144065', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14850', '973209601', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14851', '973275137', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14852', '973340673', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14853', '973406209', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 5.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14854', '973471745', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14855', '973537281', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14856', '973602817', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14857', '973668353', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14858', '973733889', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14859', '973799425', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14860', '973864961', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14861', '973930497', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14862', '973996033', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14863', '974061569', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14864', '974127105', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14865', '974192641', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14866', '974258177', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14867', '974323713', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14868', '974389249', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14869', '974454785', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14870', '974520321', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14871', '974585857', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14872', '974651393', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14873', '974716929', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14874', '974782465', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14875', '974848001', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14876', '974913537', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14877', '974979073', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14878', '975044609', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14879', '975110145', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14880', '975175681', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14881', '975241217', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14882', '975306753', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 53.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14883', '975372289', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14884', '975437825', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14885', '975503361', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14886', '975568897', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14887', '975634433', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14888', '975699969', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 53.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14889', '975765505', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14890', '975831041', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14891', '975896577', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14892', '975962113', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14893', '976027649', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14894', '976093185', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14895', '976158721', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14896', '976224257', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14897', '976289793', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14898', '976355329', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14899', '976420865', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14900', '976486401', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14901', '976551937', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14902', '976617473', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 53.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14903', '976683009', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14904', '976748545', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14905', '976814081', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14906', '976879617', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14907', '976945153', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14908', '977010689', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14909', '977076225', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14910', '977141761', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14911', '977207297', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14912', '977272833', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14913', '977338369', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14914', '977403905', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14915', '977469441', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14916', '977534977', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('14917', '977600513', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14918', '977666049', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14919', '977731585', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14920', '977797121', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14921', '977862657', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14922', '977928193', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14923', '977993729', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14924', '978059265', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 248.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14925', '978124801', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 495.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14926', '978190337', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14927', '978255873', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 248.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14928', '978321409', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 495.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14929', '978386945', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14930', '978452481', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14931', '978518017', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14932', '978583553', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14933', '978649089', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14934', '978714625', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14935', '978780161', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14936', '978845697', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14937', '978911233', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 313.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14938', '978976769', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14939', '979042305', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14940', '979107841', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14941', '979173377', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 248.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14942', '979238913', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 248.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14943', '979304449', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 495.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14944', '979369985', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14945', '979435521', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14946', '979501057', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14947', '979566593', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14948', '979632129', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14949', '979697665', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 365.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14950', '979763201', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14951', '979828737', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 90 temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14952', '979894273', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14953', '979959809', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 90 temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14954', '980025345', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 60 temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14955', '980090881', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14956', '980156417', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 79.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14957', '980221953', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14958', '980287489', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 47.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14959', '980353025', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '3');
INSERT INTO `zeus_augment_data` VALUES ('14960', '980418561', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14961', '980484097', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14962', '980549633', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14963', '980615169', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14964', '980680705', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14965', '980746241', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14966', '980811777', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('14967', '980877313', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 20% of additional Exp.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14968', '980942849', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14969', '981008385', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14970', '981073921', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14971', '981139457', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14972', '981204993', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14973', '981270529', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14974', '981336065', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 36.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14975', '981401601', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14976', '981467137', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14977', '981532673', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14978', '981598209', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14979', '981663745', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14980', '981729281', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14981', '981794817', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14982', '981860353', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14983', '981925889', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14984', '981991425', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14985', '982056961', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14986', '982122497', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14987', '982188033', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14988', '982253569', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens doors below level 2 with 100% probability, level 3 doors with 5% probability, and chests below level 44 with 90% probability. Requires 6 Keys of a Thief.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14989', '982319105', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 57.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14990', '982384641', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14991', '982450177', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 72.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14992', '982515713', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14993', '982581249', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14994', '982646785', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14995', '982712321', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14996', '982777857', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14997', '982843393', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14998', '982908929', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('14999', '982974465', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15000', '983040001', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15001', '983105537', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15002', '983171073', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15003', '983236609', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15004', '983302145', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15005', '983367681', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15006', '983433217', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15007', '983498753', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15008', '983564289', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15009', '983629825', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15010', '983695361', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15011', '983760897', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15012', '983826433', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15013', '983891969', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15014', '983957505', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15015', '984023041', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15016', '984088577', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15017', '984154113', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15018', '984219649', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15019', '984285185', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15020', '984350721', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15021', '984416257', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15022', '984481793', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15023', '984547329', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15024', '984612865', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15025', '984678401', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15026', '984743937', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15027', '984809473', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15028', '984875009', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15029', '984940545', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15030', '985006081', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15031', '985071617', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 6.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15032', '985137153', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15033', '985202689', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15034', '985268225', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15035', '985333761', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15036', '985399297', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15037', '985464833', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15038', '985530369', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15039', '985595905', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15040', '985661441', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15041', '985726977', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15042', '985792513', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15043', '985858049', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15044', '985923585', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15045', '985989121', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15046', '986054657', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15047', '986120193', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15048', '986185729', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15049', '986251265', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15050', '986316801', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15051', '986382337', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15052', '986447873', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15053', '986513409', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15054', '986578945', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15055', '986644481', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15056', '986710017', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15057', '986775553', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15058', '986841089', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15059', '986906625', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15060', '986972161', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 57.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15061', '987037697', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15062', '987103233', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15063', '987168769', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15064', '987234305', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15065', '987299841', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15066', '987365377', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 57.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15067', '987430913', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15068', '987496449', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15069', '987561985', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15070', '987627521', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15071', '987693057', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15072', '987758593', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15073', '987824129', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15074', '987889665', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15075', '987955201', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15076', '988020737', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15077', '988086273', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15078', '988151809', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15079', '988217345', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15080', '988282881', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 57.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15081', '988348417', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15082', '988413953', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15083', '988479489', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15084', '988545025', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15085', '988610561', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15086', '988676097', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15087', '988741633', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15088', '988807169', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15089', '988872705', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15090', '988938241', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15091', '989003777', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15092', '989069313', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15093', '989134849', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15094', '989200385', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('15095', '989265921', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15096', '989331457', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15097', '989396993', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15098', '989462529', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15099', '989528065', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15100', '989593601', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15101', '989659137', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15102', '989724673', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 262.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15103', '989790209', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 523.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15104', '989855745', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15105', '989921281', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 262.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15106', '989986817', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 523.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15107', '990052353', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15108', '990117889', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15109', '990183425', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15110', '990248961', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15111', '990314497', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15112', '990380033', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15113', '990445569', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15114', '990511105', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15115', '990576641', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 337.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15116', '990642177', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15117', '990707713', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15118', '990773249', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15119', '990838785', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 262.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15120', '990904321', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 262.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15121', '990969857', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 523.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15122', '991035393', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15123', '991100929', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15124', '991166465', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15125', '991232001', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15126', '991297537', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15127', '991363073', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 393.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15128', '991428609', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15129', '991494145', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 120 temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15130', '991559681', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15131', '991625217', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 120 temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15132', '991690753', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 80 temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15133', '991756289', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15134', '991821825', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 84.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15135', '991887361', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15136', '991952897', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 50.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15137', '992018433', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '4');
INSERT INTO `zeus_augment_data` VALUES ('15138', '992083969', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15139', '992149505', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15140', '992215041', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15141', '992280577', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15142', '992346113', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15143', '992411649', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15144', '992477185', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15145', '992542721', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 30% of additional Exp.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15146', '992608257', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15147', '992673793', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15148', '992739329', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15149', '992804865', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15150', '992870401', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15151', '992935937', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15152', '993001473', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 39.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15153', '993067009', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15154', '993132545', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15155', '993198081', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15156', '993263617', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15157', '993329153', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15158', '993394689', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15159', '993460225', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15160', '993525761', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15161', '993591297', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15162', '993656833', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15163', '993722369', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15164', '993787905', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15165', '993853441', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15166', '993918977', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens doors below level 2 with 100% probability, level 3 doors with 30% probability, and chests below level 48 with 90% probability. Requires 7 Keys of a Thief.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15167', '993984513', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 61.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15168', '994050049', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15169', '994115585', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 77.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15170', '994181121', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15171', '994246657', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15172', '994312193', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15173', '994377729', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15174', '994443265', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15175', '994508801', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15176', '994574337', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15177', '994639873', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15178', '994705409', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15179', '994770945', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15180', '994836481', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15181', '994902017', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15182', '994967553', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15183', '995033089', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15184', '995098625', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15185', '995164161', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15186', '995229697', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15187', '995295233', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15188', '995360769', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15189', '995426305', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15190', '995491841', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15191', '995557377', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15192', '995622913', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15193', '995688449', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15194', '995753985', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15195', '995819521', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15196', '995885057', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15197', '995950593', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15198', '996016129', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15199', '996081665', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15200', '996147201', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15201', '996212737', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15202', '996278273', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15203', '996343809', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15204', '996409345', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15205', '996474881', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15206', '996540417', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15207', '996605953', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15208', '996671489', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15209', '996737025', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 6.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15210', '996802561', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15211', '996868097', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15212', '996933633', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15213', '996999169', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15214', '997064705', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15215', '997130241', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15216', '997195777', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15217', '997261313', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15218', '997326849', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15219', '997392385', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15220', '997457921', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15221', '997523457', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15222', '997588993', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15223', '997654529', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15224', '997720065', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15225', '997785601', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15226', '997851137', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15227', '997916673', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15228', '997982209', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15229', '998047745', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15230', '998113281', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15231', '998178817', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15232', '998244353', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15233', '998309889', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15234', '998375425', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15235', '998440961', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15236', '998506497', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15237', '998572033', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15238', '998637569', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 61.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15239', '998703105', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15240', '998768641', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15241', '998834177', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15242', '998899713', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15243', '998965249', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15244', '999030785', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 61.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15245', '999096321', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15246', '999161857', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15247', '999227393', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15248', '999292929', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15249', '999358465', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15250', '999424001', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15251', '999489537', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15252', '999555073', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15253', '999620609', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15254', '999686145', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15255', '999751681', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15256', '999817217', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15257', '999882753', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15258', '999948289', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 61.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15259', '1000013825', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15260', '1000079361', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15261', '1000144897', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15262', '1000210433', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15263', '1000275969', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15264', '1000341505', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15265', '1000407041', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15266', '1000472577', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15267', '1000538113', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15268', '1000603649', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15269', '1000669185', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '4');
INSERT INTO `zeus_augment_data` VALUES ('15270', '1000734721', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15271', '1000800257', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15272', '1000865793', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15273', '1000931329', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15274', '1000996865', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15275', '1001062401', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15276', '1001127937', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15277', '1001193473', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15278', '1001259009', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15279', '1001324545', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15280', '1001390081', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 275.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15281', '1001455617', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 549.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15282', '1001521153', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15283', '1001586689', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 275.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15284', '1001652225', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 549.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15285', '1001717761', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15286', '1001783297', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15287', '1001848833', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15288', '1001914369', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15289', '1001979905', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15290', '1002045441', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15291', '1002110977', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15292', '1002176513', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15293', '1002242049', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 361.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15294', '1002307585', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15295', '1002373121', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15296', '1002438657', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15297', '1002504193', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 275.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15298', '1002569729', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 275.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15299', '1002635265', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 549.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15300', '1002700801', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15301', '1002766337', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15302', '1002831873', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15303', '1002897409', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15304', '1002962945', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15305', '1003028481', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 421.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15306', '1003094017', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15307', '1003159553', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 150 temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15308', '1003225089', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15309', '1003290625', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 150 temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15310', '1003356161', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 100 temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15311', '1003421697', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15312', '1003487233', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 90.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15313', '1003552769', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15314', '1003618305', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 53.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15315', '1003683841', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '5');
INSERT INTO `zeus_augment_data` VALUES ('15316', '1003749377', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15317', '1003814913', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15318', '1003880449', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15319', '1003945985', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15320', '1004011521', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15321', '1004077057', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15322', '1004142593', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15323', '1004208129', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 40% of additional Exp.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15324', '1004273665', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15325', '1004339201', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15326', '1004404737', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15327', '1004470273', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15328', '1004535809', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15329', '1004601345', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15330', '1004666881', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 41.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15331', '1004732417', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15332', '1004797953', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15333', '1004863489', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15334', '1004929025', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15335', '1004994561', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15336', '1005060097', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15337', '1005125633', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15338', '1005191169', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15339', '1005256705', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15340', '1005322241', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15341', '1005387777', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15342', '1005453313', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15343', '1005518849', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15344', '1005584385', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens doors below level 2 with 100% probability, level 3 doors with 75% probability, and chests below level 52 with 90% probability. Requires 8 Keys of a Thief.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15345', '1005649921', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 66.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15346', '1005715457', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15347', '1005780993', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 82.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15348', '1005846529', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15349', '1005912065', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15350', '1005977601', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15351', '1006043137', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15352', '1006108673', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15353', '1006174209', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15354', '1006239745', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15355', '1006305281', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15356', '1006370817', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15357', '1006436353', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15358', '1006501889', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15359', '1006567425', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15360', '1006632961', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15361', '1006698497', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15362', '1006764033', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15363', '1006829569', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15364', '1006895105', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15365', '1006960641', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15366', '1007026177', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15367', '1007091713', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15368', '1007157249', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15369', '1007222785', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15370', '1007288321', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15371', '1007353857', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15372', '1007419393', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15373', '1007484929', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15374', '1007550465', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15375', '1007616001', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15376', '1007681537', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15377', '1007747073', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15378', '1007812609', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15379', '1007878145', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15380', '1007943681', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15381', '1008009217', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15382', '1008074753', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15383', '1008140289', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15384', '1008205825', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15385', '1008271361', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15386', '1008336897', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15387', '1008402433', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 6.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15388', '1008467969', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15389', '1008533505', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15390', '1008599041', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15391', '1008664577', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15392', '1008730113', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15393', '1008795649', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15394', '1008861185', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15395', '1008926721', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15396', '1008992257', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15397', '1009057793', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15398', '1009123329', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15399', '1009188865', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15400', '1009254401', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15401', '1009319937', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15402', '1009385473', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15403', '1009451009', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15404', '1009516545', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15405', '1009582081', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15406', '1009647617', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15407', '1009713153', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15408', '1009778689', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15409', '1009844225', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15410', '1009909761', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15411', '1009975297', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15412', '1010040833', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15413', '1010106369', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15414', '1010171905', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15415', '1010237441', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15416', '1010302977', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 66.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15417', '1010368513', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15418', '1010434049', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15419', '1010499585', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15420', '1010565121', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15421', '1010630657', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15422', '1010696193', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 66.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15423', '1010761729', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15424', '1010827265', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15425', '1010892801', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15426', '1010958337', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15427', '1011023873', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15428', '1011089409', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15429', '1011154945', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15430', '1011220481', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15431', '1011286017', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15432', '1011351553', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15433', '1011417089', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15434', '1011482625', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15435', '1011548161', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15436', '1011613697', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 66.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15437', '1011679233', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15438', '1011744769', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15439', '1011810305', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15440', '1011875841', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15441', '1011941377', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15442', '1012006913', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15443', '1012072449', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15444', '1012137985', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15445', '1012203521', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15446', '1012269057', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15447', '1012334593', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '5');
INSERT INTO `zeus_augment_data` VALUES ('15448', '1012400129', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15449', '1012465665', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15450', '1012531201', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15451', '1012596737', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15452', '1012662273', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15453', '1012727809', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15454', '1012793345', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15455', '1012858881', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15456', '1012924417', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15457', '1012989953', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15458', '1013055489', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 287.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15459', '1013121025', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 574.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15460', '1013186561', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15461', '1013252097', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 287.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15462', '1013317633', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 574.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15463', '1013383169', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15464', '1013448705', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15465', '1013514241', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15466', '1013579777', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15467', '1013645313', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15468', '1013710849', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15469', '1013776385', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15470', '1013841921', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15471', '1013907457', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 384.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15472', '1013972993', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15473', '1014038529', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15474', '1014104065', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15475', '1014169601', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 287.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15476', '1014235137', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 287.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15477', '1014300673', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 574.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15478', '1014366209', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15479', '1014431745', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15480', '1014497281', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15481', '1014562817', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15482', '1014628353', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15483', '1014693889', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 448.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15484', '1014759425', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15485', '1014824961', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 180 temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15486', '1014890497', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15487', '1014956033', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 180 temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15488', '1015021569', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 120 temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15489', '1015087105', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15490', '1015152641', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 96.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15491', '1015218177', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15492', '1015283713', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 57.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15493', '1015349249', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '6');
INSERT INTO `zeus_augment_data` VALUES ('15494', '1015414785', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15495', '1015480321', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15496', '1015545857', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15497', '1015611393', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15498', '1015676929', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15499', '1015742465', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15500', '1015808001', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15501', '1015873537', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 50% of additional Exp.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15502', '1015939073', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15503', '1016004609', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15504', '1016070145', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15505', '1016135681', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15506', '1016201217', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15507', '1016266753', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15508', '1016332289', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 44.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15509', '1016397825', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15510', '1016463361', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15511', '1016528897', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15512', '1016594433', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15513', '1016659969', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15514', '1016725505', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15515', '1016791041', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15516', '1016856577', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15517', '1016922113', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15518', '1016987649', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15519', '1017053185', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15520', '1017118721', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15521', '1017184257', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15522', '1017249793', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 3 doors with 100% probability and chests below level 56 with 90% probability. Requires 10 Keys of a Thief.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15523', '1017315329', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 70.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15524', '1017380865', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15525', '1017446401', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 87.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15526', '1017511937', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15527', '1017577473', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15528', '1017643009', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15529', '1017708545', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15530', '1017774081', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15531', '1017839617', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15532', '1017905153', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15533', '1017970689', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15534', '1018036225', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15535', '1018101761', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15536', '1018167297', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15537', '1018232833', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15538', '1018298369', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15539', '1018363905', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15540', '1018429441', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15541', '1018494977', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15542', '1018560513', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15543', '1018626049', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15544', '1018691585', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15545', '1018757121', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15546', '1018822657', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15547', '1018888193', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15548', '1018953729', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15549', '1019019265', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15550', '1019084801', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15551', '1019150337', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15552', '1019215873', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15553', '1019281409', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15554', '1019346945', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15555', '1019412481', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15556', '1019478017', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15557', '1019543553', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15558', '1019609089', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15559', '1019674625', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15560', '1019740161', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15561', '1019805697', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15562', '1019871233', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15563', '1019936769', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15564', '1020002305', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15565', '1020067841', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 7.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15566', '1020133377', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15567', '1020198913', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15568', '1020264449', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15569', '1020329985', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15570', '1020395521', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15571', '1020461057', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15572', '1020526593', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15573', '1020592129', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15574', '1020657665', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15575', '1020723201', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15576', '1020788737', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15577', '1020854273', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15578', '1020919809', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15579', '1020985345', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15580', '1021050881', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15581', '1021116417', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15582', '1021181953', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15583', '1021247489', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15584', '1021313025', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15585', '1021378561', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15586', '1021444097', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15587', '1021509633', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15588', '1021575169', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15589', '1021640705', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15590', '1021706241', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15591', '1021771777', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15592', '1021837313', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15593', '1021902849', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15594', '1021968385', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 70.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15595', '1022033921', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15596', '1022099457', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15597', '1022164993', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15598', '1022230529', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15599', '1022296065', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15600', '1022361601', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 70.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15601', '1022427137', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15602', '1022492673', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15603', '1022558209', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15604', '1022623745', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15605', '1022689281', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15606', '1022754817', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15607', '1022820353', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15608', '1022885889', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15609', '1022951425', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15610', '1023016961', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15611', '1023082497', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15612', '1023148033', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15613', '1023213569', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15614', '1023279105', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 70.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15615', '1023344641', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15616', '1023410177', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15617', '1023475713', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15618', '1023541249', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15619', '1023606785', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15620', '1023672321', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15621', '1023737857', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15622', '1023803393', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15623', '1023868929', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15624', '1023934465', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15625', '1024000001', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '6');
INSERT INTO `zeus_augment_data` VALUES ('15626', '1024065537', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15627', '1024131073', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15628', '1024196609', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15629', '1024262145', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15630', '1024327681', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15631', '1024393217', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15632', '1024458753', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15633', '1024524289', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15634', '1024589825', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15635', '1024655361', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15636', '1024720897', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 299.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15637', '1024786433', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 597.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15638', '1024851969', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15639', '1024917505', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 299.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15640', '1024983041', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 597.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15641', '1025048577', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15642', '1025114113', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15643', '1025179649', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15644', '1025245185', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15645', '1025310721', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15646', '1025376257', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15647', '1025441793', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15648', '1025507329', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15649', '1025572865', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 406.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15650', '1025638401', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15651', '1025703937', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15652', '1025769473', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15653', '1025835009', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 299.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15654', '1025900545', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 299.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15655', '1025966081', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 597.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15656', '1026031617', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15657', '1026097153', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15658', '1026162689', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15659', '1026228225', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15660', '1026293761', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15661', '1026359297', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 474.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15662', '1026424833', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15663', '1026490369', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 210 temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15664', '1026555905', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15665', '1026621441', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 210 temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15666', '1026686977', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 140 temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15667', '1026752513', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15668', '1026818049', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 101.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15669', '1026883585', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15670', '1026949121', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 59.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15671', '1027014657', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '7');
INSERT INTO `zeus_augment_data` VALUES ('15672', '1027080193', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15673', '1027145729', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15674', '1027211265', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15675', '1027276801', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15676', '1027342337', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15677', '1027407873', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15678', '1027473409', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15679', '1027538945', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 55% of additional Exp.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15680', '1027604481', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15681', '1027670017', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15682', '1027735553', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15683', '1027801089', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15684', '1027866625', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15685', '1027932161', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15686', '1027997697', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 46.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15687', '1028063233', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15688', '1028128769', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15689', '1028194305', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15690', '1028259841', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15691', '1028325377', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15692', '1028390913', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15693', '1028456449', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15694', '1028521985', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15695', '1028587521', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15696', '1028653057', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15697', '1028718593', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15698', '1028784129', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15699', '1028849665', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15700', '1028915201', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 3 doors with 100% probability and chests below level 60 with 90% probability. Requires 11 Keys of a Thief.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15701', '1028980737', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 74.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15702', '1029046273', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15703', '1029111809', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 92.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15704', '1029177345', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15705', '1029242881', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15706', '1029308417', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15707', '1029373953', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15708', '1029439489', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15709', '1029505025', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15710', '1029570561', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15711', '1029636097', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15712', '1029701633', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15713', '1029767169', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15714', '1029832705', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15715', '1029898241', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15716', '1029963777', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15717', '1030029313', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15718', '1030094849', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15719', '1030160385', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15720', '1030225921', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15721', '1030291457', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15722', '1030356993', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15723', '1030422529', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15724', '1030488065', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15725', '1030553601', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15726', '1030619137', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15727', '1030684673', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15728', '1030750209', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15729', '1030815745', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15730', '1030881281', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15731', '1030946817', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15732', '1031012353', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15733', '1031077889', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15734', '1031143425', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15735', '1031208961', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15736', '1031274497', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15737', '1031340033', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15738', '1031405569', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15739', '1031471105', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15740', '1031536641', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15741', '1031602177', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15742', '1031667713', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15743', '1031733249', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 7.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15744', '1031798785', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15745', '1031864321', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15746', '1031929857', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15747', '1031995393', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15748', '1032060929', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15749', '1032126465', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15750', '1032192001', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15751', '1032257537', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15752', '1032323073', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15753', '1032388609', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15754', '1032454145', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15755', '1032519681', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15756', '1032585217', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15757', '1032650753', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15758', '1032716289', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15759', '1032781825', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15760', '1032847361', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15761', '1032912897', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15762', '1032978433', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15763', '1033043969', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15764', '1033109505', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15765', '1033175041', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15766', '1033240577', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15767', '1033306113', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15768', '1033371649', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15769', '1033437185', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15770', '1033502721', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15771', '1033568257', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15772', '1033633793', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 74.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15773', '1033699329', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15774', '1033764865', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15775', '1033830401', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15776', '1033895937', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15777', '1033961473', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15778', '1034027009', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 74.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15779', '1034092545', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15780', '1034158081', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15781', '1034223617', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15782', '1034289153', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15783', '1034354689', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15784', '1034420225', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15785', '1034485761', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15786', '1034551297', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15787', '1034616833', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15788', '1034682369', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15789', '1034747905', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15790', '1034813441', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15791', '1034878977', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15792', '1034944513', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 74.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15793', '1035010049', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15794', '1035075585', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15795', '1035141121', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15796', '1035206657', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15797', '1035272193', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15798', '1035337729', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15799', '1035403265', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15800', '1035468801', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15801', '1035534337', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15802', '1035599873', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15803', '1035665409', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '7');
INSERT INTO `zeus_augment_data` VALUES ('15804', '1035730945', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15805', '1035796481', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15806', '1035862017', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15807', '1035927553', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15808', '1035993089', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15809', '1036058625', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15810', '1036124161', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15811', '1036189697', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15812', '1036255233', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15813', '1036320769', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15814', '1036386305', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 309.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15815', '1036451841', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 617.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15816', '1036517377', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15817', '1036582913', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 309.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15818', '1036648449', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 617.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15819', '1036713985', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15820', '1036779521', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15821', '1036845057', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15822', '1036910593', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15823', '1036976129', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15824', '1037041665', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15825', '1037107201', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15826', '1037172737', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15827', '1037238273', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 427.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15828', '1037303809', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15829', '1037369345', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15830', '1037434881', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15831', '1037500417', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 309.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15832', '1037565953', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 309.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15833', '1037631489', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 617.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15834', '1037697025', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15835', '1037762561', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15836', '1037828097', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15837', '1037893633', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15838', '1037959169', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15839', '1038024705', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 498.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15840', '1038090241', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15841', '1038155777', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 240 temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15842', '1038221313', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15843', '1038286849', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 240 temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15844', '1038352385', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 160 temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15845', '1038417921', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15846', '1038483457', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 107.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15847', '1038548993', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15848', '1038614529', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 62.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15849', '1038680065', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '8');
INSERT INTO `zeus_augment_data` VALUES ('15850', '1038745601', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15851', '1038811137', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15852', '1038876673', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15853', '1038942209', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15854', '1039007745', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15855', '1039073281', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15856', '1039138817', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15857', '1039204353', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 60% of additional Exp.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15858', '1039269889', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15859', '1039335425', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15860', '1039400961', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15861', '1039466497', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15862', '1039532033', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15863', '1039597569', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15864', '1039663105', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 49.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15865', '1039728641', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15866', '1039794177', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15867', '1039859713', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15868', '1039925249', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15869', '1039990785', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15870', '1040056321', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15871', '1040121857', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15872', '1040187393', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15873', '1040252929', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15874', '1040318465', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15875', '1040384001', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15876', '1040449537', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15877', '1040515073', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15878', '1040580609', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 3 doors with 100% probability and chests below level 64 with 90% probability. Requires 13 Keys of a Thief.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15879', '1040646145', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 78.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15880', '1040711681', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15881', '1040777217', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 97.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15882', '1040842753', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15883', '1040908289', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15884', '1040973825', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15885', '1041039361', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15886', '1041104897', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15887', '1041170433', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15888', '1041235969', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15889', '1041301505', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15890', '1041367041', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15891', '1041432577', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15892', '1041498113', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15893', '1041563649', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15894', '1041629185', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15895', '1041694721', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15896', '1041760257', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15897', '1041825793', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15898', '1041891329', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15899', '1041956865', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15900', '1042022401', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15901', '1042087937', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15902', '1042153473', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15903', '1042219009', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15904', '1042284545', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15905', '1042350081', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15906', '1042415617', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15907', '1042481153', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15908', '1042546689', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15909', '1042612225', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15910', '1042677761', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15911', '1042743297', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15912', '1042808833', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15913', '1042874369', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15914', '1042939905', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15915', '1043005441', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15916', '1043070977', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15917', '1043136513', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15918', '1043202049', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15919', '1043267585', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15920', '1043333121', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15921', '1043398657', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 7.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15922', '1043464193', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15923', '1043529729', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15924', '1043595265', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15925', '1043660801', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15926', '1043726337', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15927', '1043791873', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15928', '1043857409', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15929', '1043922945', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15930', '1043988481', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15931', '1044054017', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15932', '1044119553', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15933', '1044185089', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15934', '1044250625', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15935', '1044316161', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15936', '1044381697', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15937', '1044447233', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15938', '1044512769', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15939', '1044578305', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15940', '1044643841', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15941', '1044709377', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15942', '1044774913', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15943', '1044840449', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15944', '1044905985', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15945', '1044971521', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15946', '1045037057', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15947', '1045102593', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15948', '1045168129', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15949', '1045233665', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15950', '1045299201', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 78.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15951', '1045364737', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15952', '1045430273', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15953', '1045495809', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15954', '1045561345', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15955', '1045626881', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15956', '1045692417', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 78.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15957', '1045757953', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15958', '1045823489', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15959', '1045889025', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15960', '1045954561', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15961', '1046020097', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15962', '1046085633', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15963', '1046151169', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15964', '1046216705', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15965', '1046282241', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15966', '1046347777', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15967', '1046413313', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15968', '1046478849', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15969', '1046544385', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15970', '1046609921', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 78.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15971', '1046675457', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15972', '1046740993', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15973', '1046806529', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15974', '1046872065', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15975', '1046937601', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15976', '1047003137', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15977', '1047068673', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15978', '1047134209', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15979', '1047199745', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15980', '1047265281', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15981', '1047330817', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '8');
INSERT INTO `zeus_augment_data` VALUES ('15982', '1047396353', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15983', '1047461889', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15984', '1047527425', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('15985', '1047592961', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15986', '1047658497', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15987', '1047724033', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15988', '1047789569', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15989', '1047855105', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15990', '1047920641', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15991', '1047986177', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('15992', '1048051713', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 318.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15993', '1048117249', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 635.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15994', '1048182785', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15995', '1048248321', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 318.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15996', '1048313857', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 635.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15997', '1048379393', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15998', '1048444929', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('15999', '1048510465', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16000', '1048576001', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16001', '1048641537', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16002', '1048707073', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16003', '1048772609', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16004', '1048838145', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16005', '1048903681', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 446.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16006', '1048969217', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16007', '1049034753', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16008', '1049100289', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16009', '1049165825', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 318.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16010', '1049231361', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 318.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16011', '1049296897', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 635.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16012', '1049362433', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16013', '1049427969', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16014', '1049493505', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16015', '1049559041', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16016', '1049624577', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16017', '1049690113', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 520.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16018', '1049755649', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16019', '1049821185', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 270 temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16020', '1049886721', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16021', '1049952257', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 270 temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16022', '1050017793', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 180 temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16023', '1050083329', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16024', '1050148865', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 112.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16025', '1050214401', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16026', '1050279937', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 65.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16027', '1050345473', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '9');
INSERT INTO `zeus_augment_data` VALUES ('16028', '1050411009', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16029', '1050476545', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16030', '1050542081', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16031', '1050607617', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16032', '1050673153', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16033', '1050738689', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16034', '1050804225', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16035', '1050869761', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 65% of additional Exp.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16036', '1050935297', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16037', '1051000833', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16038', '1051066369', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16039', '1051131905', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16040', '1051197441', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16041', '1051262977', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16042', '1051328513', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 51.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16043', '1051394049', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16044', '1051459585', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16045', '1051525121', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16046', '1051590657', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16047', '1051656193', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16048', '1051721729', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16049', '1051787265', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16050', '1051852801', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16051', '1051918337', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16052', '1051983873', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16053', '1052049409', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16054', '1052114945', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16055', '1052180481', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16056', '1052246017', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 3 doors with 100% probability and chests below level 68 with 90% probability. Requires 15 Keys of a Thief.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16057', '1052311553', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 82.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16058', '1052377089', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16059', '1052442625', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 102.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16060', '1052508161', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16061', '1052573697', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16062', '1052639233', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16063', '1052704769', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16064', '1052770305', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16065', '1052835841', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16066', '1052901377', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16067', '1052966913', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16068', '1053032449', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16069', '1053097985', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16070', '1053163521', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16071', '1053229057', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16072', '1053294593', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16073', '1053360129', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16074', '1053425665', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16075', '1053491201', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16076', '1053556737', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16077', '1053622273', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16078', '1053687809', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16079', '1053753345', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16080', '1053818881', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16081', '1053884417', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16082', '1053949953', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16083', '1054015489', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16084', '1054081025', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16085', '1054146561', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16086', '1054212097', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16087', '1054277633', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16088', '1054343169', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16089', '1054408705', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16090', '1054474241', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16091', '1054539777', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16092', '1054605313', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16093', '1054670849', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16094', '1054736385', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16095', '1054801921', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16096', '1054867457', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16097', '1054932993', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16098', '1054998529', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16099', '1055064065', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 8.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16100', '1055129601', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16101', '1055195137', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16102', '1055260673', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16103', '1055326209', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16104', '1055391745', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16105', '1055457281', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16106', '1055522817', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16107', '1055588353', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16108', '1055653889', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16109', '1055719425', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16110', '1055784961', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16111', '1055850497', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16112', '1055916033', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16113', '1055981569', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16114', '1056047105', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16115', '1056112641', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16116', '1056178177', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16117', '1056243713', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16118', '1056309249', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16119', '1056374785', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16120', '1056440321', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16121', '1056505857', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16122', '1056571393', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16123', '1056636929', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16124', '1056702465', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16125', '1056768001', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16126', '1056833537', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16127', '1056899073', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16128', '1056964609', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 82.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16129', '1057030145', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16130', '1057095681', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16131', '1057161217', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16132', '1057226753', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16133', '1057292289', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16134', '1057357825', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 82.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16135', '1057423361', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16136', '1057488897', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16137', '1057554433', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16138', '1057619969', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16139', '1057685505', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16140', '1057751041', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16141', '1057816577', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16142', '1057882113', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16143', '1057947649', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16144', '1058013185', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16145', '1058078721', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16146', '1058144257', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16147', '1058209793', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16148', '1058275329', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 82.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16149', '1058340865', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16150', '1058406401', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16151', '1058471937', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16152', '1058537473', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16153', '1058603009', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16154', '1058668545', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16155', '1058734081', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16156', '1058799617', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16157', '1058865153', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16158', '1058930689', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16159', '1058996225', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16160', '1059061761', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16161', '1059127297', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16162', '1059192833', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16163', '1059258369', 'Active', ' Temporarily increases the size of your head.', '3203', 'Mystery Skill: Active: Increases your head size.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16164', '1059323905', 'Active', ' Temporarily increases your lung capacity.', '3143', 'Kiss of Eva: Active: Increases Lung Capacity temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16165', '1059389441', 'Active', ' Temporarily increases the distance you can fall without sustaining damage.', '3144', 'Acrobatics: Active: Increases the height from which you can jump without sustaining damage temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16166', '1059454977', 'Active', ' Temporarily increases resistance to damage from falling.', '3145', 'Iron Body: Active: Raises resistance to damage from falling.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16167', '1059520513', 'Active', ' Ignites a firecracker.', '3156', 'Firework: Active: Ignites a Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16168', '1059586049', 'Active', ' Play music for a short duration.', '3206', 'Music: Active: Plays music.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16169', '1059651585', 'Active', ' Ignites a large firecracker.', '3157', 'Large Firework: Active: Ignites a Large Firework.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16170', '1059717121', 'Chance', ' Temporarily decreases your target\'s will to attack during a physical attack.', '3081', 'Charm: Chance: Decreases a target\'s urge to attack during a general physical attack. Power 330.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16171', '1059782657', 'Chance', ' Temporarily increases your target\'s will to attack during a physical attack.', '3080', 'Aggression: Chance: Provokes a target to attack during an ordinary physical attack. Power 659.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16172', '1059848193', 'Chance', ' Momentarily decreases your target\'s speed during a physical attack.', '3083', 'Slow: Chance: Momentarily decreases a target\'s speed during an ordinary physical attack. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16173', '1059913729', 'Chance', ' Temporarily decreases your target\'s will to attack during a critical attack.', '3109', 'Aggression Down: Chance: Decreases a target\'s urge to attack during a critical attack. Power 330.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16174', '1059979265', 'Chance', ' Temporarily increases your target\'s will to attack during a critical attack.', '3108', 'Aggression Up: Chance: Increases a target\'s urge to attack during a critical attack. Power 659.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16175', '1060044801', 'Chance', ' Momentarily decreases your target\'s speed during a critical attack.', '3111', 'Slow: Chance: Momentarily decreases the target\'s speed during a critical attack. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16176', '1060110337', 'Chance', ' Momentarily decreases your target\'s speed during a magic attack. Effect 3.', '3096', 'Slow: Chance: Momentarily decreases the target\'s speed during magic use. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16177', '1060175873', 'Passive', ' Increases lung capacity.', '3252', 'Kiss of Eva: Passive: Increases lung capacity when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16178', '1060241409', 'Passive', ' Increases the height from which you can fall without sustaining damage.', '3253', 'Acrobatics: Passive: Increases the height from which you can jump without sustaining damage when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16179', '1060306945', 'Passive', ' Raises resistance to damage from falling.', '3254', 'Iron Body: Passive: Raises resistance to damage from falling when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16180', '1060372481', 'Active', ' Temporarily decreases your target\'s Atk. Spd.', '3188', 'Winter: Active: Temporarily decreases a target\'s Atk. Spd.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16181', '1060438017', 'Active', ' Temporarily increases Dodge.', '3139', 'Agility: Active: Increases Dodge temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16182', '1060503553', 'Active', ' Temporarily causes a target to bleed heavily.', '3196', 'Bleed: Active: Temporarily causes a target to bleed heavily. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16183', '1060569089', 'Active', ' Restores your CP.', '3130', 'Ritual: Active: Regenerates CP. Power 473.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16184', '1060634625', 'Active', ' Inflicts damage by throwing a boulder.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16185', '1060700161', 'Active', ' Momentarily frightens away your target.', '3194', 'Fear: Active: Momentarily throws the target into a state of fear and causes him to flee.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16186', '1060765697', 'Active', ' Detonates a fireball by compressing the air around the caster.', '3173', 'Prominence: Active: Unleashes a flaming attack against the enemies near a target. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16187', '1060831233', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 330.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16188', '1060896769', 'Active', ' Decreases your target\'s will to attack.', '3150', 'Charm: Active: Decreases a target\'s urge to attack. Power 330.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16189', '1060962305', 'Active', ' Increases your target\'s will to attack.', '3149', 'Aggression: Active: Increases the target\'s urge to attack. Power 659.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16190', '1061027841', 'Active', ' Temporarily increases your Accuracy.', '3140', 'Guidance: Active: Increases Accuracy temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16191', '1061093377', 'Active', ' Temporarily holds your target. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3190', 'Hold: Active: Temporarily throws the target into a state of hold. The target cannot be affected by any additional hold attacks while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16192', '1061158913', 'Active', ' Launches a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16193', '1061224449', 'Active', ' Temporarily increases the power of HP recovery magic.', '3138', 'Heal Empower: Active: Increases the power of HP recovery magic temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16194', '1061289985', 'Active', ' Temporarily increases the effectiveness of HP recovery magic.', '3126', 'Prayer: Active: Increases the effectiveness of HP recovery magic temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16195', '1061355521', 'Active', ' Instantly restores your HP.', '3123', 'Heal: Active: Immediately recovers your HP. Power 552.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16196', '1061421057', 'Active', ' Temporarily increases your M. Atk.', '3133', 'Empower: Active: Increases M. Atk. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16197', '1061486593', 'Active', ' Temporarily increases your maximum CP.', '3131', 'Cheer: Active: Increases the Max. CP by 300 temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16198', '1061552129', 'Active', ' Increases your maximum HP temporarily and restores HP by the increased amount.', '3125', 'Battle Roar: Active: Increases the Max. HP temporarily and restores HP by the increased amount.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16199', '1061617665', 'Active', ' Temporarily increases your maximum HP.', '3124', 'Blessed Body: Active: Increases the Max. HP by 300 temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16200', '1061683201', 'Active', ' Temporarily increases your maximum MP.', '3128', 'Blessed Soul: Active: Increases the maximum MP by 200 temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16201', '1061748737', 'Active', ' Temporarily increases your M. Def.', '3136', 'Magic Barrier: Active: Increases M. Def. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16202', '1061814273', 'Active', ' Burns up your enemy\'s MP.', '3154', 'Mana Burn: Active: Burns up the enemy\'s MP. Power 120.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16203', '1061879809', 'Active', ' Increases your MP recharge recovery rate.', '3129', 'Mana Gain: Active: Increases the recharge recover rate of MP.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16204', '1061945345', 'Active', ' Regenerates MP.', '3127', 'Recharge: Active: Regenerates MP. Power 69.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16205', '1062010881', 'Active', ' Unleashes a general attack and temporarily decreases your magic attack power during PvP.', '3172', 'Aura Flare', '10');
INSERT INTO `zeus_augment_data` VALUES ('16206', '1062076417', 'Active', ' Temporarily increases your P. Atk.', '3132', 'Might: Active: Increases P. Atk. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16207', '1062141953', 'Active', ' Temporarily paralyzes a target.', '3192', 'Paralyze: Active: Temporarily throws the target into a state of paralysis.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16208', '1062207489', 'Active', ' Temporarily increases your P. Def.', '3135', 'Shield: Active: Increases P. Def. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16209', '1062273025', 'Active', ' Temporarily poisons your target.', '3195', 'Poison: Active: Temporarily poisons a target. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16210', '1062338561', 'Active', ' Temporarily decreases your target\'s P.Atk. during PvP.', '3137', 'Duel Weakness: Active: Decreases the opponent\'s PVP P. Atk. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16211', '1062404097', 'Active', ' Temporarily increases your P.Atk. during PvP.', '3134', 'Duel Might: Active: Increases PVP P. Atk. temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16212', '1062469633', 'Active', ' Teleports the caster to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3146', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16213', '1062535169', 'Active', ' Resurrects a corpse.', '3160', 'Resurrection: Active: Resurrects a corpse. Restores about 70% of additional Exp.', '9');
INSERT INTO `zeus_augment_data` VALUES ('16214', '1062600705', 'Active', ' Emits an area of effect earth attack.', '3183', 'Stone: Active: Unleashes an earthen attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16215', '1062666241', 'Active', ' Emits an area of effect fire attack.', '3180', 'Prominence: Active: Unleashes a flaming attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16216', '1062731777', 'Active', ' Emits an area of effect sacred magic attack.', '3184', 'Solar Flare: Active: Unleashes a sacred attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16217', '1062797313', 'Active', ' Emits elemental damage over an area.', '3186', 'Aura Flare: Active: Unleashes an elemental attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16218', '1062862849', 'Active', ' Emits an area of effect dark attack.', '3185', 'Shadow Flare: Active: Unleashes a dark attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16219', '1062928385', 'Active', ' Emits an area of effect water attack.', '3181', 'Hydro Blast: Active: Unleashes a powerful liquidy attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16220', '1062993921', 'Active', ' Emits an area of effect wind attack.', '3182', 'Hurricane: Active: Unleashes a powerful gusting attack against nearby enemies. Power 55.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16221', '1063059457', 'Active', ' Temporarily puts your target to sleep. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3191', 'Sleep: Skills Used: Instantly puts a target into sleep. Additional chance to be put into sleep greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16222', '1063124993', 'Active', ' Temporarily decreases your target\'s speed.', '3187', 'Slow: Active: Temporarily decreases a target\'s speed.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16223', '1063190529', 'Active', ' Temporarily stuns your target.', '3189', 'Stun: Active: Temporarily throws the target into a state of shock.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16224', '1063256065', 'Active', ' Inflicts an earth attack.', '3169', 'Stone: Active: Attacks the target with a stone boulder. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16225', '1063321601', 'Active', ' Inflicts a fire attack.', '3165', 'Prominence: Active: Detonates a fireball by compressing the air around the caster. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16226', '1063387137', 'Active', ' Inflicts a sacred magic attack.', '3170', 'Solar Flare: Active: Unleashes a sacred attack. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16227', '1063452673', 'Active', ' Inflicts elemental damage.', '3172', 'Aura Flare: Active: Unleashes an elemental attack. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16228', '1063518209', 'Active', ' Inflicts a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16229', '1063583745', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16230', '1063649281', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16231', '1063714817', 'Active', ' Cancels your enemy\'s target.', '3152', 'Trick: Active: Cancels the target\'s status.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16232', '1063780353', 'Active', ' Temporarily petrifies your target.', '3193', 'Medusa: Active: Temporarily throws the target into a petrified state.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16233', '1063845889', 'Active', ' Launches a dark attack.', '3171', 'Shadow Flare: Active: Unleashes a dark attack. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16234', '1063911425', 'Active', ' Has a chance to open doors and treasure chests. Requires Keys of a Thief.', '3155', 'Unlock: Active: Opens level 3 doors with 100% probability and chests below level 72 with 90% probability. Requires 17 Keys of a Thief.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16235', '1063976961', 'Active', ' Absorbs HP from your target.', '3153', 'Vampiric Touch: Active: Absorbs HP. Power 88.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16236', '1064042497', 'Active', ' Inflicts a water attack.', '3167', 'Hydro Blast: Active: Unleashes a spray of highly pressurized water. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16237', '1064108033', 'Active', ' Inflicts a wind attack.', '3168', 'Hurricane: Active: Creates a whirlwind of destruction. Power 110.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16238', '1064173569', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a physical attack.', '3084', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. during an ordinary physical attack. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16239', '1064239105', 'Chance', ' Momentarily causes your target to bleed during a physical attack.', '3092', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a general physical attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16240', '1064304641', 'Chance', ' Momentarily frightens away your target during a physical attack.', '3090', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a general physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16241', '1064370177', 'Chance', ' Momentarily holds your target during a physical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3086', 'Hold: Chance: Momentarily throws the target into a state of hold during an ordinary physical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16242', '1064435713', 'Chance', ' Momentarily poisons your target during a physical attack.', '3091', 'Poison: Chance: Momentarily throws the target into a poisoned state during a general physical attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16243', '1064501249', 'Chance', ' Momentarily petrifies your target during a physical attack.', '3089', 'Medusa: Chance: Momentarily throws the target into a petrified state during a general physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16244', '1064566785', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a critical attack.', '3112', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during a critical attack. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16245', '1064632321', 'Chance', ' Momentarily causes your target to bleed during a critical attack.', '3120', 'Bleed: Chance: Momentarily throws the target into a bleeding state during a critical attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16246', '1064697857', 'Chance', ' Momentarily frightens your target during a critical attack.', '3118', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16247', '1064763393', 'Chance', ' Momentarily holds your target during a critical attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3114', 'Hold: Chance: Momentarily throws the target into a state of hold during a critical attack. The target cannot be affected by any additional hold attacks while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16248', '1064828929', 'Chance', ' Momentarily poisons your target during a critical attack.', '3119', 'Poison: Chance: Momentarily throws the target into a poisoned state during a critical attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16249', '1064894465', 'Chance', ' Momentarily petrifies your target during a critical attack.', '3117', 'Medusa: Chance: Momentarily throws the target into a petrified state during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16250', '1064960001', 'Chance', ' Has a chance to decrease the Atk. Spd. of a target that damages you..', '3227', 'Winter: Chance: Momentarily decreases a target\'s Atk. Spd. when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16251', '1065025537', 'Chance', ' Has a chance to increase your Evasion when you take damage.', '3221', 'Agility: Chance: Temporarily increases Evasion when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16252', '1065091073', 'Chance', ' Has a chance to cause bleeding on a target that damages you.', '3235', 'Bleed: Chance: Momentarily causes the target to bleed when you are under attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16253', '1065156609', 'Chance', ' Has a chance to regenerate CP when you take damage.', '3213', 'Ritual: Chance: Restores CP when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16254', '1065222145', 'Chance', ' Has a chance to increase critical attack rate when you take damage.', '3223', 'Focus: Chance: Temporarily increases the critical attack rate when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16255', '1065287681', 'Chance', ' Has a chance to decrease the will to attack of a target that damages you.', '3225', 'Charm: Chance: Decreases the enemy\'s urge to attack when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16256', '1065353217', 'Chance', ' Has a chance to increase your Accuracy when you take damage.', '3222', 'Guidance: Chance: Temporarily increases Accuracy when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16257', '1065418753', 'Chance', ' Has a chance to hold a target that damages you. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3229', 'Hold: Active: Momentarily holds the target when you are under attack. Additional chance to be put into hold greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16258', '1065484289', 'Chance', ' Has a chance to increase the effectiveness of HP recovery magic when you take damage.', '3209', 'Prayer: Chance: Increases the effect of HP recovery magic by using attack rate for a certain amount of time.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16259', '1065549825', 'Chance', ' Has a chance to regenerate HP when you take damage.', '3207', 'Heal: Chance: Restores your HP by using attack rate.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16260', '1065615361', 'Chance', ' Has a chance to increase your M. Atk. when you take damage.', '3216', 'Empower: Chance: Temporarily increases PVP M. Atk. when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16261', '1065680897', 'Chance', ' Has a chance to increase critical attack rate of magic attacks when you take damage.', '3224', 'Wild Magic: Chance: Temporarilty increases the critical attack rate of magic attacks when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16262', '1065746433', 'Chance', ' Has a chance to increase your maximum CP when you take damage.', '3214', 'Cheer: Chance: Increases Max. CP when under attack for a certain amount of time.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16263', '1065811969', 'Chance', ' Has a chance to increase your maximum HP when you take damage.', '3208', 'Blessed Body: Chance: Increases Max. HP by using attack rate for a certain amount of time.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16264', '1065877505', 'Chance', ' Has a chance to increase your maximum MP when you take damage.', '3211', 'Blessed Soul: Chance: Increases maximum MP when under attack for a certain amount of time.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16265', '1065943041', 'Chance', ' Has a chance to increase your M. Def. when you take damage.', '3219', 'Magic Barrier: Chance: Temporarily increases M. Def. when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16266', '1066008577', 'Chance', ' Has a chance to increase your P. Atk. when you take damage.', '3215', 'Might: Chance: Temporarily increases P. Atk. when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16267', '1066074113', 'Chance', ' Has a chance to increase your P. Def. when you take damage.', '3218', 'Shield: Chance: Temporarily increases P. Def. when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16268', '1066139649', 'Chance', ' Has a chance to poison a target that damages you.', '3234', 'Poison: Chance: Momentarily poisons the target when you are under attack. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16269', '1066205185', 'Chance', ' Has a chance to decrease the PVP power of a target that damages you in PvP.', '3220', 'Duel Weakness: Chance: Temporarily decreases the opponent\'s PVP P. Atk. when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16270', '1066270721', 'Chance', ' Has a chance to increase your PVP power when you take damage in PvP.', '3217', 'Duel Might: Chance: Temporarily increases PVP P. Atk. when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16271', '1066336257', 'Chance', ' Has a chance to sleep a target that damages you. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3230', 'Sleep: Active: Momentarily causes the target to sleep when you are under attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16272', '1066401793', 'Chance', ' Has a chance to decrease the speed of a target that damages you in PvP.', '3226', 'Slow: Chance: Momentarily decreases a target\'s Speed when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16273', '1066467329', 'Chance', ' Momentarily decreases your target\'s Atk. Spd. during a magic attack.', '3097', 'Winter: Chance: Momentarily decreases the target\'s Atk. Spd. during magic use. Effect 3.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16274', '1066532865', 'Chance', ' Momentarily causes your target to bleed during a magic attack.', '3105', 'Bleed: Chance: Momentarily throws the target into a bleeding state during magic use. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16275', '1066598401', 'Chance', ' Momentarily frightens away your target during a magic attack.', '3103', 'Fear: Chance: Momentarily throws the target into a state of fear and causes him to flee during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16276', '1066663937', 'Chance', ' Momentarily holds your target during a magic attack. Your target cannot be affected by any additional hold attacks while the effect lasts.', '3099', 'Hold: Chance: Momentarily throws the target into a state of hold during magic use. The target cannot be affected by any additional hold attacks while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16277', '1066729473', 'Chance', ' Momentarily poisons your target during a magic attack.', '3104', 'Poison: Chance: Momentarily throws the target into a poisoned state during magic use. Effect 8.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16278', '1066795009', 'Chance', ' Momentarily petrifies your target during a magic attack.', '3102', 'Medusa: Chance: Momentarily throws the target into a petrified state during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16279', '1066860545', 'Passive', ' Increases the power of HP recovery magic.', '3246', 'Heal Empower: Passive: Increases the power of HP recovery magic when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16280', '1066926081', 'Passive', ' Increases the effectiveness of HP recovery magic.', '3238', 'Prayer: Passive: Increases the effect of HP recovery magic when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16281', '1066991617', 'Passive', ' Increases your M. Atk.', '3241', 'Empower: Passive: Increases M. Atk. when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16282', '1067057153', 'Passive', ' Increases your M. Def.', '3245', 'Magic Barrier: Passive: Increases M. Def. when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16283', '1067122689', 'Passive', ' Increases your P. Atk.', '3240', 'Might: Passive: Increases P. Atk. when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16284', '1067188225', 'Passive', ' Increases your P. Def.', '3244', 'Shield: Passive: Increases P. Def. when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16285', '1067253761', 'Passive', ' Increases your P. Atk. in PvP.', '3243', 'Duel Might: Passive: Increases PVP P. Atk. when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16286', '1067319297', 'Passive', ' Increases your weight limit by 2.', '3251', 'Weight Limit: Passive: Increases the weapon weight limit by 2 times when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16287', '1067384833', 'Active', ' Temporarily decreases all skill re-use times.', '3202', 'Refresh: Active: Temporarily decreases the re-use times for all skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16288', '1067450369', 'Active', ' Temporarily decreases MP consumption rates for all skills.', '3164', 'Clarity: Active: Temporarily decreases the MP consumption rates for all skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16289', '1067515905', 'Active', ' Temporarily increases your critical attack rate.', '3141', 'Focus: Active: Increases the chance of a critical attack temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16290', '1067581441', 'Active', ' Temporarily reflects some of the damage you receive back to the enemy. Excludes damage from skills or ranged attacks.', '3204', 'Reflect Damage: Active: Allows you to reflect some of the damage you incurred back to the enemy for a certain amount of time. Excludes damage from skill or remote attacks.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16291', '1067646977', 'Active', ' Temporarily blocks all of your target\'s physical/magic skills.', '3198', 'Doom: Active: Temporarily blocks all of the target\'s physical/magic skills.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16292', '1067712513', 'Active', ' Teleports you to the nearest village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3147', 'Recall: Active: Teleports the caster to a village. Cannot be used in special areas, such as the GM Consultation Room.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16293', '1067778049', 'Active', ' Renders you momentarily invincible.', '3158', 'Lesser Celestial Shield: Active: Bestows temporary invincibility.', '1');
INSERT INTO `zeus_augment_data` VALUES ('16294', '1067843585', 'Active', ' Temporarily increases your critical attack rate for magic attacks.', '3142', 'Wild Magic: Active: Increases the critical attack rate of magic attacks temporarily.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16295', '1067909121', 'Active', ' Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '3205', 'Party Recall: Active: Teleports party members to a village. Cannot be used in a specially designated place such as the GM Consultation Service.', '2');
INSERT INTO `zeus_augment_data` VALUES ('16296', '1067974657', 'Active', ' Temporarily blocks your target\'s magic skills.', '3197', 'Silence: Active: Temporarily blocks the target\'s magic skills.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16297', '1068040193', 'Active', ' Temporarily decreases the skill re-use time.', '3199', 'Skill Refresh: Active: Temporarily decreases the re-use time for physical skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16298', '1068105729', 'Active', ' Temporarily decreases the skill MP consumption rate.', '3161', 'Skill Clarity: Active: Temporarily decreases the MP consumption rate for physical skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16299', '1068171265', 'Active', ' Temporarily decreases the re-use time for singing and dancing skills.', '3201', 'Music Refresh: Active: Temporarily decreases the re-use time for song/dance skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16300', '1068236801', 'Active', ' Temporarily decreases the MP consumption rate for singing and dancing skills.', '3163', 'Music Clarity: Active: Temporarily decreases the MP consumption rate for song/dance skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16301', '1068302337', 'Active', ' Temporarily decreases the magic re-use time.', '3200', 'Spell Refresh: Active: Temporarily decreases the re-use time for magic skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16302', '1068367873', 'Active', ' Temporarily decreases the magic MP consumption rate.', '3162', 'Spell Clarity: Active: Temporarily decreases the MP consumption rate for magical skills.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16303', '1068433409', 'Active', ' Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3159', 'Stealth: Active: Temporarily blocks a monster\'s pre-emptive attack. Fighting ability significantly decreases while in effect.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16304', '1068498945', 'Active', ' Temporarily increases the amount of HP absorbed from damage done to your target. Excludes damage by skill or ranged attacks.', '3148', 'Vampiric Rage: Active: Increases the ability to restore some HP from the damage inflicted on an enemy temporarily. Excludes damage by skill or long-range attacks.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16305', '1068564481', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a physical attack.', '3094', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a general physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16306', '1068630017', 'Chance', ' Momentarily burns up your target\'s MP during a physical attack.', '3082', 'Mana Burn: Chance: Burns up a target\'s MP during an ordinary physical attack. Power 88.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16307', '1068695553', 'Chance', ' Momentarily paralyzes your target during a physical attack.', '3088', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during an ordinary physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16308', '1068761089', 'Chance', ' Momentarily blocks your target\'s magic skill during a physical attack.', '3093', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a general physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16309', '1068826625', 'Chance', ' Momentarily puts your target to sleep during a physical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3087', 'Sleep: Active: Momentarily throws the target into a state of sleep during a general physical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16310', '1068892161', 'Chance', ' Momentarily stuns your target during a physical attack.', '3085', 'Stun: Chance: Momentarily throws the target into a state of shock during an ordinary physical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16311', '1068957697', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a critical attack.', '3122', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16312', '1069023233', 'Chance', ' Momentarily burns up your target\'s MP during a critical attack.', '3110', 'Mana Burn: Chance: Burns up a target\'s MP during a critical attack. Power 88.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16313', '1069088769', 'Chance', ' Momentarily paralyzes your target during a critical attack.', '3116', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16314', '1069154305', 'Chance', ' Momentarily blocks your target\'s magic skill during a critical attack.', '3121', 'Silence: Chance: Momentarily blocks the target\'s magic skill during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16315', '1069219841', 'Chance', ' Momentarily puts your target to sleep during a critical attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3115', 'Sleep: Active: Momentarily throws the target into a state of sleep during a critical attack. Additional chance to be put into sleep greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16316', '1069285377', 'Chance', ' Momentarily stuns your target during a critical attack.', '3113', 'Stun: Chance: Momentarily throws the target into a state of shock during a critical attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16317', '1069350913', 'Chance', ' Has a chance to block the use of all physical and magical skills by a target that damages you.', '3237', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16318', '1069416449', 'Chance', ' Has a chance to frighten away a target that damages you.', '3233', 'Fear: Chance: Momentarily instills a feeling of fear on the target that causes it to flee when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16319', '1069481985', 'Chance', ' Has a chance to increase the effectiveness of MP recovery magic when you take damage.', '3212', 'Mana Gain: Chance: Increases the recharge recovery rate of MP when under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16320', '1069547521', 'Chance', ' Has a chance to regenerate MP when you take damage.', '3210', 'Recharge: Chance: Restores your MP by using attack rate.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16321', '1069613057', 'Chance', ' Has a chance to paralyze a target that damages you.', '3231', 'Paralyze: Chance: Momentarily paralyzes the target when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16322', '1069678593', 'Chance', ' Has a chance to block the use of all magic skills by a target that damages you.', '3236', 'Silence: Chance: Momentarily blocks the target\'s magic skills when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16323', '1069744129', 'Chance', ' Has a chance to stun a target that damages you.', '3228', 'Stun: Chance: Momentarily stuns the target when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16324', '1069809665', 'Chance', ' Has a chance to petrify a target that damages you.', '3232', 'Medusa: Chance: Momentarily petrifies the target when you are under attack.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16325', '1069875201', 'Chance', ' Momentarily blocks all of your target\'s physical and magic skills during a magic attack.', '3107', 'Doom: Chance: Momentarily blocks all of the target\'s physical and magic skills during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16326', '1069940737', 'Chance', ' Momentarily burns up your target\'s MP during a magic attack.', '3095', 'Mana Burn: Chance: Burns up a target\'s MP during magic use. Power 88.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16327', '1070006273', 'Chance', ' Momentarily paralyzes your target during a magic attack.', '3101', 'Paralyze: Chance: Momentarily throws the target into a state of paralysis during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16328', '1070071809', 'Chance', ' Momentarily blocks your target\'s magic skill during a magic attack.', '3106', 'Silence: Chance: Momentarily blocks the target\'s magic skill during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16329', '1070137345', 'Chance', ' Momentarily puts your target to sleep during a magic attack. Your target cannot be affected by any additional sleep attacks while the effect lasts.', '3100', 'Sleep: Active: Momentarily throws the target into a state of sleep during magic use. Additional chance to be put into sleep greatly decreases while the effect lasts.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16330', '1070202881', 'Chance', ' Momentarily stuns your target during a magic attack.', '3098', 'Stun: Chance: Momentarily throws the target into a state of shock during magic use.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16331', '1070268417', 'Passive', ' Decreases the MP consumption rate for all skills.', '3258', 'Clarity: Passive: Decreases the MP consumption rate for all skills when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16332', '1070333953', 'Passive', ' Increases your Evasion.', '3247', 'Agility: Passive: Increases evasion when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16333', '1070399489', 'Passive', ' Increases your critical attack rate.', '3249', 'Focus: Passive: Increases critical attack rate when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16334', '1070465025', 'Passive', ' Increases the ability to reflect some of the damage you incurred back to the enemy. Excludes damage by skill or ranged attacks.', '3259', 'Reflect Damage: Passive: Increases the ability to reflect some of the damage you incur back to the enemy when equipped. Excludes damage by skill or long-range attacks.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16335', '1070530561', 'Passive', ' Increases your Accuracy.', '3248', 'Guidance: Passive: Increases accuracy when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16336', '1070596097', 'Passive', ' Increases the critical attack rate of magic attacks.', '3250', 'Wild Magic: Passive: Increases the critical attack rate of magic attacks when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16337', '1070661633', 'Passive', ' Increases your MP recharge recovery rate.', '3239', 'Mana Gain: Passive: Increases the recharge recovery rate of MP when equipped.', '10');
INSERT INTO `zeus_augment_data` VALUES ('16338', '1070727169', 'Passive', ' Decreases the skill MP consumption rate.', '3255', 'Skill Clarity: Passive: Decreases the MP consumption rate for physical skills when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16339', '1070792705', 'Passive', ' Decreases the song/dance skill MP consumption rate.', '3257', 'Music Clarity: Passive: Decreases the MP consumption rate for song/dance skills when equipped.', '3');
INSERT INTO `zeus_augment_data` VALUES ('16340', '1070858241', 'Passive', ' Decreases the magic MP consumption rate.', '3256', 'Spell Clarity: Passive: Decreases the MP consumption rate for magic skills when equipped.', '3');

-- ----------------------------
-- Table structure for `zeus_buffer_buff_list`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buffer_buff_list`;
CREATE TABLE `zeus_buffer_buff_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `buffClass` int(2) DEFAULT NULL,
  `buffType` varchar(10) DEFAULT NULL,
  `buffOrder` int(2) NOT NULL DEFAULT '100',
  `buffId` int(5) DEFAULT '0',
  `buffLevel` int(5) DEFAULT '0',
  `buffDesc` text NOT NULL,
  `forClass` tinyint(1) DEFAULT '0',
  `canUse` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buffer_buff_list
-- ----------------------------
INSERT INTO `zeus_buffer_buff_list` VALUES ('1', '1', 'buff', '26', '4', '1', 'Increases Speed by 40 for 15 seconds.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('2', '5', 'cubic', '1', '10', '8', 'Summons a storm cubic that inflicts damage on the enemy with magic. Consumes 4 Spirit Ore when summoning.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('3', '5', 'cubic', '2', '22', '7', 'Summons a Vampiric Cubic. A Vampiric Cubic uses magic that absorbs the target\'s HP and with it regenerates its master\'s HP. Consumes 5 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('4', '5', 'cubic', '3', '33', '8', 'Summons Phantom Cubic. A Phantom Cubic uses magic that decreases the target\'s P. Atk., P. Def., and Atk. Spd. Requires 2 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('5', '5', 'cubic', '4', '67', '7', 'Summons a Life Cubic. The Life Cubic uses magic to regenerate HP to its owner and party members. Requires 5 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('6', '1', 'buff', '6', '77', '1', 'Increases P. Atk. by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('7', '1', 'buff', '9', '78', '1', 'Increases P. Atk. by 20% for 1 minute.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('8', '1', 'buff', '31', '82', '1', 'Increases P. Def. by 7% and decreases Evasion by 2 for 5 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('9', '1', 'buff', '30', '91', '1', 'Increases P. Def. by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('10', '2', 'resist', '2', '112', '1', 'Increases Resistance to bow attacks by 16% and Resistance to crossbow attacks by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('11', '1', 'buff', '40', '121', '1', 'Regenerates 10% of the user\'s original HP and increases the user\'s Max HP by 10% for 10 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('12', '9', 'others', '15', '123', '1', 'Increases the user\'s M. Def. by 15% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('13', '1', 'buff', '15', '131', '1', 'Decreases user\'s P. Def. by 10% and increases Accuracy by 6 for 5 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('14', '1', 'buff', '24', '230', '1', 'Increases Speed by 22 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('15', '7', 'song', '6', '264', '1', 'Increases P. Def. of all party members by 25% for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('16', '7', 'song', '8', '265', '1', 'Increases HP Regeneration of all party members by 20% for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('17', '7', 'song', '2', '266', '1', 'Increases Evasion of all party members by 3 for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('18', '7', 'song', '5', '267', '1', 'Increases M. Def. of all party members by 30% for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('19', '7', 'song', '4', '268', '1', 'Increases Speed of all party members by 20 for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('20', '7', 'song', '1', '269', '1', 'Increases Critical Rate of all party members by 100% for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('21', '7', 'song', '16', '270', '1', 'Increases all party members\'s Resistance to Dark by 20 for 2 minutes. Increases MP consumption when singing while song/dance effect lasts.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('22', '6', 'dance', '5', '271', '1', 'Increases P. Atk. of all party members by 12% for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('23', '6', 'dance', '7', '272', '1', 'Increases Accuracy of all party members by 4 for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('24', '6', 'dance', '2', '273', '1', 'Increases M. Atk. of all party members by 20% for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '1', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('25', '6', 'dance', '6', '274', '1', 'Increases Critical Damage of all party members by 35% for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('26', '6', 'dance', '4', '275', '1', 'Increases Atk. Spd. of all party members by 15% for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('27', '6', 'dance', '1', '276', '1', 'Decreases all party members\'s magic cancel damage by 40 and increases Casting Spd. by 30% for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '1', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('28', '6', 'dance', '11', '277', '1', 'Increases holy P. Atk. of all party members by 20 for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('29', '5', 'cubic', '5', '278', '6', 'Summons a Viper Cubic. The Viper Cubic uses magic that poisons a targeted enemy. Consumes 2 Spirit Ore when summoning.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('30', '2', 'resist', '12', '287', '1', 'Increases Resistance to Paralysis, Hold, Sleep, Stun and buff-canceling attacks by 40% for 1 minute.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('31', '9', 'others', '24', '297', '1', 'Increases dualsword weapon Atk. Spd. by 8% for 1 minute and ordinary/skill attack damage by 5% during PvP.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('32', '1', 'buff', '38', '303', '1', 'Increases the user\'s Max MP by 10% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('33', '7', 'song', '10', '304', '1', 'Increases Max HP of all party members by 30% for 2 minutes. Additionally increases MP consumption when singing while song/dance is in effect.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('34', '7', 'song', '3', '305', '1', 'For 2 minutes, gives a party member the ability to transfer 20% of received standard short-range damage back to the enemy. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('35', '7', 'song', '14', '306', '1', 'Increases party members\'s Resistance to Fire by 30 for 2 minutes. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('36', '6', 'dance', '12', '307', '1', 'Increases party members\'s Resistance to Water by 30 for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('37', '7', 'song', '15', '308', '1', 'Increases party members\'s Resistance to Wind by 30 for 2 minutes. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('38', '6', 'dance', '13', '309', '1', 'Increases party members\'s Resistance to Earth by 30 for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('39', '6', 'dance', '8', '310', '1', 'For 2 minutes, gives all party members the ability to recover as HP 8% of any standard melee damage inflicted on the enemy. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('40', '6', 'dance', '10', '311', '1', 'For 2 minutes, gives all party members the ability to decrease by 30 any environment-related damage received. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('41', '7', 'song', '11', '349', '1', 'For 2 minutes, decreases all party members\'s physical/magic skill MP consumption by 5% and re-use time by 20%. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('42', '7', 'song', '7', '363', '1', 'For 2 minutes, increases all party members\'s MP Recovery Bonus by 20%, and decreases magic skill use MP consumption by 10%. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('43', '7', 'song', '12', '364', '1', 'For 2 minutes, decreases all party members\'s MP consumption by 20% and reuse time by 10% for physical/sing/dance skill use. The MP consumption increases additionally when singing while sing/dance effect lasts.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('44', '6', 'dance', '3', '365', '1', 'For 2 minutes, increases all party members\'s magic damage by 100%. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('45', '6', 'dance', '16', '366', '1', 'For 2 minutes, decreases all party members\'s Speed by 50% and prevents them from being pre-emptively attacked by monsters. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('46', '8', 'special', '3', '395', '1', 'For 30 seconds, increases P. Def. by 5400, M. Def. by 4050, Resistance to buff-canceling attacks by 80, and Speed by 5. Consumes 40 Soul Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('47', '8', 'special', '14', '396', '1', 'For 2 minutes, increases the user\'s Accuracy by 8, P. Atk. by 500, M. Atk. by 500, Atk. Spd. by 100, Casting Spd. by 100, Speed by 20, Resistance to buff-canceling attacks by 80, and the effect of received HP recovery magic by 100%. Bestows complete Resistance to de-buff attacks. Decreases P. Def. by 25%, M. Def. by 25%, and Evasion by 8. Consumes 40 Soul Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('48', '1', 'buff', '43', '415', '1', 'Decreases physical skill MP consumption by 10% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('49', '1', 'buff', '44', '416', '1', 'Decreases physical skill re-use time by 10% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('50', '1', 'buff', '20', '439', '1', 'For 5 minutes, returns damage from close range physical attacks and skills back to the opponent at a fixed rate.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('51', '5', 'cubic', '6', '449', '4', 'Summons Attractive Cubic. Attractive Cubic can continuously use hate and hold magic on the enemy. Requires 8 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('52', '1', 'buff', '12', '482', '1', 'For 1 minute 30 seconds, increases the user\'s Critical Rate by 20% and Critical Damage by 25%. Decreases P. Def. by 5%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('53', '7', 'song', '17', '529', '1', 'Increases all party members\'s Resistance to Fire, Water, Wind and Earth attacks by 30 for 2 minutes. Additionally increases MP consumption when singing while song/dance is in effect.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('54', '6', 'dance', '14', '530', '1', 'Increases all party members\'s Resistance to Holy or Dark attacks by 30 for 2 minutes. Additionally increases MP consumption when dancing while song/dance is in effect. Requires a dualsword.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('55', '1', 'buff', '5', '761', '1', 'One sows a seed of wrath himself. There is a chance that the Seed of Wrath will grow when being attacked. P. Atk. increases with growth.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('56', '7', 'song', '13', '764', '1', 'For 2 minutes, increases a party member\'s Resistance to bows by 10 and Resistance to crossbows by 5. Increases the additional MP consumption when singing while sing/dance effect lasts.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('57', '6', 'dance', '9', '765', '1', 'For 15 seconds, increases a party member\'s Resistance to bows by 45 and Resistance to crossbows by 25. Decreases M. Atk. by 99%. Additionally increases MP consumption when singing while song/dance is in effect. Requires a dualsword.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('58', '5', 'cubic', '7', '779', '1', 'Summons Smart Cubic. Cancels all the bad abnormal conditions which were cast on the master, and uses useful skills additionally. Consumes 38 Spirit Ore when summoning.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('59', '9', 'others', '4', '825', '1', 'Sharpens a bladed weapon to increase P. Atk. by 5% and Critical Rate by 20% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('60', '9', 'others', '5', '826', '1', 'Adds a spike to a blunt weapon to increase P. Atk. by 5% and its weight for shock attacks by 8% for 20 minute(s).', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('61', '9', 'others', '6', '827', '1', 'Enhances the string of a bow or crossbow to increase P. Atk. by 5% and range by 100 for 20 minute(s).', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('62', '9', 'others', '27', '828', '1', 'Enhances the armor surface to increase P. Def. by 10% for 20 minute(s).', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('63', '9', 'others', '16', '829', '1', 'Tans armor to increase P. Def. by 5% and Evasion by 2 for 20 minutes. Works only on light armor users and can\'t be used on pets.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('64', '9', 'others', '17', '830', '1', 'Embroiders a robe to increase P. Def. by 5% and MP Recovery Bonus by 2 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('65', '9', 'others', '18', '834', '1', 'Through a blood contract, increases party members\'s maximum HP by 10% and HP Recovery Bonus by 10 for 2 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('66', '2', 'resist', '1', '913', '1', 'Increases Resistance to magic attacks by 100% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('67', '7', 'song', '9', '914', '1', 'Provides an 80% chance of removing a party member\'s de-buffs. And for the next 2 minutes, increases Resistance to de-buff attacks by 30% and increases the power of received HP recovery magic by 30%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('68', '6', 'dance', '15', '915', '1', 'Decreases a party member\'s P. Def., M. Def. and Evasion, and increases their P. Atk., M. Atk., Atk. Spd., Casting Spd. and Speed for 2 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('69', '1', 'buff', '23', '916', '1', 'For 8 seconds, transfers magical damage back to the enemy caster.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('70', '9', 'others', '26', '917', '1', 'For 30 seconds, you call upon your hidden reserves to increase your skills power and ordinary attack damage by 30% during PvP.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('71', '1', 'buff', '46', '982', '1', 'Increases party member\'s P. Atk. by 3% and Atk. Spd. by 3% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('72', '4', 'chant', '1', '1002', '3', 'Increases Casting Spd. of all party members by 15% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('73', '4', 'chant', '5', '1003', '3', 'Increases P. Atk. of nearby clan members by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('74', '4', 'chant', '2', '1004', '3', 'Increases Casting Spd. of nearby clan members by 15% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('75', '4', 'chant', '18', '1005', '3', 'Increases P. Def. of nearby clan members by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('76', '4', 'chant', '16', '1006', '3', 'Increases M. Def. of all party members by 15% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('77', '4', 'chant', '6', '1007', '3', 'Increases P. Atk. of all party members by 8% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('78', '4', 'chant', '17', '1008', '3', 'Increases M. Def. of nearby clan members by 15% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('79', '4', 'chant', '19', '1009', '3', 'Increases P. Def. of all party members by 8% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('80', '4', 'chant', '20', '1010', '3', 'Increases P. Def. by 8% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('81', '2', 'resist', '3', '1032', '3', 'Increases Resistance to Bleed attacks by 30% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('82', '2', 'resist', '11', '1033', '3', 'Increases Resistance to Poison by 30% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('83', '1', 'buff', '53', '1035', '4', 'Increases Resistance to Hold/Sleep/Mental attaks by 20 for 20 minutes.', '1', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('84', '1', 'buff', '28', '1036', '2', 'Increases M. Def. by 23% for 20 minutes.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('85', '1', 'buff', '32', '1040', '3', 'Increases P. Def. by 8% for 20 minutes.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('86', '1', 'buff', '55', '1043', '1', 'Increases a party member\'s holy P. Atk. by 20 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('87', '1', 'buff', '35', '1044', '3', 'Increases HP Regeneration by 10% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('88', '1', 'buff', '41', '1045', '6', 'Increases Max HP by 10% for 20 minutes.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('89', '1', 'buff', '33', '1047', '4', 'Increases the user\'s MP Recovery Bonus by 1.72 for 20 minutes. Consumes 7 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('90', '1', 'buff', '39', '1048', '6', 'Increases Max MP by 10% for 20 minutes.', '1', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('91', '1', 'buff', '2', '1059', '3', 'Increases M. Atk. by 55% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('92', '1', 'buff', '45', '1062', '2', 'Decreases a party member\'s P. Def. by 5%, M. Def. by 10% and Evasion by 2, and increases P. Atk. by 5%, M. Atk. by 10%, Atk. Spd. by 5%, Casting Spd. by 5% and Speed by 5 for 20 minutes.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('93', '1', 'buff', '8', '1068', '3', 'Increases P. Atk. by 8% for 20 minutes.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('94', '1', 'buff', '17', '1073', '1', 'Increases lung capacity by 400% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('95', '1', 'buff', '11', '1077', '3', 'Increases Critical Rate by 20% for 20 minutes.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('96', '1', 'buff', '47', '1078', '6', 'Decreases magic cancel damage by 18 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('97', '1', 'buff', '1', '1085', '3', 'Increases Casting Spd. by 15% for 20 minutes.', '1', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('98', '1', 'buff', '4', '1086', '2', 'Increases Atk. Spd. by 15% for 20 minutes.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('99', '1', 'buff', '16', '1087', '3', 'Increases Evasion by 2 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('100', '2', 'resist', '4', '1182', '3', 'Increases Resistance to Water by 10 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('101', '2', 'resist', '5', '1189', '3', 'Increases Resistance to Wind by 10 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('102', '2', 'resist', '6', '1191', '3', 'Increases Resistance to Fire by 10 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('103', '1', 'buff', '25', '1204', '2', 'Increases Speed by 20 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('104', '1', 'buff', '21', '1232', '3', 'For 20 minutes, transfers 10% of the target\'s received standard short-range damage back to the enemy.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('105', '1', 'buff', '22', '1238', '3', 'For 20 minutes, transfers 10% of received standard short-range damage back to the enemy.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('106', '1', 'buff', '14', '1240', '3', 'Increases Accuracy by 2 for 20 minutes.', '4', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('107', '1', 'buff', '13', '1242', '3', 'Increases Critical Damage by 25% for 20 minutes.', '2', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('108', '1', 'buff', '18', '1243', '6', 'Increases Shield Defense by 5% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('109', '4', 'chant', '9', '1249', '3', 'Increases Accuracy of nearby clan members by 2 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('110', '4', 'chant', '13', '1250', '3', 'Increases Shield Defense of nearby clan members by 30% for 20 minutes. Effect 1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('111', '4', 'chant', '3', '1251', '2', 'Increases Atk. Spd. of all party members by 15% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('112', '4', 'chant', '11', '1252', '3', 'Increases Evasion of all party members by 2 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('113', '4', 'chant', '8', '1253', '3', 'Increases Critical Damage of all party members by 25% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('114', '1', 'buff', '48', '1257', '3', 'Increases the weight penalty interval by 3000 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('115', '2', 'resist', '10', '1259', '4', 'Increases Resistance to Stun attacks by 15% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('116', '4', 'chant', '12', '1260', '3', 'Increases Evasion of nearby clan members by 2 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('117', '4', 'chant', '30', '1261', '2', 'Decreases nearby clan members\'s P. Def. by 5%, M. Def. by 10%, and Evasion by 2, and increases P. Atk. by 5%, M. Atk. by 10%, Atk. Spd. by 5%, Casting Spd. by 5%, and Speed by 5 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('118', '1', 'buff', '37', '1268', '4', 'For 20 minutes, 6% of the standard melee damage inflicted on the enemy is recovered as HP.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('119', '5', 'cubic', '8', '1279', '9', 'Summons Binding Cubic. Binding Cubic uses magic that paralyzes a targeted enemy. Requires 4 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('120', '5', 'cubic', '9', '1280', '9', 'Summons Aqua Cubic. Aqua Cubic uses attack magic that inflicts continuous damage to the enemy. Requires 2 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('121', '5', 'cubic', '10', '1281', '9', 'Summons Spark Cubic. Spark Cubic uses magic that stuns a targeted enemy. Requires 4 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('122', '4', 'chant', '15', '1282', '2', 'Increases Speed of nearby clan members by 20 for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('123', '4', 'chant', '14', '1284', '3', 'For 20 minutes, gives a party member the ability to transfer 10% of received standard short-range damage back to the enemy.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('124', '9', 'others', '7', '1285', '1', 'Uses Fire energy to increase user\'s Fire attack level by 20.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('125', '9', 'others', '8', '1286', '1', 'Uses Water energy to increase user\'s Water attack level by 20.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('126', '9', 'others', '9', '1287', '1', 'Uses Wind energy to increase user\'s Wind attack level by 20.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('127', '1', 'buff', '3', '1303', '2', 'For 20 minutes, increases by 1 the damage rate of magic.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('128', '1', 'buff', '19', '1304', '3', 'Increases Shield Defense by 30% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('129', '1', 'buff', '36', '1307', '3', 'Increases the power of HP recovery magic received by all party members by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('130', '4', 'chant', '7', '1308', '3', 'Increases Critical Rate of all party members by 20% for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('131', '4', 'chant', '10', '1309', '3', 'Increases Accuracy of all party members by 2 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('132', '4', 'chant', '22', '1310', '4', 'For 20 minutes, gives all party members the ability to recover as HP 6% of any standard melee damage inflicted on the enemy.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('133', '9', 'others', '19', '1311', '6', 'Restores HP of all party members by 10% and increases Max HP by 10% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('134', '8', 'special', '15', '1323', '1', 'Maintains target\'s buff/de-buff condition even following death and resurrection. The Blessing of Noblesse and the Amulet of Luck disappear, however. Consumes 5 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('135', '1', 'buff', '50', '1352', '1', 'For 20 minutes, increases Resistance to Fire, Water, Wind and Earth attacks.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('136', '1', 'buff', '49', '1353', '1', 'For 20 minutes, increases Resistance to Dark by 30 and Resistance to Holy by 20.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('137', '1', 'buff', '54', '1354', '1', 'For 20 minutes, increases Resistance to buff-canceling attacks by 30 and Resistance to de-buff attacks by 20.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('138', '3', 'prophecy', '2', '1355', '1', 'For 5 minutes, a powerful spirit acts to increase the damage caused by the targeted party member\'s magic damage by 2, MP Recovery Bonus by 20%, P. Atk. by 10%, P. Def. by 20%, Atk. Spd. by 20%, M. Atk. by 20%, M. Def. by 20%, Casting Spd. by 20%, and Resistance to de-buffs by 10%. Decreases Speed by 20% and MP consumption for skill use by 5%. Consumes 10 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('139', '3', 'prophecy', '1', '1356', '1', 'For 5 minutes, a powerful spirit acts to increase a party member\'s Max MP by 20%, HP Recovery Bonus by 20%, magic damage by 2, Critical Damages by 20%, P. Atk. by 10%, P. Def. by 20%, Atk. Spd. by 20%, M. Atk. by 20%, M. Def. by 20%, Casting Spd. by 20%, and Resistance to de-buffs by 10%. Decreases Speed by 20%. Consumes 10 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('140', '3', 'prophecy', '3', '1357', '1', 'For 5 minutes, a powerful spirit acts to increase a party member\'s Max HP by 20%, Critical Rate by 20%, magic damage by 20%, P. Atk. by 10%, P. Def. by 20%, Atk. Spd. by 20%, M. Atk. by 20%, M. Def. by 20%, Casting Spd. by 20%, and Resistance to de-buff by 10%. Decreases Speed by 20%. Bestows the ability to recover as HP 5% of the standard melee damage inflicted on the enemy. Consumes 10 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('141', '4', 'chant', '25', '1362', '1', 'For 20 minutes, increases all party members\'s Resistance to buff-canceling attacks by 30 and Resistance to de-buff attacks by 20.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('142', '3', 'prophecy', '5', '1363', '1', 'Recovers all party members\'s HP by 20%, and for 5 minutes, receives help from a great spirit to increase Max HP by 20%, magic damage Critical Damage by 2, Critical Damage by 20%, P. Atk. by 10%, P. Def. by 20%, Atk. Spd. by 20%, M. Atk. by 20%, M. Def. by 20%, Casting Spd. by 20%, Resistance to de-buffs by 10%, and Accuracy by 4. Decreases Speed by 20%. Consumes 40 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('143', '4', 'chant', '27', '1364', '1', 'For 20 minutes, increases nearby clan members\'s Accuracy by 4 and decreases the rate of being hit by a critical attack by 30%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('144', '4', 'chant', '29', '1365', '1', 'For 20 minutes, increases nearby clan members\'s M. Atk. by 75% and M. Def. by 30%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('145', '1', 'buff', '51', '1388', '3', 'Increases P. Atk. by 4% for 20 minutes. Consumes 1 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('146', '1', 'buff', '52', '1389', '3', 'Increases P. Def. by 5% for 20 minutes. Consumes 1 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('147', '4', 'chant', '4', '1390', '3', 'Increases P. Atk. of all party members by 4% for 20 minutes. Consumes 4 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('148', '4', 'chant', '21', '1391', '3', 'Increases P. Def. of all party members by 5% for 20 minutes. Consumes 4 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('149', '2', 'resist', '7', '1392', '3', 'Increases Resistance to Holy by 15 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('150', '2', 'resist', '8', '1393', '3', 'Increases Resistance to Dark by 15 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('151', '1', 'buff', '42', '1397', '3', 'For 20 minutes, decreases physical skill MP consumption by 10%, magic skill MP consumption by 4%, and song/dance skill MP consumption by 10%. Consumes 1 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('152', '2', 'resist', '14', '1411', '1', 'Makes one invincible to various debuffs for a certain period of time. Usable only on party members.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('153', '4', 'chant', '28', '1413', '1', 'For 5 minutes, a powerful spirit acts to increase Max MP of all party members by 15%, MP Recovery Bonus by 1.5 when equipped with light or heavy armor, MP Recovery Bonus by 4 when equipped with a robe, M. Def. by 30%, M. Atk. by 30%, Casting Spd. by 20%, Resistance to Fire, Water, Wind and Earth damage by 10, Resistance to de-buff attacks by 25, and Resistance to buff-canceling attacks by 40. Consumes 40 Spirit Ore.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('154', '4', 'chant', '31', '1414', '1', 'For 5 minutes, a powerful spirit acts to increase nearby clan members\'s Max CP by 20%, CP recovery bonus by 20%, Max MP by 20%, Critical Rate by 20%, the power of Prominent Damage through magic damage by 20%, P. Atk. by 10%, P. Def. by 20%, Atk. Spd. by 20%, M. Atk. by 20%, M. Def. by 20%, Casting Spd. by 20%, and Resistance to de-buff by 10%. Decreases Speed by 20%. Consumes 40 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('155', '4', 'chant', '26', '1415', '1', 'For 5 minutes, increases nearby clan members\'s Resistance to buff-canceling attacks by 30% and Resistance to de-buff attacks by 20%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('156', '4', 'chant', '23', '1416', '1', 'Regenerates nearby clan members\'s CP by 800 and increases Max CP by 800 for 5 minutes. Consumes 20 Spirit Ore.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('157', '1', 'buff', '7', '1432', '1', 'Increases the user\'s P. Atk. by 8% for 20 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('158', '2', 'resist', '9', '1442', '1', 'Increases the target\'s Resistance to Dark by 15 for 20 minutes.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('159', '9', 'others', '14', '1443', '1', 'Increases the target\'s Dark attack level by 20 for 20 minutes. Can be used on party members.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('160', '9', 'others', '2', '1444', '1', 'When using a Kamael-exclusive weapon, P. Atk., M. Atk. and lethal attack rate are increased by 10% for 3 minutes. May be applied to Kamael party members.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('161', '9', 'others', '1', '1457', '1', 'For 20 minutes, increases the user\'s magic MP consumption by 35% and M. Atk. by 25%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('162', '1', 'buff', '34', '1460', '1', 'For 20 minutes, increases the recharge power received by the target by 85.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('163', '4', 'chant', '24', '1461', '1', 'For 20 minutes, decreases Critical Damage received by a party member by 30%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('164', '9', 'others', '10', '1463', '1', 'Adds Fire damage to a P. Atk.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('165', '9', 'others', '11', '1464', '1', 'Adds Water damage to a P. Atk.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('166', '9', 'others', '12', '1465', '1', 'Adds Wind damage to a P. Atk.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('167', '9', 'others', '13', '1466', '1', 'Bestows the Earth elemental to a P. Atk.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('168', '1', 'buff', '27', '1470', '1', 'Increases M. Def. of all party members by 3000 for 30 seconds.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('169', '8', 'special', '2', '1476', '1', 'For 15 seconds, awakens a party member\'s destructive instincts and increases P. Atk., Critical Rate and Critical Damage by 30%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('170', '1', 'buff', '29', '1478', '1', 'Stirs up the defense instinct to increase P. Def. by 1200 and M. Def. by 900 for 15 seconds.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('171', '9', 'others', '20', '1492', '1', 'Engulfs the user with a protective coat of fire. For 20 minutes, increases Resistance to fire attacks by 60 and causes burn damage on the attacking enemy.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('172', '9', 'others', '21', '1493', '1', 'Engulfs the user with a protective glacier barrier. For 20 minutes, increases Resistance to water attacks by 60 and slows down the attacking enemy.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('173', '9', 'others', '22', '1494', '1', 'Engulfs the body with a storm barrier. For 20 minutes, increases Resistance to wind attacks by 60 and slows down the attacking enemy\'s Atk. Spd.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('174', '0', 'improved', '1', '1499', '1', 'Combines P. Atk. increase and P. Def. increase to have more advanced combat power increase effect. For 40 minutes, increases P. Atk. by 15% and P. Def. by 15%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('175', '0', 'improved', '13', '1500', '1', 'Increases both M. Atk. and M. Def. to have more advanced magic ability increase effect. For 40 minutes, increases M. Atk. by 75% and M. Def. by 30%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('176', '0', 'improved', '9', '1501', '1', 'Combines maximum HP increase and Max MP increase to have more advanced mental and physical power. For 40 minutes, increases Max HP by 35% and Max MP by 35%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('177', '0', 'improved', '4', '1502', '1', 'Combines Critical Rate increase and Critical Damage increase to have more advanced critical increase effect. For 40 minutes, increases Critical Rate by 30% and Critical Damage by 35%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('178', '0', 'improved', '6', '1503', '1', 'Combines shield Def. rate increase and Shield Defense. increase to have more advanced shield ability increase effect. For 40 minutes, increases Shield Defense by 30% and Shield Defense by 50%.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('179', '0', 'improved', '12', '1504', '1', 'Combines Spd. increase and Evasion increase to have more advanced movement increase effect. For 40 minutes, increases Speed by 33 and Evasion by 4.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('180', '9', 'others', '23', '1514', '1', 'Spreads the soul\'s defensive barrier to increase your Resistance to bows by 10, Resistance to crossbows by 6, and M. Def. by 100% for 10 seconds.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('181', '0', 'improved', '2', '1517', '1', 'Combines party members\'s P. Atk. increase and P. Def. increase to have more advanced combat power increase effect. For 40 minutes, increases P. Atk. by 15% and P. Def. by 15%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('182', '0', 'improved', '5', '1518', '1', 'Combines party members\'s Critical Rate increase and Critical Damage increase to have more advanced critical increase effect. Increases Critical Rate by 30% and Critical Damage by 35% for 40 minutes.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('183', '0', 'improved', '10', '1519', '1', 'Combines party members\'s general attack damage absorption and Atk. Spd. increase to have a more advanced blood awakening effect. For 40 minutes, increases Atk. Spd. by 33% and bestows the ability to recover as HP 9% of the standard melee damage inflicted on the enemy.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('184', '8', 'special', '1', '1532', '1', 'User receives mystical enlightenment for 20 seconds, increasing M. Atk. by 40%, Casting Spd. by 50% and magic Critical Rate by 50%, and decreasing MP consumption by 90%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('185', '0', 'improved', '11', '1535', '1', 'Combines a party member\'s Speed increase and Evasion increase effects for a more advanced movement increase effect. For 40 minutes, increases Speed by 33 and Evasion by 4.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('186', '0', 'improved', '3', '1536', '1', 'Combines a party/clan member\'s P. Atk. increase and P. Def. increase to have more advanced combat power increase effect. For 40 minutes, increases P. Atk. by 15% and P. Def. by 15%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('187', '0', 'improved', '7', '1537', '1', 'Combines a party/clan member\'s Critical Rate increase and Critical Damage increase effects for a more advanced critical increase effect. For 40 minutes, increases Critical Rate by 30% and Critical Damage by 35%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('188', '0', 'improved', '8', '1538', '1', 'Combines a party/clan member\'s maximum HP increase and Max MP increase effects for more advanced mental and physical power. For 40 minutes, increases Max HP by 35% and Max MP by 35%.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('189', '8', 'special', '4', '1542', '1', 'For 20 minutes, increases the target\'s P. Def. against Critical by 30%. When the target receives an attack above a certain amount of damage, Critical Damage of General Short-Range P. Atk. is increased for 8 seconds.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('190', '9', 'others', '3', '1547', '1', 'For 20 minutes, increases the user\'s P. Atk. by 3%, M. Atk. by 3%, skill P. Atk. by 5%, Atk. Spd. by 2%, Casting Spd. by 1%, and Critical Rate by 5%. The master\'s abilities are extended to the servitor.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('191', '8', 'special', '5', '2076', '1', 'Increase lung capacity. Effect 1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('192', '8', 'special', '6', '4551', '1', 'Infected with Hot Springs Rheumatism. While afflicted, your critical attack success rate is increased. Effect 1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('193', '8', 'special', '7', '4552', '1', 'Infected with Hot Springs Cholera. While afflicted, your Accuracy is increased. Effect  1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('194', '8', 'special', '8', '4553', '1', 'Infected with Hot Springs Flu. While afflicted, your Atk. Spd. is increased. Effect 1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('195', '8', 'special', '9', '4554', '1', 'Infected with Hot Springs Malaria. While afflicted, your Casting Spd. is increased. Effect 1.', '0', '0');
INSERT INTO `zeus_buffer_buff_list` VALUES ('196', '8', 'special', '10', '4699', '13', '+30% Critical Rate, +25% Critical Pow.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('197', '8', 'special', '11', '4700', '13', '+10% P.Atk., +3 Accuracy.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('198', '8', 'special', '12', '4702', '13', 'Buff magic used by the Unicorn Seraphim. Party members\'s MP regeneration bonus temporarily increased. Effect 1.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('199', '8', 'special', '13', '4703', '13', 'u,Unicorn Seraphim\'s buff magic temporarily reduces party members\'s magic skill recovery time. 1.', '0', '1');
INSERT INTO `zeus_buffer_buff_list` VALUES ('200', '1', 'buff', '10', '6049', '1', 'Utters a battle cry that increases one\'s own P. Atk., P. Def. and M. Def.', '0', '0');

-- ----------------------------
-- Table structure for `zeus_buffer_scheme_contents`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buffer_scheme_contents`;
CREATE TABLE `zeus_buffer_scheme_contents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `schemeId` int(11) DEFAULT NULL,
  `buffId` int(5) DEFAULT NULL,
  `buffLevel` int(5) DEFAULT NULL,
  `buffClass` int(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buffer_scheme_contents
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_buffer_scheme_list`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buffer_scheme_list`;
CREATE TABLE `zeus_buffer_scheme_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `playerId` varchar(40) DEFAULT NULL,
  `buffPlayer` tinyint(1) NOT NULL DEFAULT '0',
  `schemeName` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buffer_scheme_list
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_buffstore`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buffstore`;
CREATE TABLE `zeus_buffstore` (
  `idChar` int(10) DEFAULT NULL,
  `idRequest` int(11) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `title` varchar(50) DEFAULT '',
  `clan` enum('true','false') DEFAULT NULL,
  `wan` enum('true','false') DEFAULT NULL,
  `friend` enum('true','false') DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `z` int(11) DEFAULT NULL,
  `time` bigint(13) DEFAULT NULL,
  `ipwan` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buffstore
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_buff_char_buff`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buff_char_buff`;
CREATE TABLE `zeus_buff_char_buff` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idBuff` smallint(6) NOT NULL,
  `level` smallint(6) DEFAULT NULL,
  `nom` varchar(130) DEFAULT NULL,
  `act` enum('1','0') DEFAULT '1',
  `secc` tinyint(4) DEFAULT NULL,
  `descrip` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of zeus_buff_char_buff
-- ----------------------------
INSERT INTO `zeus_buff_char_buff` VALUES ('17', '982', '1', 'Combat Aura', '1', '3', 'Increases P. Atk. and Atk. Spd by 5%');
INSERT INTO `zeus_buff_char_buff` VALUES ('18', '1035', '4', 'Mental Shield', '1', '2', 'Increases resist to hold/sleep');
INSERT INTO `zeus_buff_char_buff` VALUES ('19', '1036', '2', 'Magic Barrier', '1', '2', 'Increases M. Def. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('20', '1040', '3', 'Shield', '1', '2', 'Increases P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('21', '1043', '1', 'Holy Weapon', '1', '1', 'Increases divine P. Atk. by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('22', '1044', '3', 'Regeneration', '1', '1', 'Increases HP Regeneration by 20%');
INSERT INTO `zeus_buff_char_buff` VALUES ('23', '1045', '6', 'Bless the Body', '1', '1', 'Increases Max HP by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('25', '1048', '6', 'Bless the Soul', '1', '1', 'Increases Max MP by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('26', '1059', '3', 'Empower', '1', '1', 'Increases M. Atk. by 75%');
INSERT INTO `zeus_buff_char_buff` VALUES ('27', '1062', '2', 'Berserker Spirit', '1', '1', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('28', '1068', '3', 'Might', '1', '1', 'Increases P. Atk. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('30', '1077', '3', 'Focus', '1', '1', 'Increases critical rate by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('31', '1078', '6', 'Concentration', '1', '2', 'Decrases magic cancel dmg. by 53');
INSERT INTO `zeus_buff_char_buff` VALUES ('32', '1085', '3', 'Acumen', '1', '1', 'Increases Casting Spd. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('33', '1086', '2', 'Haste', '1', '1', 'Increases Atk. Spd. by 33%');
INSERT INTO `zeus_buff_char_buff` VALUES ('34', '1087', '3', 'Agility', '1', '1', 'Increases evasion by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('35', '1204', '2', 'Wind Walk', '1', '1', 'Increases movement speed by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('36', '1232', '3', 'Blazing Skin', '1', '3', 'Transfer 20% of receive damage');
INSERT INTO `zeus_buff_char_buff` VALUES ('37', '1238', '3', 'Freezing Skin', '1', '3', 'Transfer 20% of receive damage');
INSERT INTO `zeus_buff_char_buff` VALUES ('38', '1240', '3', 'Guidance', '1', '1', 'Increases accuracy by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('39', '1242', '3', 'Death Whisper', '1', '1', 'Increases critical power by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('40', '1243', '6', 'Bless Shield', '1', '1', 'Increases shield def. rate by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('42', '1268', '4', 'Vampiric Rage', '1', '1', 'Drain 9% enemy HP by hit');
INSERT INTO `zeus_buff_char_buff` VALUES ('43', '1303', '2', 'Wild Magic', '1', '1', 'Increases magic dmg rate by 2');
INSERT INTO `zeus_buff_char_buff` VALUES ('44', '1304', '3', 'Advanced Block', '1', '2', 'Increases shield defence by 50%');
INSERT INTO `zeus_buff_char_buff` VALUES ('45', '1307', '3', 'Prayer', '1', '4', 'Increases HP Recovery magic by 12%');
INSERT INTO `zeus_buff_char_buff` VALUES ('46', '1352', '1', 'Elemental Protection', '1', '2', 'Resist to fire/water/wind/earth');
INSERT INTO `zeus_buff_char_buff` VALUES ('47', '1353', '1', 'Divine Protection', '1', '2', 'Increases resistance to divine by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('48', '1354', '1', 'Arcane Protection', '1', '2', 'Increases resist to cancel by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('49', '1388', '3', 'Greater Might', '1', '1', 'Increases P. Atk. by 10%');
INSERT INTO `zeus_buff_char_buff` VALUES ('50', '1389', '3', 'Greater Shield', '1', '2', 'Increases P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('51', '1397', '3', 'Clarity', '1', '1', 'Decreases MP consumption');
INSERT INTO `zeus_buff_char_buff` VALUES ('53', '1460', '1', 'Mana Gain', '1', '4', 'Increases recharge power by 85');
INSERT INTO `zeus_buff_char_buff` VALUES ('59', '1032', '1', 'Invigor', '1', '2', 'Increases resistance to bleed by 50%');
INSERT INTO `zeus_buff_char_buff` VALUES ('60', '1033', '1', 'Resist Poison', '1', '2', 'Increases resist to poison by 50');
INSERT INTO `zeus_buff_char_buff` VALUES ('61', '1182', '1', 'Resist Aqua', '1', '2', 'Increases resistance to water by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('62', '1189', '1', 'Resist Wind', '1', '2', 'Increases resistance to wind by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('63', '1191', '1', 'Resist Fire', '1', '2', 'Increases resistance to fire by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('64', '1259', '1', 'Resist Shock', '1', '2', 'Increases resistance to stun by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('65', '1392', '1', 'Resist Holy', '1', '2', 'Increases resistance to divine by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('66', '1393', '1', 'Resist Dark', '1', '2', 'Increases resistance to dark by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('69', '264', '1', 'Song of Earth', '1', '6', 'Increases the P. Def. by 25%');
INSERT INTO `zeus_buff_char_buff` VALUES ('70', '265', '1', 'Song of Life', '1', '6', 'Increases HP Regeneration by 20%');
INSERT INTO `zeus_buff_char_buff` VALUES ('71', '266', '1', 'Song of Water', '1', '6', 'Increases evasion by 3');
INSERT INTO `zeus_buff_char_buff` VALUES ('72', '267', '1', 'Song of Warding', '1', '6', 'Increases M. Def. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('73', '268', '1', 'Song of Wind', '1', '6', 'Increases movement speed by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('74', '269', '1', 'Song of Hunter', '1', '6', 'Increases critical rate by 100%');
INSERT INTO `zeus_buff_char_buff` VALUES ('75', '270', '1', 'Song of Invocation', '1', '6', 'Increases resistance to dark by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('76', '304', '1', 'Song of Vitality', '1', '6', 'Increases Max HP by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('77', '305', '1', 'Song of Vengeance', '1', '6', 'Abillity to transfer 20% dmg back');
INSERT INTO `zeus_buff_char_buff` VALUES ('78', '306', '1', 'Song of Flame Guard', '1', '6', 'Increases resistance to fire by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('79', '308', '1', 'Song of Storm Guard', '1', '6', 'Increases resistance to wind by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('80', '349', '1', 'Song of Renewal', '1', '6', 'Decreases re-use time by 20%');
INSERT INTO `zeus_buff_char_buff` VALUES ('81', '363', '1', 'Song of Meditation', '1', '6', 'Increases MP Regeneration by 20%');
INSERT INTO `zeus_buff_char_buff` VALUES ('82', '364', '1', 'Song of Champion', '1', '6', 'Increases physical re-use by 10%');
INSERT INTO `zeus_buff_char_buff` VALUES ('83', '529', '1', 'Song of Elemental', '1', '6', 'Resistance to fire/water/wind/earth');
INSERT INTO `zeus_buff_char_buff` VALUES ('85', '914', '1', 'Song of Purification', '1', '6', 'Resistance to de-buff attacks by 80%');
INSERT INTO `zeus_buff_char_buff` VALUES ('86', '271', '1', 'Dance of the Warrior', '1', '5', 'Increases the P. Atk. by 12%');
INSERT INTO `zeus_buff_char_buff` VALUES ('87', '272', '1', 'Dance of Inspiration', '1', '5', 'Increases the accuracy by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('88', '273', '1', 'Dance of the Mystic', '1', '5', 'Increases the M. Atk. by 20%');
INSERT INTO `zeus_buff_char_buff` VALUES ('89', '274', '1', 'Dance of Fire', '1', '5', 'Increases the critical by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('90', '275', '1', 'Dance of Fury', '1', '5', 'Increases the Atk. Spd. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('91', '276', '1', 'Dance of Concentration', '1', '5', 'Increases the Cast. Spd. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('92', '277', '1', 'Dance of Light', '1', '5', 'Increases divine P. Atk. by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('93', '307', '1', 'Dance of Aqua Guard', '1', '5', 'Icreases resist to water by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('94', '309', '1', 'Dance of Earth Guard', '1', '5', 'Increases resist to earth by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('95', '310', '1', 'Dance of the Vampire', '1', '5', 'Drain 8% enemy HP by hit');
INSERT INTO `zeus_buff_char_buff` VALUES ('96', '311', '1', 'Dance of Protection', '1', '5', 'Increases resist to cancel by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('97', '365', '1', 'Dance of Siren', '1', '5', 'Increases magic critical rate');
INSERT INTO `zeus_buff_char_buff` VALUES ('98', '366', '1', 'Dance of Shadows', '1', '5', 'Prevents from being attacked');
INSERT INTO `zeus_buff_char_buff` VALUES ('99', '530', '1', 'Dance of Alignment', '1', '5', 'Increases resist to dark/divine by 30');
INSERT INTO `zeus_buff_char_buff` VALUES ('101', '915', '1', 'Dance of Berserker', '1', '5', 'A powerfull spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('104', '825', '1', 'Sharp Edge', '1', '3', 'Increases P. Atk. and critical rate');
INSERT INTO `zeus_buff_char_buff` VALUES ('105', '826', '1', 'Spike', '1', '3', 'Increases P. Atk. and shock weight');
INSERT INTO `zeus_buff_char_buff` VALUES ('106', '827', '1', 'Restring', '1', '3', 'Increases P. Atk. and shoot range');
INSERT INTO `zeus_buff_char_buff` VALUES ('107', '828', '1', 'Case Harden', '1', '3', 'Increases P. Def. by 10%');
INSERT INTO `zeus_buff_char_buff` VALUES ('108', '829', '1', 'Hard Tanning', '1', '3', 'Increases P. Def. and evasion');
INSERT INTO `zeus_buff_char_buff` VALUES ('109', '830', '1', 'Embroider', '1', '3', 'Increases P. Def. and MP recov.rate');
INSERT INTO `zeus_buff_char_buff` VALUES ('110', '1002', '1', 'Flame Chant', '1', '7', 'Increases Casting Spd. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('111', '1003', '1', 'Pa\'agrian Gift', '1', '7', 'Increases P. Atk. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('112', '1004', '1', 'The Wisdom of Pa\'agrio', '1', '7', 'Increases Cast. Spd. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('113', '1005', '1', 'Blessings of Pa\'agrio', '1', '7', 'Increases P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('114', '1006', '1', 'Chant of Fire', '1', '7', 'Increases M. Def. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('115', '1007', '1', 'Chant of Battle', '1', '7', 'Increases P. Atk. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('116', '1008', '1', 'The Glory of Pa\'agrio', '1', '7', 'Increases M. Def. by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('117', '1009', '1', 'Chant of Shielding', '1', '7', 'Increases P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('119', '1249', '1', 'The Vision of Pa\'agrio', '1', '7', 'Increases accuracy by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('120', '1250', '1', 'Shield of Pa\'agrio', '1', '7', 'Increases shield def. rate by 50%');
INSERT INTO `zeus_buff_char_buff` VALUES ('121', '1251', '1', 'Chant of Fury', '1', '7', 'Increases Atk. Spd. by 33%');
INSERT INTO `zeus_buff_char_buff` VALUES ('122', '1252', '1', 'Chant of Evasion', '1', '7', 'Increases evasion by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('123', '1253', '1', 'Chant of Rage', '1', '7', 'Increases critical power by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('124', '1260', '1', 'The Tact of Pa\'agrio', '1', '7', 'Increases evasion by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('125', '1261', '1', 'Rage of Pa\'agrio', '1', '7', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('126', '1282', '1', 'Pa\'agrian Haste', '1', '7', 'Increases movement speed by 20');
INSERT INTO `zeus_buff_char_buff` VALUES ('127', '1284', '1', 'Chant of Revenge', '1', '7', 'Transfer 20% received damage');
INSERT INTO `zeus_buff_char_buff` VALUES ('128', '1308', '1', 'Chant of Predator', '1', '7', 'Increases critical rate by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('129', '1309', '1', 'Chant of Eagle', '1', '7', 'Increases accuracy by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('130', '1310', '1', 'Chant of Vampire', '1', '7', 'Drain 9% enemy HP by hit');
INSERT INTO `zeus_buff_char_buff` VALUES ('131', '1362', '1', 'Chant of Spirit', '1', '7', 'Resist to cancel/de-buffs by 20/30');
INSERT INTO `zeus_buff_char_buff` VALUES ('132', '1364', '1', 'Eye of Pa\'agrio', '1', '7', 'Increases accuracy by 4');
INSERT INTO `zeus_buff_char_buff` VALUES ('133', '1365', '1', 'Soul of Pa\'agrio', '1', '7', 'Increases M. Atk. by 75%');
INSERT INTO `zeus_buff_char_buff` VALUES ('134', '1390', '1', 'War Chant', '1', '7', 'Increases P. Atk. by 10%');
INSERT INTO `zeus_buff_char_buff` VALUES ('135', '1391', '1', 'Earth Chant', '1', '7', 'Increases P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('136', '1413', '1', 'Magnus\' Chant', '1', '7', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('137', '1414', '1', 'Victory of Pa\'agrio', '1', '7', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('138', '1415', '1', 'Pa\'agrio\'s Emblem', '1', '7', 'Increases resist to cancel by 30%');
INSERT INTO `zeus_buff_char_buff` VALUES ('139', '1416', '1', 'Pa\'agrio\'s Fist', '1', '7', 'Increases Max CP by 800');
INSERT INTO `zeus_buff_char_buff` VALUES ('140', '1461', '1', 'Chant of Protection', '1', '7', 'Decreases critical dmg received');
INSERT INTO `zeus_buff_char_buff` VALUES ('143', '1323', '1', 'Noblesse Blessing', '1', '3', 'The blessing of noblesse');
INSERT INTO `zeus_buff_char_buff` VALUES ('146', '1542', '1', 'Counter Critical', '1', '3', 'Increases P. Def. against criticals');
INSERT INTO `zeus_buff_char_buff` VALUES ('152', '4699', '1', 'Blessing of the Queen', '1', '3', 'Increases crit. rate and power');
INSERT INTO `zeus_buff_char_buff` VALUES ('153', '4700', '1', 'Gift of the Queeen', '1', '3', 'Increases P. Atk. and accuracy');
INSERT INTO `zeus_buff_char_buff` VALUES ('154', '4702', '1', 'Blessing of Seraphim', '1', '3', 'Increases MP regeneration');
INSERT INTO `zeus_buff_char_buff` VALUES ('155', '4703', '1', 'Gift of Seraphim', '1', '3', 'Reduces magic skill recovery time');
INSERT INTO `zeus_buff_char_buff` VALUES ('156', '1355', '1', 'Prophecy of Water', '1', '3', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('157', '1356', '1', 'Prophecy of Fire', '1', '3', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('158', '1357', '1', 'Prophecy of Wind', '1', '3', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('159', '1363', '1', 'Chant of Victory', '1', '3', 'A powerful spirit acts');
INSERT INTO `zeus_buff_char_buff` VALUES ('160', '1499', '1', 'Improved Combat', '1', '4', 'Increases P. Att. and P.Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('161', '1500', '1', 'Improved Magic', '1', '4', 'Increases M. Att. and M. Def.');
INSERT INTO `zeus_buff_char_buff` VALUES ('162', '1501', '1', 'Improved Condition', '1', '4', 'Increases Max HP and MP by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('163', '1502', '1', 'Improved Critical Attack', '1', '4', 'Increases critical rate and dmg');
INSERT INTO `zeus_buff_char_buff` VALUES ('164', '1503', '1', 'Improved Shield Defense', '1', '4', 'Increases shield defence');
INSERT INTO `zeus_buff_char_buff` VALUES ('165', '1504', '1', 'Improved Movement', '1', '4', 'Increases speed and evasion');
INSERT INTO `zeus_buff_char_buff` VALUES ('168', '1519', '1', 'Chant of Blood Awakening', '1', '7', 'Increases Atk.Spd 33% and drain 9%');
INSERT INTO `zeus_buff_char_buff` VALUES ('169', '1535', '1', 'Chant of Movement', '1', '7', 'Increases Speed and Evasion');
INSERT INTO `zeus_buff_char_buff` VALUES ('170', '1536', '1', 'Combat of Pa\'agrio', '0', '4', 'Increases P. Att. and P. Def. by 15%');
INSERT INTO `zeus_buff_char_buff` VALUES ('171', '1537', '1', 'Critical of Pa\'agrio', '1', '4', 'Increases Critical Rate and Damage');
INSERT INTO `zeus_buff_char_buff` VALUES ('172', '1538', '1', 'Condition of Pa\'agrio', '1', '4', 'Increases Max HP and MP by 35%');
INSERT INTO `zeus_buff_char_buff` VALUES ('212', '1311', '6', 'Body of Avatar', '0', '4', 'Increases Max Hp by 35%');

-- ----------------------------
-- Table structure for `zeus_buff_char_cate`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buff_char_cate`;
CREATE TABLE `zeus_buff_char_cate` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `nomCat` varchar(30) DEFAULT NULL,
  `posi` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buff_char_cate
-- ----------------------------
INSERT INTO `zeus_buff_char_cate` VALUES ('1', 'Buff', '1');
INSERT INTO `zeus_buff_char_cate` VALUES ('2', 'Resist', '2');
INSERT INTO `zeus_buff_char_cate` VALUES ('3', 'Special', '3');
INSERT INTO `zeus_buff_char_cate` VALUES ('4', 'Other', '4');
INSERT INTO `zeus_buff_char_cate` VALUES ('5', 'Dance', '5');
INSERT INTO `zeus_buff_char_cate` VALUES ('6', 'Song', '6');
INSERT INTO `zeus_buff_char_cate` VALUES ('7', 'Chant', '7');

-- ----------------------------
-- Table structure for `zeus_buff_char_sch`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buff_char_sch`;
CREATE TABLE `zeus_buff_char_sch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `idChar` int(11) NOT NULL,
  `NomSch` varchar(15) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buff_char_sch
-- ----------------------------
INSERT INTO `zeus_buff_char_sch` VALUES ('15', '268482228', 'asdasd');

-- ----------------------------
-- Table structure for `zeus_buff_char_sch_buff`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buff_char_sch_buff`;
CREATE TABLE `zeus_buff_char_sch_buff` (
  `idSch` bigint(20) NOT NULL,
  `idBuff` int(11) NOT NULL,
  `lvlBuff` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buff_char_sch_buff
-- ----------------------------
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1189', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1191', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1352', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1032', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1033', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1353', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('1', '1354', '1');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('11', '1059', '3');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('10', '1068', '3');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('11', '1388', '3');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('10', '1062', '2');
INSERT INTO `zeus_buff_char_sch_buff` VALUES ('11', '1043', '1');

-- ----------------------------
-- Table structure for `zeus_buff_for_aio`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_buff_for_aio`;
CREATE TABLE `zeus_buff_for_aio` (
  `nombreBuff` varchar(80) DEFAULT NULL,
  `idBuff` int(11) DEFAULT NULL,
  `buffLevel` int(11) DEFAULT NULL,
  `buffEnchant` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_buff_for_aio
-- ----------------------------
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Champion', '364', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Shield', '1040', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Holy Weapon', '1043', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Regeneration', '1044', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Magnus\'s Chant', '1413', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Magician\'s Movement', '118', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Cubic Mastery', '143', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dual Weapon Mastery', '144', '37', '37');
INSERT INTO `zeus_buff_for_aio` VALUES ('Anti Magic', '146', '45', '45');
INSERT INTO `zeus_buff_for_aio` VALUES ('Spellcraft', '163', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Lucky', '194', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mana Recovery', '214', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Fast Spell Casting', '228', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Fast Mana Recovery', '229', '7', '7');
INSERT INTO `zeus_buff_for_aio` VALUES ('Light Armor Mastery', '236', '41', '41');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Earth', '264', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Life', '265', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Water', '266', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Warding', '267', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Wind', '268', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Hunter', '269', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Invocation', '270', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of the Warrior', '271', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Inspiration', '272', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of the Mystic', '273', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Fire', '274', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Fury', '275', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Concentration', '276', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Light', '277', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Vitality', '304', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Vengeance', '305', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Flame Guard', '306', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Aqua Guard', '307', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Storm Guard', '308', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Earth Guard', '309', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of the Vampire', '310', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Protection', '311', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Wisdom', '328', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Renewal', '349', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Meditation', '363', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Champion', '364', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Siren', '365', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Summon Lore', '435', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Divine Lore', '436', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Elemental', '529', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Alignment', '530', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Flame Chant', '1002', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Pa\'agrian Gift', '1003', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Blessings of Pa\'agrio', '1005', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Fire', '1006', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Battle', '1007', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Shielding', '1009', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Soul Shield', '1010', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Invigor', '1032', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Poison', '1033', '315', '315');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mental Shield', '1035', '4', '4');
INSERT INTO `zeus_buff_for_aio` VALUES ('Magic Barrier', '1036', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Shield', '1040', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Holy Weapon', '1043', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Regeneration', '1044', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Bless the Body', '1045', '6', '6');
INSERT INTO `zeus_buff_for_aio` VALUES ('Bless the Soul', '1048', '6', '6');
INSERT INTO `zeus_buff_for_aio` VALUES ('Empower', '1059', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Berserker Spirit', '1062', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Focus', '1077', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Concentration', '1078', '6', '6');
INSERT INTO `zeus_buff_for_aio` VALUES ('Acumen', '1085', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Haste', '1086', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Agility', '1087', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Aqua', '1182', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Wind', '1189', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Fire', '1191', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Earth', '1548', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Holy', '1392', '130', '130');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Dark', '1393', '130', '130');
INSERT INTO `zeus_buff_for_aio` VALUES ('Wind Walk', '1204', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Self Heal', '1216', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Guidance', '1240', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Death Whisper', '1242', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Bless Shield', '1243', '6', '6');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Fury', '1251', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Evasion', '1252', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Rage', '1253', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Resist Shock', '1259', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Vampiric Rage', '1268', '4', '4');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Revenge', '1284', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Wild Magic', '1303', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Advanced Block', '1304', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Predator', '1308', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Eagle', '1309', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Vampire', '1310', '4', '4');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mass Summon Storm Cubic', '1328', '8', '8');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mass Summon Aqua Cubic', '1329', '9', '9');
INSERT INTO `zeus_buff_for_aio` VALUES ('Summon Feline Queen', '1331', '10', '10');
INSERT INTO `zeus_buff_for_aio` VALUES ('Summon Seraphim the Unicorn', '1332', '10', '10');
INSERT INTO `zeus_buff_for_aio` VALUES ('Elemental Protection', '1352', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Divine Protection', '1353', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Arcane Protection', '1354', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Prophecy of Water', '1355', '315', '315');
INSERT INTO `zeus_buff_for_aio` VALUES ('Prophecy of Fire', '1356', '315', '315');
INSERT INTO `zeus_buff_for_aio` VALUES ('Prophecy of Wind', '1357', '315', '315');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Spirit', '1362', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Victory', '1363', '315', '315');
INSERT INTO `zeus_buff_for_aio` VALUES ('Greater Might', '1388', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Greater Shield', '1389', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('War Chant', '1390', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Earth Chant', '1391', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Erase', '1395', '10', '10');
INSERT INTO `zeus_buff_for_aio` VALUES ('Clarity', '1397', '230', '230');
INSERT INTO `zeus_buff_for_aio` VALUES ('Summon Friend', '1403', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Cleanse', '1409', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Salvation', '1410', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Magnus\'s Chant', '1413', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Gate Chant', '1429', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Invocation', '1430', '5', '5');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mana Gain', '1460', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Protection', '1461', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Steal Mana', '1526', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Song of Wind Storm', '764', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dance of Blade Storm', '765', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Magician\'s Will', '945', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Combat', '1499', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Condition', '1501', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Blessing of Eva', '1506', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Counter Critical', '1542', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('The Wisdom of Pa\'agrio', '1004', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('The Glory of Pa\'agrio', '1008', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('The Vision of Pa\'agrio', '1249', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Shield of Pa\'agrio', '1250', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Condition of Pa\'agrio', '1538', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Movement', '1535', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('The Tact of Pa\'agrio', '1260', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Rage of Pa\'agrio', '1261', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Pa\'agrian Haste', '1282', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Eye of Pa\'agrio', '1364', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Soul of Pa\'agrio', '1365', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Victory of Pa\'agrio', '1414', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Pa\'agrio\'s Emblem', '1415', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Pa\'agrio\'s Fist', '1416', '115', '115');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Movement', '1504', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Sharp Edge', '825', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Spike', '826', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Restring', '827', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Case Harden', '828', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Hard Tanning', '829', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Embroider', '830', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Protection from Darkness', '1442', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Dark Weapon', '1443', '130', '130');
INSERT INTO `zeus_buff_for_aio` VALUES ('Blazing Skin', '1232', '330', '330');
INSERT INTO `zeus_buff_for_aio` VALUES ('Combat Aura', '982', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Magic', '1500', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Critical Attack', '1502', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Improved Shield Defense', '1503', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Blood Awakening', '1519', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Blessing of Vitality', '23179', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Super Haste', '7029', '3', '3');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Elements', '1549', '1', '1');
INSERT INTO `zeus_buff_for_aio` VALUES ('Chant of Berserker', '1562', '2', '2');
INSERT INTO `zeus_buff_for_aio` VALUES ('Boost Mana AIO', '10003', '130', '130');
INSERT INTO `zeus_buff_for_aio` VALUES ('Mana Recovery AIO', '10004', '1', '1');

-- ----------------------------
-- Table structure for `zeus_bug_report`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_bug_report`;
CREATE TABLE `zeus_bug_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tipo` varchar(50) DEFAULT NULL,
  `mensaje` varchar(350) DEFAULT NULL,
  `fechaIngreso` datetime DEFAULT NULL,
  `PlayerNom` varchar(80) DEFAULT NULL,
  `leido` enum('SI','NO') DEFAULT 'NO',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_bug_report
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_cb_clan_foro`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_cb_clan_foro`;
CREATE TABLE `zeus_cb_clan_foro` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idChar` int(11) DEFAULT NULL,
  `idClan` int(11) DEFAULT NULL,
  `memo` text,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_cb_clan_foro
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_char_config`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_char_config`;
CREATE TABLE `zeus_char_config` (
  `idchar` int(10) NOT NULL,
  `annouc` tinyint(4) DEFAULT '0',
  `effect` tinyint(4) DEFAULT '0',
  `statt` tinyint(4) DEFAULT '0',
  `pin` tinyint(4) DEFAULT '0',
  `pinCode` varchar(4) DEFAULT '9876',
  `banOly` tinyint(4) DEFAULT '0',
  `hero` tinyint(4) DEFAULT '0',
  `expsp` tinyint(4) DEFAULT '1',
  `trade` tinyint(4) DEFAULT '1',
  `badbuff` tinyint(4) DEFAULT '0',
  `hidestore` tinyint(4) DEFAULT '0',
  `refusal` tinyint(4) DEFAULT '0',
  `partymatching` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`idchar`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_char_config
-- ----------------------------
INSERT INTO `zeus_char_config` VALUES ('268482194', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268482202', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268482209', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268482217', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268482228', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268482235', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268483162', '0', '0', '0', '0', '9876', '0', '0', '1', '0', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268486690', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268497175', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268499225', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268499279', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268499824', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268500250', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');
INSERT INTO `zeus_char_config` VALUES ('268500833', '0', '0', '0', '0', '9876', '0', '0', '1', '1', '0', '0', '0', '0');

-- ----------------------------
-- Table structure for `zeus_config_seccion`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_config_seccion`;
CREATE TABLE `zeus_config_seccion` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `seccion` varchar(80) NOT NULL DEFAULT '',
  `param` varchar(550) DEFAULT NULL,
  PRIMARY KEY (`id`,`seccion`)
) ENGINE=InnoDB AUTO_INCREMENT=598 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_config_seccion
-- ----------------------------
INSERT INTO `zeus_config_seccion` VALUES ('1', 'ID_NPC', '955');
INSERT INTO `zeus_config_seccion` VALUES ('2', 'ID_NPC_CH', '956');
INSERT INTO `zeus_config_seccion` VALUES ('3', 'MAX_LISTA_PVP', '30');
INSERT INTO `zeus_config_seccion` VALUES ('4', 'MAX_LISTA_PVP_LOG', '30');
INSERT INTO `zeus_config_seccion` VALUES ('5', 'DEBUG_CONSOLA_ENTRADAS', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('6', 'DEBUG_CONSOLA_ENTRADAS_TO_USER', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('7', 'BTN_SHOW_VOTE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('8', 'BTN_SHOW_BUFFER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('9', 'BTN_SHOW_TELEPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('10', 'BTN_SHOW_SHOP', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('11', 'BTN_SHOW_WAREHOUSE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('12', 'BTN_SHOW_AUGMENT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('13', 'BTN_SHOW_SUBCLASES', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('14', 'BTN_SHOW_CLASS_TRANSFER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('15', 'BTN_SHOW_CONFIG_PANEL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('16', 'BTN_SHOW_DROP_SEARCH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('17', 'BTN_SHOW_PVPPK_LIST', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('18', 'BTN_SHOW_LOG_PELEAS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('19', 'BTN_SHOW_CASTLE_MANAGER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('20', 'BTN_SHOW_DESAFIO', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('21', 'BTN_SHOW_SYMBOL_MARKET', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('22', 'BTN_SHOW_CLANALLY', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('23', 'BTN_SHOW_PARTYFINDER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('24', 'BTN_SHOW_FLAGFINDER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('25', 'BTN_SHOW_COLORNAME', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('26', 'BTN_SHOW_DELEVEL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('27', 'BTN_SHOW_REMOVE_ATRIBUTE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('28', 'BTN_SHOW_BUG_REPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('29', 'BTN_SHOW_DONATION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('30', 'BTN_SHOW_CAMBIO_NOMBRE_PJ', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('31', 'BTN_SHOW_CAMBIO_NOMBRE_CLAN', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('32', 'BTN_SHOW_VARIAS_OPCIONES', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('33', 'BTN_SHOW_ELEMENT_ENHANCED', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('34', 'BTN_SHOW_ENCANTAMIENTO_ITEM', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('35', 'BTN_SHOW_AUGMENT_SPECIAL', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('36', 'BTN_SHOW_GRAND_BOSS_STATUS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('37', 'BTN_SHOW_RAIDBOSS_INFO', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('38', 'BTN_SHOW_VOTE_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('39', 'BTN_SHOW_BUFFER_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('40', 'BTN_SHOW_TELEPORT_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('41', 'BTN_SHOW_SHOP_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('42', 'BTN_SHOW_WAREHOUSE_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('43', 'BTN_SHOW_AUGMENT_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('44', 'BTN_SHOW_SUBCLASES_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('45', 'BTN_SHOW_CLASS_TRANSFER_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('46', 'BTN_SHOW_CONFIG_PANEL_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('47', 'BTN_SHOW_DROP_SEARCH_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('48', 'BTN_SHOW_PVPPK_LIST_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('49', 'BTN_SHOW_LOG_PELEAS_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('50', 'BTN_SHOW_CASTLE_MANAGER_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('51', 'BTN_SHOW_DESAFIO_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('52', 'BTN_SHOW_SYMBOL_MARKET_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('53', 'BTN_SHOW_CLANALLY_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('54', 'BTN_SHOW_PARTYFINDER_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('55', 'BTN_SHOW_FLAGFINDER_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('56', 'BTN_SHOW_COLORNAME_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('57', 'BTN_SHOW_DELEVEL_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('58', 'BTN_SHOW_REMOVE_ATRIBUTE_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('59', 'BTN_SHOW_BUG_REPORT_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('60', 'BTN_SHOW_DONATION_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('61', 'BTN_SHOW_CAMBIO_NOMBRE_PJ_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('62', 'BTN_SHOW_CAMBIO_NOMBRE_CLAN_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('63', 'BTN_SHOW_VARIAS_OPCIONES_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('64', 'BTN_SHOW_ELEMENT_ENHANCED_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('65', 'BTN_SHOW_ENCANTAMIENTO_ITEM_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('66', 'BTN_SHOW_AUGMENT_SPECIAL_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('67', 'BTN_SHOW_GRAND_BOSS_STATUS_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('68', 'BTN_SHOW_RAIDBOSS_INFO_CH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('69', 'VOTO_REWARD_TOPZONE', '57,8000000');
INSERT INTO `zeus_config_seccion` VALUES ('70', 'VOTO_REWARD_HOPZONE', '3470,5');
INSERT INTO `zeus_config_seccion` VALUES ('71', 'VOTO_REWARD_ACTIVO_TOPZONE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('72', 'VOTO_REWARD_ACTIVO_HOPZONE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('73', 'VOTO_REWARD_SEG_ESPERA', '30');
INSERT INTO `zeus_config_seccion` VALUES ('74', 'VOTO_ITEM_BUFF_ENCHANT_PRICE', '3470,2');
INSERT INTO `zeus_config_seccion` VALUES ('75', 'VOTO_WEB_GET_VOTOS', 'www');
INSERT INTO `zeus_config_seccion` VALUES ('76', 'DONA_ID_ITEM', '3470');
INSERT INTO `zeus_config_seccion` VALUES ('82', 'DONA_WEB_SEND_NOTIFICACION', 'www');
INSERT INTO `zeus_config_seccion` VALUES ('83', 'TELEPORT_PRICE', '57,85000');
INSERT INTO `zeus_config_seccion` VALUES ('84', 'FREE_TELEPORT', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('85', 'DESAFIO_85_PREMIO', '3470,10;57,547874');
INSERT INTO `zeus_config_seccion` VALUES ('86', 'DESAFIO_NOBLE_PREMIO', '3470,20;57,87878787');
INSERT INTO `zeus_config_seccion` VALUES ('87', 'DESAFIO_NPC_BUSQUEDAS', '8,70030,70033');
INSERT INTO `zeus_config_seccion` VALUES ('88', 'DROP_SEARCH_COBRAR_TELEPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('89', 'DROP_TELEPORT_COST', '57,800000');
INSERT INTO `zeus_config_seccion` VALUES ('90', 'DROP_SEARCH_MOSTRAR_LISTA', '18');
INSERT INTO `zeus_config_seccion` VALUES ('91', 'PARTY_FINDER_PRICE', '57,8000000;3470,1000');
INSERT INTO `zeus_config_seccion` VALUES ('92', 'PARTY_FINDER_GO_LEADER_DEATH', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('93', 'PARTY_FINDER_GO_LEADER_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('94', 'PARTY_FINDER_GO_LEADER_FLAGPK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('95', 'PARTY_FINDER_CAN_USE_PK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('96', 'PARTY_FINDER_CAN_USE_FLAG', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('97', 'PARTY_FINDER_CAN_USE_LVL', '40');
INSERT INTO `zeus_config_seccion` VALUES ('98', 'FLAG_FINDER_PRICE', '57,800000;3470,8');
INSERT INTO `zeus_config_seccion` VALUES ('99', 'FLAG_FINDER_CAN_USE_FLAG', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('100', 'FLAG_FINDER_CAN_USE_PK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('101', 'FLAG_FINDER_CAN_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('102', 'FLAG_FINDER_LVL', '40');
INSERT INTO `zeus_config_seccion` VALUES ('103', 'FLAG_PVP_PK_LVL_MIN', '60');
INSERT INTO `zeus_config_seccion` VALUES ('104', 'PINTAR_PRICE', '57,8000;3470,300');
INSERT INTO `zeus_config_seccion` VALUES ('105', 'PINTAR_COLORS', '0000FF,00FF00,FFFF00,CCEEFF,81DAF5');
INSERT INTO `zeus_config_seccion` VALUES ('106', 'AUGMENT_ITEM_PRICE', '57,585858');
INSERT INTO `zeus_config_seccion` VALUES ('107', 'AUGMENT_SPECIAL_PRICE', '3470,10');
INSERT INTO `zeus_config_seccion` VALUES ('108', 'AUGMENT_SPECIAL_x_PAGINA', '20');
INSERT INTO `zeus_config_seccion` VALUES ('109', 'AUGMENT_SPECIAL_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('110', 'AUGMENT_SPECIAL_LVL', '80');
INSERT INTO `zeus_config_seccion` VALUES ('111', 'ENCHANT_ITEM_PRICE', '3470,80');
INSERT INTO `zeus_config_seccion` VALUES ('112', 'ENCHANT_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('113', 'ENCHANT_LVL', '80');
INSERT INTO `zeus_config_seccion` VALUES ('114', 'ENCHANT_MIN_ENCHANT', '10');
INSERT INTO `zeus_config_seccion` VALUES ('115', 'ENCHANT_MAX_ENCHANT', '18');
INSERT INTO `zeus_config_seccion` VALUES ('116', 'ENCHANT_x_VEZ', '3');
INSERT INTO `zeus_config_seccion` VALUES ('117', 'RAIDBOSS_INFO_TELEPORT_PRICE', '57,85000;3470,1');
INSERT INTO `zeus_config_seccion` VALUES ('118', 'RAIDBOSS_INFO_LISTA_X_HOJA', '20');
INSERT INTO `zeus_config_seccion` VALUES ('119', 'RAIDBOSS_INFO_TELEPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('120', 'RAIDBOSS_INFO_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('121', 'RAIDBOSS_INFO_LVL', '80');
INSERT INTO `zeus_config_seccion` VALUES ('122', 'OPCIONES_CHAR_SEXO_ITEM_PRICE', '3470,2');
INSERT INTO `zeus_config_seccion` VALUES ('123', 'OPCIONES_CHAR_NOBLE_ITEM_PRICE', '3470,2;57,80000');
INSERT INTO `zeus_config_seccion` VALUES ('124', 'OPCIONES_CHAR_LVL85_ITEM_PRICE', '3470,2');
INSERT INTO `zeus_config_seccion` VALUES ('125', 'OPCIONES_CHAR_CAMBIO_NOMBRE_PJ_ITEM_PRICE', '3470,8');
INSERT INTO `zeus_config_seccion` VALUES ('126', 'OPCIONES_CHAR_CAMBIO_NOMBRE_CLAN_ITEM_PRICE', '57,8788555;3470,10');
INSERT INTO `zeus_config_seccion` VALUES ('127', 'OPCIONES_CHAR_CAMBIO_NOMBRE_NOBLE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('128', 'OPCIONES_CHAR_CAMBIO_NOMBRE_LVL', '40');
INSERT INTO `zeus_config_seccion` VALUES ('129', 'OPCIONES_CHAR_CAMBIO_NOMBRE_CLAN_LVL', '4');
INSERT INTO `zeus_config_seccion` VALUES ('130', 'ELEMENTAL_ITEM_PRICE', '3470,5');
INSERT INTO `zeus_config_seccion` VALUES ('131', 'ELEMENTAL_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('132', 'ELEMENTAL_LVL', '40');
INSERT INTO `zeus_config_seccion` VALUES ('133', 'DELEVEL_PRICE', '57,800000');
INSERT INTO `zeus_config_seccion` VALUES ('134', 'DELEVEL_LVL_MAX', '50');
INSERT INTO `zeus_config_seccion` VALUES ('135', 'DELEVEL_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('136', 'BUFFER_ID_ITEM', '57');
INSERT INTO `zeus_config_seccion` VALUES ('137', 'BUFFER_CON_KARMA', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('138', 'BUFFER_LVL_MIN', '1');
INSERT INTO `zeus_config_seccion` VALUES ('139', 'BUFFER_TIME_WAIT', '10');
INSERT INTO `zeus_config_seccion` VALUES ('140', 'BUFF_GRATIS', 'False');
INSERT INTO `zeus_config_seccion` VALUES ('141', 'BUFFER_GM_ONLY', 'False');
INSERT INTO `zeus_config_seccion` VALUES ('142', 'BUFFER_ID_ACCESO_GM', '1');
INSERT INTO `zeus_config_seccion` VALUES ('143', 'BUFFER_ID_ACCESO_ADMIN', '8');
INSERT INTO `zeus_config_seccion` VALUES ('144', 'BUFFER_SINGLE_BUFF_CHOICE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('145', 'BUFFER_SCHEME_SYSTEM', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('146', 'BUFFER_SCHEMA_X_PLAYER', '3');
INSERT INTO `zeus_config_seccion` VALUES ('147', 'BUFFER_IMPROVED_SECTION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('148', 'BUFFER_IMPROVED_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('149', 'BUFFER_BUFF_SECTION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('150', 'BUFFER_BUFF_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('151', 'BUFFER_CHANT_SECTION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('152', 'BUFFER_CHANT_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('153', 'BUFFER_DANCE_SECTION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('154', 'BUFFER_DANCE_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('155', 'BUFFER_SONG_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('156', 'BUFFER_SONG_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('157', 'BUFFER_RESIST_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('158', 'BUFFER_RESIST_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('159', 'BUFFER_CUBIC_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('160', 'BUFFER_CUBIC_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('161', 'BUFFER_PROPHECY_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('162', 'BUFFER_PROHECY_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('163', 'BUFFER_SPECIAL_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('164', 'BUFFER_SPECIAL_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('165', 'BUFFER_OTROS_SECTION', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('166', 'BUFFER_OTROS_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('167', 'BUFFER_AUTOBUFF', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('168', 'BUFFER_HEAL', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('169', 'BUFFER_HEAL_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('170', 'BUFFER_REMOVE_BUFF', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('171', 'BUFFER_REMOVE_BUFF_VALOR', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('172', 'RATE_EXP_OFF', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('173', 'LOG_FIGHT_PVP_PK', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('174', 'ENCHANT_ANNOUCEMENT', '4,5,6,7,9,11');
INSERT INTO `zeus_config_seccion` VALUES ('175', 'PVP_PK_PROTECTION_LVL', '30');
INSERT INTO `zeus_config_seccion` VALUES ('176', 'ALLOW_BLESSED_ESCAPE_PVP', 'False');
INSERT INTO `zeus_config_seccion` VALUES ('177', 'PVP_PK_GRAFICAL_EFFECT', 'True');
INSERT INTO `zeus_config_seccion` VALUES ('178', 'PVP_COLOR_SYSTEM_ENABLED', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('179', 'PK_COLOR_SYSTEM_ENABLED', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('180', 'PVP_AMOUNT_1', '120');
INSERT INTO `zeus_config_seccion` VALUES ('181', 'PVP_AMOUNT_2', '200');
INSERT INTO `zeus_config_seccion` VALUES ('182', 'PVP_AMOUNT_3', '300');
INSERT INTO `zeus_config_seccion` VALUES ('183', 'PVP_AMOUNT_4', '400');
INSERT INTO `zeus_config_seccion` VALUES ('184', 'PVP_AMOUNT_5', '500');
INSERT INTO `zeus_config_seccion` VALUES ('185', 'PVP_AMOUNT_6', '600');
INSERT INTO `zeus_config_seccion` VALUES ('186', 'PVP_AMOUNT_7', '700');
INSERT INTO `zeus_config_seccion` VALUES ('187', 'PVP_AMOUNT_8', '800');
INSERT INTO `zeus_config_seccion` VALUES ('188', 'PVP_AMOUNT_9', '900');
INSERT INTO `zeus_config_seccion` VALUES ('189', 'PVP_AMOUNT_10', '1000');
INSERT INTO `zeus_config_seccion` VALUES ('190', 'NAME_COLOR_FOR_PVP_AMOUNT_1', 'ffff00');
INSERT INTO `zeus_config_seccion` VALUES ('191', 'NAME_COLOR_FOR_PVP_AMOUNT_2', 'ffffff');
INSERT INTO `zeus_config_seccion` VALUES ('192', 'NAME_COLOR_FOR_PVP_AMOUNT_3', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('193', 'NAME_COLOR_FOR_PVP_AMOUNT_4', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('194', 'NAME_COLOR_FOR_PVP_AMOUNT_5', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('195', 'NAME_COLOR_FOR_PVP_AMOUNT_6', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('196', 'NAME_COLOR_FOR_PVP_AMOUNT_7', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('197', 'NAME_COLOR_FOR_PVP_AMOUNT_8', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('198', 'NAME_COLOR_FOR_PVP_AMOUNT_9', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('199', 'NAME_COLOR_FOR_PVP_AMOUNT_10', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('200', 'PK_AMOUNT_1', '50');
INSERT INTO `zeus_config_seccion` VALUES ('201', 'PK_AMOUNT_2', '100');
INSERT INTO `zeus_config_seccion` VALUES ('202', 'PK_AMOUNT_3', '150');
INSERT INTO `zeus_config_seccion` VALUES ('203', 'PK_AMOUNT_4', '200');
INSERT INTO `zeus_config_seccion` VALUES ('204', 'PK_AMOUNT_5', '250');
INSERT INTO `zeus_config_seccion` VALUES ('205', 'PK_AMOUNT_6', '300');
INSERT INTO `zeus_config_seccion` VALUES ('206', 'PK_AMOUNT_7', '350');
INSERT INTO `zeus_config_seccion` VALUES ('207', 'PK_AMOUNT_8', '400');
INSERT INTO `zeus_config_seccion` VALUES ('208', 'PK_AMOUNT_9', '80');
INSERT INTO `zeus_config_seccion` VALUES ('209', 'PK_AMOUNT_10', '500');
INSERT INTO `zeus_config_seccion` VALUES ('210', 'TITLE_COLOR_FOR_PK_AMOUNT_1', 'ffffff');
INSERT INTO `zeus_config_seccion` VALUES ('211', 'TITLE_COLOR_FOR_PK_AMOUNT_2', '00ffff');
INSERT INTO `zeus_config_seccion` VALUES ('212', 'TITLE_COLOR_FOR_PK_AMOUNT_3', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('213', 'TITLE_COLOR_FOR_PK_AMOUNT_4', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('214', 'TITLE_COLOR_FOR_PK_AMOUNT_5', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('215', 'TITLE_COLOR_FOR_PK_AMOUNT_6', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('216', 'TITLE_COLOR_FOR_PK_AMOUNT_7', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('217', 'TITLE_COLOR_FOR_PK_AMOUNT_8', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('218', 'TITLE_COLOR_FOR_PK_AMOUNT_9', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('219', 'TITLE_COLOR_FOR_PK_AMOUNT_10', 'D358F7');
INSERT INTO `zeus_config_seccion` VALUES ('220', 'MENSAJE_PVP_PK_CICLOS', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('221', 'CANTIDAD_CICLO_MENSAJE_PVP_1', '10');
INSERT INTO `zeus_config_seccion` VALUES ('222', 'CANTIDAD_CICLO_MENSAJE_PVP_2', '20');
INSERT INTO `zeus_config_seccion` VALUES ('223', 'CANTIDAD_CICLO_MENSAJE_PVP_3', '30');
INSERT INTO `zeus_config_seccion` VALUES ('224', 'CANTIDAD_CICLO_MENSAJE_PVP_4', '40');
INSERT INTO `zeus_config_seccion` VALUES ('225', 'CANTIDAD_CICLO_MENSAJE_PVP_5', '50');
INSERT INTO `zeus_config_seccion` VALUES ('226', 'CANTIDAD_CICLO_MENSAJE_PK_1', '2');
INSERT INTO `zeus_config_seccion` VALUES ('227', 'CANTIDAD_CICLO_MENSAJE_PK_2', '5');
INSERT INTO `zeus_config_seccion` VALUES ('228', 'CANTIDAD_CICLO_MENSAJE_PK_3', '15');
INSERT INTO `zeus_config_seccion` VALUES ('229', 'CANTIDAD_CICLO_MENSAJE_PK_4', '30');
INSERT INTO `zeus_config_seccion` VALUES ('230', 'CANTIDAD_CICLO_MENSAJE_PK_5', '40');
INSERT INTO `zeus_config_seccion` VALUES ('231', 'CICLO_MENSAJE_PVP_1', '%CHAR_NAME% es un asesino en masa con %CANT% de PVP sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('232', 'CICLO_MENSAJE_PVP_2', '%CHAR_NAME% es un asesino en masa con %CANT% de PVP sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('233', 'CICLO_MENSAJE_PVP_3', '%CHAR_NAME% es un asesino en masa con %CANT% de PVP sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('234', 'CICLO_MENSAJE_PVP_4', '%CHAR_NAME% es un asesino en masa con %CANT% de PVP sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('235', 'CICLO_MENSAJE_PVP_5', '%CHAR_NAME% es un asesino en masa con %CANT% de PVP sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('236', 'CICLO_MENSAJE_PK_1', '%CHAR_NAME% es un asesino en masa con %CANT% de PK sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('237', 'CICLO_MENSAJE_PK_2', '%CHAR_NAME% es un asesino en masa con %CANT% de PK sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('238', 'CICLO_MENSAJE_PK_3', '%CHAR_NAME% es un asesino en masa con %CANT% de PK sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('239', 'CICLO_MENSAJE_PK_4', '%CHAR_NAME% es un asesino en masa con %CANT% de PK sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('240', 'CICLO_MENSAJE_PK_5', '%CHAR_NAME% es un asesino en masa con %CANT% de PK sin haber muerto');
INSERT INTO `zeus_config_seccion` VALUES ('241', 'ANNOUCE_RAID_BOS_STATUS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('242', 'RAID_ANNOUCEMENT_DIED', 'Raid Boss %RAID_NAME% a muerto, Renacera el %DATE%');
INSERT INTO `zeus_config_seccion` VALUES ('243', 'RAID_ANNOUCEMENT_LIFE', 'Raid Boss %RAID_NAME% a revivido');
INSERT INTO `zeus_config_seccion` VALUES ('244', 'RAID_ANNOUCEMENT_ID_ANNOUCEMENT', '9');
INSERT INTO `zeus_config_seccion` VALUES ('245', 'ANNOUCE_TOP_PPVPPK_ENTER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('246', 'MENSAJE_ENTRADA_PJ_TOPPVPPK', '%CHAR_NAME% Asesino en Serie');
INSERT INTO `zeus_config_seccion` VALUES ('247', 'ANNOUCE_PJ_KARMA', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('248', 'MENSAJE_ENTRADA_PJ_KARMA', '%CHAR_NAME% have %KARMA% karma');
INSERT INTO `zeus_config_seccion` VALUES ('249', 'ANNOUCE_PJ_KARMA_CANTIDAD', '200');
INSERT INTO `zeus_config_seccion` VALUES ('250', 'ANNOUCE_CLASS_OPONENT_OLY', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('251', 'SHOW_MY_STAT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('252', 'BTN_SHOW_TRANSFORMACION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('253', 'TRANSFORM_NOBLE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('254', 'TRANSFORM_LVL', '20');
INSERT INTO `zeus_config_seccion` VALUES ('255', 'TRANSFORM_PRICE', '57,85555555');
INSERT INTO `zeus_config_seccion` VALUES ('256', 'TRANSFORM_ESPECIALES', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('257', 'TRANSFORM_RAIDBOSS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('258', 'BTN_SHOW_TRANSFORMACION_CH', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('259', 'TRANSFORM_ESPECIALES_PRICE', '57,8000');
INSERT INTO `zeus_config_seccion` VALUES ('260', 'TRANSFORM_RAIDBOSS_PRICE', '57,855555;3470,2');
INSERT INTO `zeus_config_seccion` VALUES ('261', 'OPCIONES_CHAR_SEXO', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('262', 'OPCIONES_CHAR_NOBLE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('263', 'OPCIONES_CHAR_LVL85', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('264', 'OPCIONES_CHAR_BUFFER_AIO', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('265', 'OPCIONES_CHAR_BUFFER_AIO_PRICE', '3470,80000');
INSERT INTO `zeus_config_seccion` VALUES ('268', 'OPCIONES_CHAR_BUFFER_AIO_LVL', '70');
INSERT INTO `zeus_config_seccion` VALUES ('269', 'VOTO_HOPZONE_IDENTIFICACION', '');
INSERT INTO `zeus_config_seccion` VALUES ('270', 'VOTO_TOPZONE_IDENTIFICACION', '');
INSERT INTO `zeus_config_seccion` VALUES ('271', 'DESAFIO_MAX_LVL85', '8');
INSERT INTO `zeus_config_seccion` VALUES ('272', 'DESAFIO_MAX_NOBLE', '3');
INSERT INTO `zeus_config_seccion` VALUES ('273', 'DESAFIO_LVL85', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('274', 'DESAFIO_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('275', 'DESAFIO_NPC', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('276', 'OPCIONES_CHAR_FAME', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('277', 'OPCIONES_CHAR_FAME_PRICE', '57,800000');
INSERT INTO `zeus_config_seccion` VALUES ('278', 'OPCIONES_CHAR_FAME_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('279', 'OPCIONES_CHAR_FAME_LVL', '84');
INSERT INTO `zeus_config_seccion` VALUES ('280', 'OPCIONES_CHAR_FAME_GIVE', '250');
INSERT INTO `zeus_config_seccion` VALUES ('281', 'ACCESS_ID', '8,127');
INSERT INTO `zeus_config_seccion` VALUES ('282', 'OLY_ANTIFEED_CHANGE_TEMPLATE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('283', 'OLY_ANTIFEED_NO_SHOW_NAME_NPC', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('284', 'OLY_SECOND_SHOW_OPONENTES', '20,40,60');
INSERT INTO `zeus_config_seccion` VALUES ('285', 'OLY_SHOW_NAME_OPONENTES', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('286', 'OLY_ID_ACCESS_POINT_MODIF', '8,127');
INSERT INTO `zeus_config_seccion` VALUES ('287', 'TELEPORT_BD', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('288', 'SERVER_NAME', 'L2JMaster');
INSERT INTO `zeus_config_seccion` VALUES ('289', 'SHOP_USE_BD', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('290', 'ANTIBOT_COMANDO_STATUS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('291', 'ANTIBOT_OPORTUNIDADES', '3');
INSERT INTO `zeus_config_seccion` VALUES ('292', 'ANTIBOT_MINUTOS_JAIL', '1');
INSERT INTO `zeus_config_seccion` VALUES ('293', 'ANTIBOT_MOB_DEAD_TO_ACTIVATE', '90');
INSERT INTO `zeus_config_seccion` VALUES ('294', 'ANTIBOT_MINUTE_VERIF_AGAIN', '30');
INSERT INTO `zeus_config_seccion` VALUES ('295', 'ANTIBOT_MINUTOS_ESPERA', '2');
INSERT INTO `zeus_config_seccion` VALUES ('296', 'ANTIBOT_MIN_LVL', '0');
INSERT INTO `zeus_config_seccion` VALUES ('297', 'ANTIBOT_ONLY_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('298', 'ANTIBOT_ONLY_HERO', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('299', 'ANTIBOT_ANTIGUEDAD_MINUTOS_MIN', '1');
INSERT INTO `zeus_config_seccion` VALUES ('300', 'ANTIBOT_ONLY_GM', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('301', 'ANTIBOT_ANNOU_JAIL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('302', 'ANTIBOT_AUTO', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('303', 'ANTIBOT_RESET_COUNT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('304', 'BANIP_CHECK_IP_INTERNET', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('305', 'BANIP_CHECK_IP_RED', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('306', 'BANIP_STATUS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('307', 'BANIP_DISCONNECT_ALL_PLAYER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('308', 'EVENT_COLISEUM_NPC_ID', '957');
INSERT INTO `zeus_config_seccion` VALUES ('309', 'FLAG_FINDER_CAN_GO_CASTLE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('310', 'VOTE_SHOW_ONLY_ZEUS_ITEM', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('311', 'VOTE_SHOW_ONLY_ZEUS_ITEM_ID', '57');
INSERT INTO `zeus_config_seccion` VALUES ('312', 'VOTE_SHOW_ONLY_ZEUS_ITEM_GIVE_TEMPORAL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('313', 'VOTE_SHOW_ONLY_ZEUS_ITEM_ID_TEMPORTAL', '57');
INSERT INTO `zeus_config_seccion` VALUES ('314', 'VOTE_SHOW_ONLY_ZEUS_ITEM_TEMPORAL_PRICE', '3470,1');
INSERT INTO `zeus_config_seccion` VALUES ('315', 'ANTIBOT_BORRAR_ITEM', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('316', 'ANTIBOT_PORCENTAJE', '50');
INSERT INTO `zeus_config_seccion` VALUES ('317', 'ANTIBOT_ID_BORRAR', '57');
INSERT INTO `zeus_config_seccion` VALUES ('318', 'ANTIBOT_CHECK_INPEACE_ZONE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('319', 'ANNOUNCE_KARMA_PLAYER_WHEN_KILL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('320', 'ANNOUNCE_KARMA_PLAYER_WHEN_KILL_MSN', '%CHAR_NAME% es un Asesino, tiene %KARMA% de karma');
INSERT INTO `zeus_config_seccion` VALUES ('321', 'PARTY_FINDER_USE_NO_SUMMON_RULEZ', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('322', 'PARTY_FINDER_GO_LEADER_WHEN_ARE_INSTANCE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('323', 'VOTO_REWARD_AUTO_MINUTE_TIME_TO_CHECK', '30');
INSERT INTO `zeus_config_seccion` VALUES ('324', 'VOTO_REWARD_AUTO_RANGO_PREMIAR', '3');
INSERT INTO `zeus_config_seccion` VALUES ('325', 'VOTO_REWARD_AUTO_MENSAJE_FALTA', 'Nos Faltan %VOTENEED% votos en %SITE% para la meta de %VOTETOREWARD% votos.');
INSERT INTO `zeus_config_seccion` VALUES ('326', 'VOTO_REWARD_AUTO_MENSAJE_META_COMPLIDA', 'Felicidades, han alcanzado la meta en %SITE%');
INSERT INTO `zeus_config_seccion` VALUES ('327', 'VOTO_REWARD_AUTO_REWARD', '57,80000');
INSERT INTO `zeus_config_seccion` VALUES ('328', 'VOTE_AUTOREWARD', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('329', 'CASTLE_MANAGER_SHOW_GIRAN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('330', 'CASTLE_MANAGER_SHOW_ADEN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('331', 'CASTLE_MANAGER_SHOW_RUNE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('332', 'CASTLE_MANAGER_SHOW_OREN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('333', 'CASTLE_MANAGER_SHOW_DION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('334', 'CASTLE_MANAGER_SHOW_GLUDIO', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('335', 'CASTLE_MANAGER_SHOW_GODDARD', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('336', 'CASTLE_MANAGER_SHOW_SCHUTTGART', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('337', 'CASTLE_MANAGER_SHOW_INNADRIL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('338', 'DROP_SEARCH_SHOW_IDITEM_TO_PLAYER', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('339', 'SHOW_NEW_MAIN_WINDOWS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('340', 'FLAG_FINDER_PK_PRIORITY', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('341', 'AUGMENT_SPECIAL_PRICE_PAS', '3470,10;57,1');
INSERT INTO `zeus_config_seccion` VALUES ('342', 'AUGMENT_SPECIAL_PRICE_CHA', '3470,10;57,2');
INSERT INTO `zeus_config_seccion` VALUES ('343', 'AUGMENT_SPECIAL_PRICE_ACT', '3470,10;57,3');
INSERT INTO `zeus_config_seccion` VALUES ('344', 'DRESSME', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('345', 'DRESSME_CAN_USE_IN_OLYS', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('346', 'DRESSME_CAN_CHANGE_DRESS_IN_OLY', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('347', 'DRESSME_NEW_DRESS_IS_FREE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('348', 'DRESSME_NEW_DRESS_COST', '57,2;3470,88888888');
INSERT INTO `zeus_config_seccion` VALUES ('349', 'DRESSME_TARGET', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('350', 'DROP_SEARCH_ID_MOB_NO_TELEPORT', '59068,59069,59070,59071,59072,59073,59074,59075,59076,59077,59079,59080');
INSERT INTO `zeus_config_seccion` VALUES ('351', 'RAIDBOSS_ID_MOB_NO_TELEPORT', '59068,59069,59070,59072,59073,59074,59075,59076,59077,59079,59080,59071');
INSERT INTO `zeus_config_seccion` VALUES ('352', 'DROP_SEARCH_CAN_USE_TELEPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('353', 'RETURN_BUFF', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('354', 'RETURN_BUFF_MINUTES', '1');
INSERT INTO `zeus_config_seccion` VALUES ('355', 'RETURN_BUFF_IN_OLY', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('356', 'RETURN_BUFF_IN_OLY_MINUTES_TO_RETURN', '1');
INSERT INTO `zeus_config_seccion` VALUES ('357', 'RETURN_CANCEL_BUFF_NOT_STEALING', '');
INSERT INTO `zeus_config_seccion` VALUES ('358', 'TRADE_WHILE_FLAG', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('359', 'TRADE_WHILE_PK', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('360', 'TELEPORT_CAN_USE_IN_COMBAT_MODE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('361', 'BUFFCHAR_ACT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('362', 'BUFFCHAR_CAN_USE_FLAG', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('363', 'BUFFCHAR_CAN_USE_PK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('364', 'BUFFCHAR_CAN_USE_COMBAT_MODE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('365', 'BUFFCHAR_CAN_USE_SIEGE_ZONE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('366', 'BUFFCHAR_CAN_USE_INDIVIDUAL_BUFF', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('367', 'BUFFCHAR_FOR_FREE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('368', 'BUFFCHAR_HEAL_FOR_FREE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('369', 'BUFFCHAR_CANCEL_FOR_FREE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('370', 'BUFFCHAR_COST_USE', '57,22222');
INSERT INTO `zeus_config_seccion` VALUES ('371', 'BUFFCHAR_COST_HEAL', '57,55555');
INSERT INTO `zeus_config_seccion` VALUES ('372', 'BUFFCHAR_COST_CANCEL', '57,88888');
INSERT INTO `zeus_config_seccion` VALUES ('373', 'BUFFCHAR_COST_INDIVIDUAL', '57,99999');
INSERT INTO `zeus_config_seccion` VALUES ('374', 'BUFFCHAR_INDIVIDUAL_FOR_FREE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('375', 'BUFFCHAR_DONATION_SECCION', '');
INSERT INTO `zeus_config_seccion` VALUES ('376', 'BUFFCHAR_DONATION_SECCION_COST', '57,80000');
INSERT INTO `zeus_config_seccion` VALUES ('377', 'BUFFCHAR_DONATION_SECCION_REMOVE_ITEM', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('378', 'BUFFCHAR_DONATION_SECCION_ACT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('379', 'BUFFCHAR_PET', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('380', 'ANTIBOT_INACTIVE_MINUTES', '5');
INSERT INTO `zeus_config_seccion` VALUES ('381', 'OLY_ANTIFEED_SHOW_IN_NAME_CLASS', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('382', 'RADIO_PLAYER_NPC_MAXIMO', '100');
INSERT INTO `zeus_config_seccion` VALUES ('383', 'SHOW_ZEUS_ENTER_GAME', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('384', 'PVP_REWARD', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('385', 'PVP_PARTY_REWARD', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('386', 'PVP_REWARD_ITEMS', '57,1');
INSERT INTO `zeus_config_seccion` VALUES ('387', 'PVP_PARTY_REWARD_ITEMS', '57,2');
INSERT INTO `zeus_config_seccion` VALUES ('388', 'PREMIUM_CHAR', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('389', 'PREMIUM_CLAN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('390', 'PREMIUM_CHAR_EXP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('391', 'PREMIUM_CHAR_SP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('392', 'PREMIUM_CLAN_EXP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('393', 'PREMIUM_CLAN_SP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('394', 'PREMIUM_CHAR_DROP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('395', 'PREMIUM_CLAN_DROP_PORCEN', '20');
INSERT INTO `zeus_config_seccion` VALUES ('396', 'PREMIUM_DAYS_GIVE', '30');
INSERT INTO `zeus_config_seccion` VALUES ('397', 'PREMIUM_CHAR_COST_DONATION', '2');
INSERT INTO `zeus_config_seccion` VALUES ('398', 'PREMIUM_CLAN_COST_DONATION', '2');
INSERT INTO `zeus_config_seccion` VALUES ('399', 'DELEVEL_CHECK_SKILL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('400', 'MAX_IP_CHECK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('401', 'MAX_IP_COUNT', '3');
INSERT INTO `zeus_config_seccion` VALUES ('402', 'MAX_IP_RECORD_DATA', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('403', 'MAX_IP_VIP_COUNT', '5');
INSERT INTO `zeus_config_seccion` VALUES ('404', 'ANTIBOT_SEND_ALL_IP', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('405', 'ELEMENTAL_LVL_ENCHANT_MAX_WEAPON', '300');
INSERT INTO `zeus_config_seccion` VALUES ('406', 'ELEMENTAL_LVL_ENCHANT_MAX_ARMOR', '120');
INSERT INTO `zeus_config_seccion` VALUES ('407', 'CHAR_PANEL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('408', 'SHOW_FIXME_WINDOWS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('409', 'COMMUNITY_BOARD', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('410', 'COMMUNITY_BOARD_PART_EXEC', '_bbsgetfav');
INSERT INTO `zeus_config_seccion` VALUES ('411', 'COMMUNITY_BOARD_REGION', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('412', 'COMMUNITY_BOARD_REGION_PART_EXEC', '_bbsloc');
INSERT INTO `zeus_config_seccion` VALUES ('413', 'COMMUNITY_BOARD_ENGINE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('414', 'COMMUNITY_BOARD_ENGINE_PART_EXEC', '_bbslink');
INSERT INTO `zeus_config_seccion` VALUES ('415', 'PARTY_FINDER_GO_LEADER_ON_ASEDIO', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('416', 'COMMUNITY_BOARD_ROWS_FOR_PAGE', '18');
INSERT INTO `zeus_config_seccion` VALUES ('417', 'COMMUNITY_BOARD_TOPPLAYER_LIST', '20');
INSERT INTO `zeus_config_seccion` VALUES ('418', 'COMMUNITY_BOARD_CLAN_LIST', '10');
INSERT INTO `zeus_config_seccion` VALUES ('419', 'COMMUNITY_BOARD_MERCHANT_LIST', '5');
INSERT INTO `zeus_config_seccion` VALUES ('420', 'BTN_SHOW_VOTE_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('421', 'BTN_SHOW_BUFFER_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('422', 'BTN_SHOW_TELEPORT_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('423', 'BTN_SHOW_SHOP_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('424', 'BTN_SHOW_WAREHOUSE_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('425', 'BTN_SHOW_AUGMENT_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('426', 'BTN_SHOW_SUBCLASES_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('427', 'BTN_SHOW_CLASS_TRANSFER_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('428', 'BTN_SHOW_CONFIG_PANEL_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('429', 'BTN_SHOW_DROP_SEARCH_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('430', 'BTN_SHOW_PVPPK_LIST_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('431', 'BTN_SHOW_LOG_PELEAS_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('432', 'BTN_SHOW_CASTLE_MANAGER_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('433', 'BTN_SHOW_DESAFIO_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('434', 'BTN_SHOW_SYMBOL_MARKET_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('435', 'BTN_SHOW_CLANALLY_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('436', 'BTN_SHOW_PARTYFINDER_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('437', 'BTN_SHOW_FLAGFINDER_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('438', 'BTN_SHOW_COLORNAME_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('439', 'BTN_SHOW_DELEVEL_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('440', 'BTN_SHOW_REMOVE_ATRIBUTE_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('441', 'BTN_SHOW_BUG_REPORT_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('442', 'BTN_SHOW_DONATION_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('443', 'BTN_SHOW_CAMBIO_NOMBRE_PJ_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('444', 'BTN_SHOW_CAMBIO_NOMBRE_CLAN_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('445', 'BTN_SHOW_VARIAS_OPCIONES_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('446', 'BTN_SHOW_ELEMENT_ENHANCED_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('447', 'BTN_SHOW_ENCANTAMIENTO_ITEM_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('448', 'BTN_SHOW_AUGMENT_SPECIAL_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('449', 'BTN_SHOW_GRAND_BOSS_STATUS_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('450', 'BTN_SHOW_RAIDBOSS_INFO_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('451', 'BTN_SHOW_TRANSFORMACION_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('452', 'PREMIUM_CHAR_DROP_SPOIL', '1');
INSERT INTO `zeus_config_seccion` VALUES ('453', 'PREMIUM_CLAN_DROP_SPOIL', '1');
INSERT INTO `zeus_config_seccion` VALUES ('454', 'PREMIUM_CHAR_DROP_RAID', '1');
INSERT INTO `zeus_config_seccion` VALUES ('455', 'PREMIUM_CLAN_DROP_RAID', '1');
INSERT INTO `zeus_config_seccion` VALUES ('456', 'ANTI_OVER_ENCHANT_ACT', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('457', 'ANTI_OVER_ENCHANT_CHECK_BEFORE_ATTACK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('458', 'ANTI_OVER_ENCHANT_SECOND_BETWEEN_CHECK_BEFORE_ATTACK', '20');
INSERT INTO `zeus_config_seccion` VALUES ('459', 'ANTI_OVER_ENCHANT_MESJ_PUNISH', 'Tipo Castigo %TYPE_PUNISHMENT%. Nombre PJ %CHARNAME%');
INSERT INTO `zeus_config_seccion` VALUES ('460', 'ANTI_OVER_TYPE_PUNISH', 'JAIL');
INSERT INTO `zeus_config_seccion` VALUES ('461', 'ANTI_OVER_ENCHANT_ANNOUCEMENT_ALL', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('462', 'ANTI_OVER_ENCHANT_PUNISH_ALL_SAME_IP', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('463', 'ANTI_OVER_ENCHANT_MAX_WEAPON', '18');
INSERT INTO `zeus_config_seccion` VALUES ('464', 'ANTI_OVER_ENCHANT_MAX_ARMOR', '18');
INSERT INTO `zeus_config_seccion` VALUES ('465', 'REGISTER_EMAIL_ONLINE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('466', 'REGISTER_NEW_PLAYER_WAITING_TIME', '60');
INSERT INTO `zeus_config_seccion` VALUES ('467', 'REGISTER_NEW_PLAYER_RE_ENTER_WAITING_TIME', '10');
INSERT INTO `zeus_config_seccion` VALUES ('468', 'REGISTER_NEW_PlAYER_TRIES', '3');
INSERT INTO `zeus_config_seccion` VALUES ('469', 'COMMUNITY_BOARD_CLAN_PART_EXEC', '_bbsclan');
INSERT INTO `zeus_config_seccion` VALUES ('470', 'COMMUNITY_BOARD_CLAN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('471', 'COMMUNITY_BOARD_CLAN_ROWN_LIST', '6');
INSERT INTO `zeus_config_seccion` VALUES ('472', 'COMMUNITY_BOARD_PRIVATE_STORE_TELEPORT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('473', 'COMMUNITY_BOARD_PRIVATE_STORE_TELEPORT_SIEGE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('474', 'COMMUNITY_BOARD_PRIVATE_STORE_TELEPORT_INSIDE_CASTLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('475', 'COMMUNITY_BOARD_PRIVATE_STORE_TELEPORT_ONLY_PEACEZONE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('476', 'COMMUNITY_BOARD_PRIVATE_STORE_TELEPORT_INSIDE_CLANHALL_WITH_ANOTHER_CLAN', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('477', 'REGISTER_NEW_PLAYER_BLOCK_CHAT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('478', 'REGISTER_NEW_PLAYER_CHECK_BANNED_ACCOUNT', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('479', 'OPCIONES_CHAR_CAMBIO_NOMBRE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('480', 'OPCIONES_CLAN_CAMBIO_NOMBRE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('481', 'COMMUNITY_BOARD_REGION_PLAYER_ON_LIST', '50');
INSERT INTO `zeus_config_seccion` VALUES ('482', 'ANTIBOT_SECONDS_TO_RESEND_ANTIBOT', '40');
INSERT INTO `zeus_config_seccion` VALUES ('483', 'ANTIBOT_CHECK_DUALBOX', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('484', 'PREMIUM_CHAR_DROP_ITEM', '1');
INSERT INTO `zeus_config_seccion` VALUES ('485', 'PREMIUM_CLAN_DROP_ITEM', '1');
INSERT INTO `zeus_config_seccion` VALUES ('486', 'EVENT_RAIDBOSS_RAID_ID', '');
INSERT INTO `zeus_config_seccion` VALUES ('487', 'EVENT_RAIDBOSS_RAID_POSITION', '');
INSERT INTO `zeus_config_seccion` VALUES ('488', 'EVENT_RAIDBOSS_PLAYER_POSITION', '');
INSERT INTO `zeus_config_seccion` VALUES ('489', 'EVENT_RAIDBOSS_PLAYER_INMOBIL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('490', 'EVENT_RAIDBOSS_REWARD_WIN', '57,18000');
INSERT INTO `zeus_config_seccion` VALUES ('491', 'EVENT_RAIDBOSS_REWARD_LOOSER', '57,2');
INSERT INTO `zeus_config_seccion` VALUES ('492', 'EVENT_RAIDBOSS_PLAYER_MIN_LEVEL', '1');
INSERT INTO `zeus_config_seccion` VALUES ('493', 'EVENT_RAIDBOSS_PLAYER_MAX_LEVEL', '85');
INSERT INTO `zeus_config_seccion` VALUES ('494', 'EVENT_RAIDBOSS_PLAYER_MIN_REGIS', '1');
INSERT INTO `zeus_config_seccion` VALUES ('495', 'EVENT_RAIDBOSS_PLAYER_MAX_REGIS', '20');
INSERT INTO `zeus_config_seccion` VALUES ('496', 'EVENT_RAIDBOSS_SECOND_TO_BACK', '10');
INSERT INTO `zeus_config_seccion` VALUES ('497', 'EVENT_RAIDBOSS_JOINTIME', '10');
INSERT INTO `zeus_config_seccion` VALUES ('498', 'EVENT_RAIDBOSS_EVENT_TIME', '15');
INSERT INTO `zeus_config_seccion` VALUES ('499', 'EVENT_RAIDBOSS_COLORNAME', 'FF8000');
INSERT INTO `zeus_config_seccion` VALUES ('500', 'EVENT_RAIDBOSS_CHECK_DUALBOX', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('501', 'EVENT_RAIDBOSS_CANCEL_BUFF', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('502', 'EVENT_RAIDBOSS_UNSUMMON_PET', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('503', 'EVENT_RAIDBOSS_SECOND_TO_REVIVE', '10');
INSERT INTO `zeus_config_seccion` VALUES ('504', 'EVENT_RAIDBOSS_SECOND_TO_TELEPORT_RADIBOSS', '5');
INSERT INTO `zeus_config_seccion` VALUES ('505', 'ANTIBOT_SEND_JAIL_ALL_DUAL_BOX', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('506', 'ANTIFEED_ENCHANT_SKILL_REUSE', '3');
INSERT INTO `zeus_config_seccion` VALUES ('507', 'EVENT_RAIDBOSS_HOUR_TO_START', '18:00');
INSERT INTO `zeus_config_seccion` VALUES ('508', 'EVENT_RAIDBOSS_AUTOEVENT', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('509', 'CAN_USE_BSOE_PK', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('510', 'FLAG_FINDER_MIN_PVP_FROM_TARGET', '0');
INSERT INTO `zeus_config_seccion` VALUES ('511', 'PREMIUM_MESSAGE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('512', 'VOTO_REWARD_AUTO_REWARD_META_HOPZONE', '57,1');
INSERT INTO `zeus_config_seccion` VALUES ('513', 'VOTO_REWARD_AUTO_REWARD_META_TOPZONE', '57,2');
INSERT INTO `zeus_config_seccion` VALUES ('514', 'VOTO_REWARD_AUTO_AFK_CHECK', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('515', 'TRANSFORM_TIME', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('516', 'TRANSFORM_TIME_MINUTES', '10');
INSERT INTO `zeus_config_seccion` VALUES ('517', 'TRANSFORM_REUSE_TIME_MINUTES', '20');
INSERT INTO `zeus_config_seccion` VALUES ('518', 'COMMUNITY_REGION_LEGEND', '');
INSERT INTO `zeus_config_seccion` VALUES ('519', 'PVP_PK_PROTECTION_LIFETIME_MINUTES', '0');
INSERT INTO `zeus_config_seccion` VALUES ('520', 'PVP_REWARD_CHECK_DUALBOX', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('521', 'BUFFER_HEAL_CAN_FLAG', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('522', 'BUFFER_HEAL_CAN_IN_COMBAT', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('523', 'EVENT_RAIDBOSS_MINUTE_INTERVAL', '180');
INSERT INTO `zeus_config_seccion` VALUES ('524', 'EVENT_RAIDBOSS_MINUTE_INTERVAL_START_SERVER', '10');
INSERT INTO `zeus_config_seccion` VALUES ('525', 'BUFFCHAR_EDIT_SCHEME_ON_MAIN_WINDOWS_BUFFER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('526', 'ANTIBOT_BLACK_LIST', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('527', 'ANTIBOT_BLACK_LIST_MULTIPLIER', '10');
INSERT INTO `zeus_config_seccion` VALUES ('528', 'OLY_DUAL_BOX_CONTROL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('529', 'PVP_REWARD_RANGE', '800');
INSERT INTO `zeus_config_seccion` VALUES ('530', 'PARTY_FINDER_CAN_USE_ONLY_NOBLE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('531', 'TELEPORT_FOR_FREE_UP_TO_LEVEL', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('532', 'TELEPORT_FOR_FREE_UP_TO_LEVEL_LV', '40');
INSERT INTO `zeus_config_seccion` VALUES ('533', 'VOTE_REWARD_PERSONAL_VOTE_ALL_IP_PLAYER', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('534', 'EVENT_REPUTATION_CLAN', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('535', 'EVENT_REPUTATION_CLAN_ID_NPC', '958');
INSERT INTO `zeus_config_seccion` VALUES ('536', 'EVENT_REPUTATION_LVL_TO_GIVE', '5');
INSERT INTO `zeus_config_seccion` VALUES ('537', 'EVENT_REPUTATION_REPU_TO_GIVE', '50000');
INSERT INTO `zeus_config_seccion` VALUES ('538', 'EVENT_REPUTATION_MIN_PLAYER', '5');
INSERT INTO `zeus_config_seccion` VALUES ('539', 'EVENT_REPUTATION_NEED_ALL_MEMBER_ONLINE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('540', 'CHAT_SHOUT_BLOCK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('541', 'CHAT_TRADE_BLOCK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('542', 'CHAT_WISP_BLOCK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('543', 'CHAT_SHOUT_NEED_PVP', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('544', 'CHAT_SHOUT_NEED_LEVEL', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('545', 'CHAT_SHOUT_NEED_LIFETIME', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('546', 'CHAT_TRADE_NEED_PVP', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('547', 'CHAT_TRADE_NEED_LEVEL', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('548', 'CHAT_TRADE_NEED_LIFETIME', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('549', 'CHAT_WISP_NEED_PVP', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('550', 'CHAT_WISP_NEED_LEVEL', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('551', 'CHAT_WISP_NEED_LIFETIME', '-1');
INSERT INTO `zeus_config_seccion` VALUES ('552', 'FLAG_FINDER_CHECK_CLAN', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('553', 'EVENT_TOWN_WAR_AUTOEVENT', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('554', 'EVENT_TOWN_WAR_MINUTES_START_SERVER', '2');
INSERT INTO `zeus_config_seccion` VALUES ('555', 'EVENT_TOWN_WAR_MINUTES_INTERVAL', '120');
INSERT INTO `zeus_config_seccion` VALUES ('556', 'EVENT_TOWN_WAR_CITY_ON_WAR', 'ADEN');
INSERT INTO `zeus_config_seccion` VALUES ('557', 'EVENT_TOWN_WAR_MINUTES_EVENT', '10');
INSERT INTO `zeus_config_seccion` VALUES ('558', 'EVENT_TOWN_WAR_JOIN_TIME', '15');
INSERT INTO `zeus_config_seccion` VALUES ('559', 'EVENT_TOWN_WAR_GIVE_PVP_REWARD', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('560', 'EVENT_TOWN_WAR_GIVE_REWARD_TO_TOP_PPL_KILLER', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('561', 'EVENT_TOWN_WAR_REWARD_GENERAL', '');
INSERT INTO `zeus_config_seccion` VALUES ('562', 'EVENT_TOWN_WAR_REWARD_TOP_PLAYER', '');
INSERT INTO `zeus_config_seccion` VALUES ('563', 'EVENT_TOWN_WAR_RANDOM_CITY', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('564', 'EVENT_TOWN_WAR_DUAL_BOX_CHECK', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('565', 'EVENT_TOWN_WAR_HIDE_NPC', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('566', 'EVENT_TOWN_WAR_NPC_ID_HIDE', '');
INSERT INTO `zeus_config_seccion` VALUES ('567', 'EVENT_TOWN_WAR_CAN_USE_BUFFER', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('568', 'VOTE_EVERY_12_HOURS', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('569', 'COMMUNITY_BOARD_GRAND_RB', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('570', 'COMMUNITY_BOARD_GRAND_RB_EXEC', '_bbsfriends');
INSERT INTO `zeus_config_seccion` VALUES ('571', 'BTN_SHOW_BLACKSMITH_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('572', 'DONATION_LV_85_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('573', 'DONATION_NOBLE_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('574', 'DONATION_FAME_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('575', 'DONATION_FAME_AMOUNT', '200');
INSERT INTO `zeus_config_seccion` VALUES ('576', 'DONATION_CLAN_LV_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('577', 'DONATION_CLAN_LV_LV', '5');
INSERT INTO `zeus_config_seccion` VALUES ('578', 'DONATION_REDUCE_PK_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('579', 'DONATION_CHANGE_SEX_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('580', 'DONATION_AIO_CHAR_SIMPLE_COSTO', '0');
INSERT INTO `zeus_config_seccion` VALUES ('581', 'DONATION_AIO_CHAR_30_COSTO', '0');
INSERT INTO `zeus_config_seccion` VALUES ('582', 'DONATION_AIO_CHAR_LV_REQUEST', '72');
INSERT INTO `zeus_config_seccion` VALUES ('583', 'DONATION_CHANGE_CHAR_NAME_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('584', 'DONATION_CHANGE_CLAN_NAME_COST', '0');
INSERT INTO `zeus_config_seccion` VALUES ('585', 'OPCIONES_CHAR_BUFFER_AIO_30', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('586', 'OPCIONES_CHAR_BUFFER_AIO_PRICE_30', '3470,800');
INSERT INTO `zeus_config_seccion` VALUES ('587', 'BTN_SHOW_PARTYMATCHING_CBE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('588', 'COMMUNITY_BOARD_PARTYFINDER', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('589', 'COMMUNITY_BOARD_PARTYFINDER_EXEC', '_bbsfriends');
INSERT INTO `zeus_config_seccion` VALUES ('590', 'COMMUNITY_BOARD_PARTYFINDER_SEC_BETWEEN_MSJE', '40');
INSERT INTO `zeus_config_seccion` VALUES ('591', 'COMMUNITY_BOARD_PARTYFINDER_SEC_REQUEST', '15');
INSERT INTO `zeus_config_seccion` VALUES ('592', 'COMMUNITY_BOARD_PARTYFINDER_SEC_ON_BOARD', '360');
INSERT INTO `zeus_config_seccion` VALUES ('593', 'COMMUNITY_BOARD_PARTYFINDER_COMMAND_ENABLE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('594', 'BTN_SHOW_AUCTIONHOUSE_CBE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('595', 'RAIDBOSS_OBSERVE_MODE', 'true');
INSERT INTO `zeus_config_seccion` VALUES ('596', 'DROP_SEARCH_OBSERVE_MODE', 'false');
INSERT INTO `zeus_config_seccion` VALUES ('597', 'OLY_CAN_USE_SCHEME_BUFFER', 'false');

-- ----------------------------
-- Table structure for `zeus_connection`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_connection`;
CREATE TABLE `zeus_connection` (
  `ipWan` varchar(20) DEFAULT NULL,
  `ipLan` varchar(20) DEFAULT NULL,
  `account` varchar(80) DEFAULT NULL,
  `char` varchar(80) DEFAULT NULL,
  `status` enum('CONNECTED','DISCONNECTED') DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `charID` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_connection
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_dona_creditos`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_dona_creditos`;
CREATE TABLE `zeus_dona_creditos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cuenta` varchar(180) DEFAULT NULL,
  `creditos` int(11) DEFAULT '0',
  `entregados` enum('NO','SI') DEFAULT 'NO',
  `fechaDeposit` datetime DEFAULT NULL,
  `fechaEntregado` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_dona_creditos
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_dona_espera`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_dona_espera`;
CREATE TABLE `zeus_dona_espera` (
  `id` varchar(250) NOT NULL,
  `dona_monto` varchar(20) DEFAULT NULL,
  `dona_char` varchar(80) DEFAULT NULL,
  `dona_medio` varchar(80) DEFAULT NULL,
  `dona_email` varchar(250) DEFAULT NULL,
  `dona_obser` text,
  `dona_activa` enum('true','false') DEFAULT 'true',
  `dona_fecha` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_dona_espera
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_dona_shop`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_dona_shop`;
CREATE TABLE `zeus_dona_shop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(80) DEFAULT NULL,
  `tipoAccion` enum('NOBLE','HEROE','FAMA','CLAN_REPUTATION','LVL_85','SEC','MULTISELL','EXEC_MULTISELL','HTML','BUYLIST','CLAN_LVL') DEFAULT 'SEC',
  `param1` varchar(80) DEFAULT '',
  `param2` varchar(80) DEFAULT '',
  `DC_Count` int(11) DEFAULT '-1',
  `idSec` int(11) DEFAULT '-1',
  `posi` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_dona_shop
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_dressme`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_dressme`;
CREATE TABLE `zeus_dressme` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `idChar` int(11) DEFAULT NULL,
  `d1` varchar(300) DEFAULT '',
  `d2` varchar(300) DEFAULT '',
  `d3` varchar(300) DEFAULT '',
  `d4` varchar(300) DEFAULT '',
  `d5` varchar(300) DEFAULT '',
  `d6` varchar(300) DEFAULT '',
  `d7` varchar(300) DEFAULT '',
  `d8` varchar(300) DEFAULT '',
  `used` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dressme_idchar` (`idChar`) USING BTREE,
  KEY `dressme_id` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of zeus_dressme
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_evento`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_evento`;
CREATE TABLE `zeus_evento` (
  `idChar` int(11) DEFAULT NULL,
  `Accion` varchar(10) DEFAULT NULL,
  `fecha` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_evento
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_evento_in`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_evento_in`;
CREATE TABLE `zeus_evento_in` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idNPC` int(11) DEFAULT NULL,
  `idCHAR` bigint(11) DEFAULT NULL,
  `fechahora` datetime DEFAULT NULL,
  `idPremioDado` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_evento_in
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_evento_premios`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_evento_premios`;
CREATE TABLE `zeus_evento_premios` (
  `idPremio` smallint(6) DEFAULT NULL,
  `idItem` int(11) DEFAULT NULL,
  `Cantidad` int(6) DEFAULT NULL,
  `estado` enum('SI','NO') DEFAULT 'NO',
  `npcID` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_evento_premios
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_ipblock`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_ipblock`;
CREATE TABLE `zeus_ipblock` (
  `ipWAN` varchar(20) NOT NULL DEFAULT '',
  `ipRED` varchar(20) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_ipblock
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_log_fight`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_log_fight`;
CREATE TABLE `zeus_log_fight` (
  `Atacante` varchar(80) DEFAULT NULL,
  `idAtacante` int(11) DEFAULT NULL,
  `Asesinado` varchar(80) DEFAULT NULL,
  `idAsesinado` int(11) DEFAULT NULL,
  `tipPelea` enum('PK','PVP') DEFAULT NULL,
  `veces` int(11) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_log_fight
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_oly_sch`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_oly_sch`;
CREATE TABLE `zeus_oly_sch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `idChar` int(11) NOT NULL,
  `nombre` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Z_O_SCH_1` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_oly_sch
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_oly_sch_buff`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_oly_sch_buff`;
CREATE TABLE `zeus_oly_sch_buff` (
  `idsch` bigint(20) NOT NULL,
  `idbuff` smallint(6) DEFAULT NULL,
  KEY `Z_O_S_BUFF_1` (`idsch`) USING BTREE,
  KEY `Z_O_S_BUFF_2` (`idsch`,`idbuff`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_oly_sch_buff
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_premium`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_premium`;
CREATE TABLE `zeus_premium` (
  `id` varchar(50) DEFAULT NULL,
  `start_date` int(11) DEFAULT NULL,
  `end_date` int(11) DEFAULT NULL,
  `tip` enum('CHAR','CLAN') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_premium
-- ----------------------------

-- ----------------------------
-- Table structure for `zeus_shop`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_shop`;
CREATE TABLE `zeus_shop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(80) NOT NULL DEFAULT '',
  `descrip` varchar(80) DEFAULT '',
  `ima` varchar(300) DEFAULT '',
  `tip` enum('buylist','secc','multisell','exec_multisell','html') DEFAULT 'secc',
  `idarch` varchar(30) DEFAULT '0',
  `idsec` int(11) DEFAULT '-1',
  `pos` int(11) DEFAULT NULL,
  `idItemShow` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_shop
-- ----------------------------
INSERT INTO `zeus_shop` VALUES ('1', 'Armors', 'Venta de Armaduras', 'icon.armor_t95_u_i03', 'secc', '0', '-1', '1', '0');
INSERT INTO `zeus_shop` VALUES ('2', 'Weapons', 'Venta de Armas', 'icon.weapon_sacredumors_i00', 'secc', '0', '-1', '2', '0');
INSERT INTO `zeus_shop` VALUES ('3', 'Jewels', 'Venta de Joyas', 'icon.accessary_bluelycan_necklace_i00', 'secc', '0', '-1', '3', '0');

-- ----------------------------
-- Table structure for `zeus_teleport`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_teleport`;
CREATE TABLE `zeus_teleport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(80) NOT NULL,
  `descrip` varchar(80) DEFAULT '',
  `tip` enum('go','secc') DEFAULT 'secc',
  `x` int(11) DEFAULT '0',
  `y` int(11) DEFAULT '0',
  `z` int(11) DEFAULT '0',
  `idsec` int(11) DEFAULT '-1',
  `forNoble` enum('false','true') DEFAULT 'false',
  `cangoFlag` enum('true','false') DEFAULT 'true',
  `cangoKarma` enum('true','false') DEFAULT 'true',
  `lvlup` smallint(6) DEFAULT '1',
  `pos` int(11) DEFAULT NULL,
  `dualbox` enum('false','true') DEFAULT 'true',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_teleport
-- ----------------------------
INSERT INTO `zeus_teleport` VALUES ('1', 'Towns', 'Reinados', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('2', 'Town Areas', 'Pueblos y Ciudades', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('3', 'Seven Signs', 'Siete Signos', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('4', 'Arenas', 'Arenas para PvP', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('6', 'Hellbound', 'Area', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('7', 'Gracia Areas', 'Gracia', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '13', 'true');
INSERT INTO `zeus_teleport` VALUES ('8', 'Leveling Zone', 'Zonas de Leveo', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('9', 'Wedding Area', 'Wedding', 'go', '-51542', '-54170', '-2806', '-1', 'false', 'true', 'true', '1', '14', 'true');
INSERT INTO `zeus_teleport` VALUES ('10', 'Human', '', 'go', '-84318', '244579', '-3730', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('11', 'Elf', '', 'go', '46934', '51467', '-2977', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('12', 'Dark Elf', '', 'go', '9745', '15606', '-4574', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('13', 'Orc', '', 'go', '-44836', '-112524', '-235', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('14', 'Dwarf', '', 'go', '115113', '-178212', '-901', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('15', 'Kamael', '', 'go', '-117251', '46771', '360', '1', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('17', 'Giran', '', 'go', '82625', '148605', '-3468', '1', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('18', 'Aden', '', 'go', '147439', '26933', '-2204', '1', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('21', 'Goddard', '', 'go', '147736', '-56434', '-2780', '1', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('22', 'Rune', '', 'go', '45832', '-47995', '-796', '1', 'true', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('25', 'Dion', '', 'go', '16490', '143913', '-2935', '1', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('26', 'Gludio', '', 'go', '-14155', '122135', '-2988', '1', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('27', 'Gludin', '', 'go', '-80986', '150232', '-3043', '1', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('30', 'Schuttgart', '', 'go', '87358', '-141982', '-1336', '1', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('31', 'Heine', '', 'go', '105411', '218149', '-3488', '1', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('32', 'Hunters Village', '', 'go', '115210', '74454', '-2612', '1', 'false', 'true', 'true', '1', '12', 'true');
INSERT INTO `zeus_teleport` VALUES ('33', 'Floran', '', 'go', '16722', '169982', '-3497', '1', 'false', 'true', 'true', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('34', 'Oren', '', 'go', '81006', '53089', '-1559', '1', 'false', 'true', 'true', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('36', 'LVL 1-10', '', 'secc', '147545', '26647', '-2203', '8', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('37', 'LVL 11-20', '', 'secc', '147545', '26647', '-2203', '8', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('38', 'LVL 21-40', '', 'secc', '147545', '26647', '-2203', '8', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('39', 'LVL 41-60', '', 'secc', '147545', '26647', '-2203', '8', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('40', 'LVL 61-85', '', 'secc', '147545', '26647', '-2203', '8', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('41', 'Talking E T.', 'none', 'go', '-3553', '236990', '-3553', '36', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('42', 'Talking Northern T.', '', 'go', '-101728', '213557', '-3112', '36', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('43', 'Shadow Mother T.', '', 'go', '48823', '40146', '-3446', '36', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('44', 'Elven Forest', '', 'go', '25254', '41416', '-3653', '36', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('45', 'Elven Ruins', '', 'go', '-112869', '234938', '-3689', '36', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('46', 'Elven Fortress', '', 'go', '29046', '74919', '-3800', '36', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('47', 'Shilens Garden', '', 'go', '21618', '8929', '-3644', '36', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('48', 'The Dark Forest', '', 'go', '-14759', '22163', '-3662', '36', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('49', 'School of Dark Arts', '', 'go', '-47122', '59674', '-3326', '36', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('50', 'Valley Of Heroes', '', 'go', '-42896', '-106538', '-1414', '36', 'false', 'true', 'true', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('51', 'Immortal Plateau', '', 'go', '-13731', '-122018', '-2432', '36', 'false', 'true', 'true', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('52', 'Cave of Trials', '', 'go', '9227', '-112544', '-2534', '36', 'false', 'true', 'true', '1', '12', 'true');
INSERT INTO `zeus_teleport` VALUES ('53', 'Frozen Valley', '', 'go', '113192', '-174477', '-631', '36', 'false', 'true', 'true', '1', '13', 'true');
INSERT INTO `zeus_teleport` VALUES ('54', 'Western Mining Zone', '', 'go', '131909', '-209046', '-3606', '36', 'false', 'true', 'true', '1', '14', 'true');
INSERT INTO `zeus_teleport` VALUES ('55', 'Abandoned Coal Mines', '', 'go', '152377', '-179906', '854', '36', 'false', 'true', 'true', '1', '15', 'true');
INSERT INTO `zeus_teleport` VALUES ('56', 'Isle Of Souls', '', 'go', '-120653', '54175', '-1322', '36', 'false', 'true', 'true', '1', '16', 'true');
INSERT INTO `zeus_teleport` VALUES ('57', 'Ruins Of Despair', '', 'go', '-20122', '137438', '-3893', '37', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('58', 'Ruins Of Agony', '', 'go', '-56096', '106857', '-3744', '37', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('59', 'The Neutral Zone', '', 'go', '-10767', '75789', '-3609', '37', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('60', 'Windmill Hill', '', 'go', '-74921', '168620', '-3545', '37', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('61', 'Dion Hills', '', 'go', '27433', '139190', '-3093', '37', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('62', 'Plains Of Dions', '', 'go', '185', '172679', '-2952', '38', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('63', 'Execution Grounds', '', 'go', '51074', '141978', '-2859', '38', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('64', 'Cruma Marshlands', '', 'go', '5124', '126917', '-3654', '38', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('65', 'Wasteland', '', 'go', '-16497', '209395', '-3685', '38', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('66', 'Death Pass', '', 'go', '70060', '127265', '-3799', '38', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('67', 'Cruma Tower', '', 'go', '17190', '114171', '-3429', '38', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('68', 'Ivory Tower Crater', '', 'go', '79285', '13641', '-4550', '38', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('69', 'Enchanted Valley', '', 'go', '124920', '61990', '-3910', '39', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('70', 'TOI -Floor 1', '', 'go', '115122', '16003', '-5114', '39', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('71', 'The Giant\'s Cave', '', 'go', '174513', '52673', '-4359', '39', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('72', 'The Cementery', '', 'go', '172152', '20344', '-3316', '39', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('73', 'Dragon Valley Cave', '', 'go', '131357', '114464', '-3713', '39', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('74', 'Monastery Of Silence', '', 'go', '109103', '-80115', '-1375', '40', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('75', 'Ketra Orc Outpost', '', 'go', '130493', '-72310', '-3528', '40', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('76', 'Blazing Swamp', '', 'go', '145351', '-12973', '-4436', '40', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('77', 'Wall of Argos', '', 'go', '176895', '-50792', '-3384', '40', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('78', 'Imperial Tomb', '', 'go', '181077', '-82368', '-6590', '40', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('79', 'Forge Of The Gods', '', 'go', '173611', '-115473', '-3758', '40', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('80', 'Aden Areas', '', 'secc', '166244', '91514', '-3183', '2', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('81', 'Anghel Waterfall', '', 'go', '166244', '91514', '-3183', '80', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('82', 'The Blazing Swamp', '', 'go', '146828', '-12856', '-4455', '80', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('83', 'Border Outpost(East Side)', '', 'go', '109699', '-7908', '-2902', '80', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('84', 'Giran Areas', '', 'secc', '166244', '91514', '-3183', '2', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('85', 'Death Pass', '', 'go', '70000', '126636', '-3804', '84', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('87', 'Goddard Areas', '', 'secc', '147765', '-56009', '-2775', '2', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('88', 'Ketra Orc Outpost', '', 'go', '143885', '-68972', '-3757', '87', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('89', 'Rune', '', 'secc', '43866', '-49135', '-797', '2', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('91', 'Raid', '', 'secc', '-20155', '137426', '-3888', '38', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('93', 'Raid (20)', '', 'secc', '-53477', '84313', '-3545', '37', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('95', 'Madness Beast', '', 'go', '-53597', '84358', '-3545', '93', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('96', 'Zombie Lord Fekel', '', 'go', '22189', '79990', '-3162', '93', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('98', 'Princess Molrang 25', '', 'go', '-60653', '128059', '-2997', '91', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('99', 'Greyclaw Kutus 23', '', 'go', '-54771', '146201', '-2880', '91', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('101', 'Tracker Leader S. 23', '', 'go', '-56250', '186308', '-3194', '91', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('102', 'Kuroboros Priest 23', '', 'go', '-61941', '179714', '-3522', '91', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('103', 'Unrequited Kael 24', '', 'go', '-60032', '188262', '-4515', '91', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('104', 'Zombie Lord C. 25', '', 'go', '-12082', '138567', '-3596', '91', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('105', 'Ikuntai 25', '', 'go', '-21470', '152243', '-3036', '91', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('106', 'Soul Scavenger 25', '', 'go', '-45274', '111368', '-3807', '91', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('107', 'Betrayer of Urutu F. 25', '', 'go', '-18007', '-100787', '-2110', '91', 'false', 'true', 'true', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('108', 'Mammon Collector Talos 25', '', 'go', '172311', '-214407', '-3550', '91', 'false', 'true', 'true', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('109', 'Tiger Hornet 26', '', 'go', '28647', '179119', '-3613', '91', 'false', 'true', 'true', '1', '12', 'true');
INSERT INTO `zeus_teleport` VALUES ('111', 'Battered lands', '', 'go', '218', '235035', '-3267', '6', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('112', 'Hellbound', '', 'go', '-11737', '236236', '-3270', '6', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('113', 'Enchanted Megaliths', '', 'go', '-24285', '247610', '-3110', '6', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('114', 'Sand Swept Dunes', '', 'go', '-13810', '255911', '-3297', '6', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('116', 'Zonas Farm', 'Zonas Farm', 'secc', '0', '0', '0', '-1', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('118', 'Forest of the Dead', '', 'go', '52100', '-54280', '-3158', '89', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('120', 'Stakato Nest', '', 'go', '89841', '-44586', '-2136', '89', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('121', 'Beast Farm', '', 'go', '52074', '-80821', '-2814', '89', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('123', 'Swamp of Screams', '', 'go', '72520', '-49116', '-3200', '89', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('125', 'Valley of Saints', '', 'go', '74193', '-74493', '-3465', '89', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('126', 'Primaveral Isle', '', 'go', '10478', '-25029', '-3675', '89', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('127', 'Monastery of Silence', '', 'go', '108674', '-87817', '-2883', '89', 'false', 'true', 'true', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('128', 'Giran Harbor', '', 'go', '47399', '185579', '-3486', '84', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('129', 'Hardin Private Academy', '', 'go', '105922', '109684', '-3217', '84', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('130', 'Devil Isle', '', 'go', '42107', '206491', '-3757', '84', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('131', 'Dragon Valley', '', 'go', '75785', '117542', '-3775', '84', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('133', 'Antharas Lair', '', 'go', '130856', '114447', '-3729', '84', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('134', 'Ivory Tower', '', 'go', '85342', '17818', '-3516', '80', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('136', 'Coliseum', '', 'go', '146762', '46720', '-3425', '4', 'false', 'false', 'false', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('137', 'Forsaken Plains And Fortress', '', 'go', '176124', '39740', '-4124', '80', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('138', 'The Cementary', '', 'go', '185126', '20353', '-3174', '80', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('139', 'The Forest of Mirrors', '', 'go', '141973', '81183', '-3005', '80', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('141', 'Devastated Castle', '', 'go', '183555', '-15014', '-2771', '80', 'false', 'true', 'true', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('142', 'Ancient Battleground', '', 'go', '108310', '-2772', '-3439', '80', 'false', 'true', 'true', '1', '12', 'true');
INSERT INTO `zeus_teleport` VALUES ('143', 'Silent Valley', '', 'go', '172112', '55521', '-5922', '80', 'false', 'true', 'true', '1', '13', 'true');
INSERT INTO `zeus_teleport` VALUES ('144', 'Tower of Insolence', '', 'go', '114637', '13415', '-5100', '80', 'false', 'true', 'true', '1', '14', 'true');
INSERT INTO `zeus_teleport` VALUES ('145', 'The Giant Cave', '', 'go', '174474', '52808', '-4370', '80', 'false', 'true', 'true', '1', '15', 'true');
INSERT INTO `zeus_teleport` VALUES ('146', 'Varka Silenos', '', 'go', '125797', '-40908', '-3747', '87', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('147', 'Wall of Argos', '', 'go', '165044', '-47896', '-3555', '87', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('148', 'Forge of the Gods (FOG)', '', 'go', '170146', '-116253', '-2092', '87', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('149', 'Dion Areas', '', 'secc', '15651', '143012', '-2706', '2', 'false', 'false', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('150', 'Cruma Marshlands', '', 'go', '5066', '126961', '-3659', '149', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('152', 'Cruma Tower', '', 'secc', '17168', '114176', '-3440', '149', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('153', 'Cruma Tower 1', '', 'go', '17168', '114176', '-3440', '152', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('154', 'Cruma Tower 2', '', 'go', '17764', '108326', '-9062', '152', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('155', 'Cruma Tower 3', '', 'go', '17719', '115430', '-6482', '152', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('157', 'Plains of Dion', '', 'go', '582', '179173', '-3715', '149', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('159', 'Bee Hive', '', 'go', '34515', '188124', '-2971', '149', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('161', 'Tanor Canyon', '', 'go', '60103', '163609', '-2836', '149', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('162', 'Oren Areas', '', 'secc', '83029', '53173', '-1496', '2', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('164', 'Sel Mahum Training Ground (West Gate)', '', 'go', '76874', '63880', '-3643', '162', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('165', 'Sel Mahum Trining Ground (South Gate)', '', 'go', '79460', '71475', '-3443', '162', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('166', 'Sel Mahum Training Ground (Center)', '', 'go', '87412', '61478', '-3659', '162', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('167', 'Plains of the Lizardmen', '', 'go', '86692', '85821', '-2921', '162', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('168', 'Outlaw Forest', '', 'go', '91528', '-12221', '-2435', '162', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('169', 'Sea of Spores', '', 'go', '63450', '26302', '-3755', '162', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('170', 'Gludio Areas', '', 'secc', '-12766', '122800', '-3117', '2', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('172', 'Ruins of Agony', '', 'go', '-42686', '119517', '-3531', '170', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('173', 'Ruins of Despair', '', 'go', '-19882', '137974', '-3877', '170', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('174', 'Waestland', '', 'go', '-23403', '186599', '-4312', '170', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('175', 'The Ant Nest', '', 'go', '-26111', '173692', '-4147', '170', 'false', 'false', 'false', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('176', 'Windawood Manor', '', 'go', '-28332', '155104', '-3491', '170', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('178', 'Gludin Areas', '', 'secc', '-80723', '149728', '-3044', '2', 'false', 'true', 'true', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('179', 'Langk Lizardmen Dwellings', '', 'go', '-44764', '203544', '-3587', '178', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('180', 'Windmill Hill', '', 'go', '-74593', '167755', '-3579', '178', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('181', 'Forgotten Temple', '', 'go', '-52832', '190599', '-3489', '178', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('182', 'Abandoned Camp', '', 'go', '-50235', '146482', '-2789', '178', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('183', 'Red Rock Ridge', '', 'go', '-42291', '198339', '-2795', '178', 'false', 'true', 'true', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('184', 'Windy Hill', '', 'go', '-88235', '83916', '-3019', '178', 'false', 'true', 'true', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('185', 'Orc Barracks', '', 'go', '-89804', '105336', '-3571', '178', 'false', 'true', 'true', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('186', 'Schuttgart Areas', '', 'secc', '87051', '-143399', '-1293', '2', 'false', 'true', 'true', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('187', 'Den of Evil', '', 'go', '68660', '-110485', '-1899', '186', 'false', 'true', 'true', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('188', 'Planderous Plains', '', 'go', '111984', '-154190', '-1523', '186', 'false', 'true', 'true', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('189', 'Ice Merchant Cabin', '', 'go', '113830', '-109275', '-843', '186', 'false', 'true', 'true', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('190', 'Crypts of Disgrace', '', 'go', '46452', '-115939', '-3762', '186', 'false', 'true', 'true', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('191', 'Imperial Tomb', '', 'go', '188191', '-74959', '-2738', '87', 'false', 'false', 'false', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('192', 'Mon. Of Silence', '', 'go', '107944', '-87728', '-2917', '87', 'false', 'false', 'false', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('193', 'Catacomb of Heretics', '', 'go', '-53174', '-250275', '-7911', '3', 'false', 'false', 'false', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('194', 'Catacomb of Branded', '', 'go', '46542', '170305', '-4979', '3', 'false', 'false', 'false', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('195', 'Catacomb of Apostate', '', 'go', '-20195', '-250764', '-8163', '3', 'false', 'false', 'false', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('196', 'Catacomb of Witch', '', 'go', '140690', '79679', '-5429', '3', 'false', 'false', 'false', '1', '7', 'true');
INSERT INTO `zeus_teleport` VALUES ('197', 'Catacomb of Forbidden', '', 'go', '12521', '-248481', '-9585', '3', 'false', 'false', 'false', '1', '9', 'true');
INSERT INTO `zeus_teleport` VALUES ('198', 'Catacomb of Dark Omens', '', 'go', '-19176', '13504', '-4899', '3', 'false', 'false', 'false', '1', '11', 'true');
INSERT INTO `zeus_teleport` VALUES ('199', 'Necropolis of Sacrifice', '', 'go', '-41569', '210082', '-5085', '3', 'false', 'false', 'false', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('200', 'Necropolis of Pilgrims', '', 'go', '45249', '123548', '-5411', '3', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('201', 'Necropolis of Worshipers', '', 'go', '111552', '174014', '-5440', '3', 'false', 'false', 'false', '1', '6', 'true');
INSERT INTO `zeus_teleport` VALUES ('202', 'Necropolis of Patriots', '', 'go', '-21423', '77375', '-5171', '3', 'false', 'false', 'false', '1', '8', 'true');
INSERT INTO `zeus_teleport` VALUES ('203', 'Necropolis of Devotion', '', 'go', '-51942', '79096', '-4739', '3', 'false', 'false', 'false', '1', '10', 'true');
INSERT INTO `zeus_teleport` VALUES ('204', 'Necropolis of Martyrdom', '', 'go', '118576', '132800', '-4832', '3', 'false', 'false', 'false', '1', '12', 'true');
INSERT INTO `zeus_teleport` VALUES ('205', 'Necropolis of Saints', '', 'go', '83357', '209207', '-5437', '3', 'false', 'false', 'false', '1', '14', 'true');
INSERT INTO `zeus_teleport` VALUES ('206', 'Necropolis of Disciples', '', 'go', '172600', '-17599', '-4899', '3', 'false', 'false', 'false', '1', '16', 'true');
INSERT INTO `zeus_teleport` VALUES ('207', 'Gludin', '', 'go', '-87328', '142266', '-3635', '4', 'false', 'false', 'false', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('208', 'Giran', '', 'go', '73579', '142709', '-3763', '4', 'false', 'false', 'false', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('209', 'Dion', '', 'go', '11832', '183481', '-3563', '4', 'false', 'false', 'false', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('210', 'Goddard', '', 'go', '152180', '-126093', '-2282', '4', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('211', 'Aerial', '', 'go', '-213473', '244899', '2017', '4', 'false', 'false', 'false', '1', '5', 'true');
INSERT INTO `zeus_teleport` VALUES ('212', 'Airship Field', '', 'go', '-149365', '255309', '-86', '7', 'false', 'false', 'false', '1', '1', 'true');
INSERT INTO `zeus_teleport` VALUES ('213', 'Keucereus Base', '', 'go', '-186742', '244167', '2670', '7', 'false', 'false', 'false', '1', '2', 'true');
INSERT INTO `zeus_teleport` VALUES ('214', 'Seed of Annihilation', '', 'go', '-175520', '154505', '2712', '7', 'false', 'false', 'false', '1', '3', 'true');
INSERT INTO `zeus_teleport` VALUES ('215', 'Seed of Destruction', '', 'go', '-247012', '251804', '4340', '7', 'false', 'false', 'false', '1', '4', 'true');
INSERT INTO `zeus_teleport` VALUES ('216', 'Seed of Infinity', '', 'go', '-213678', '210670', '4408', '7', 'false', 'false', 'false', '1', '5', 'true');

-- ----------------------------
-- Table structure for `zeus_votos`
-- ----------------------------
DROP TABLE IF EXISTS `zeus_votos`;
CREATE TABLE `zeus_votos` (
  `idChar` int(18) NOT NULL,
  `voto` smallint(6) DEFAULT NULL,
  `fecha` int(18) DEFAULT NULL,
  `web` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of zeus_votos
-- ----------------------------
