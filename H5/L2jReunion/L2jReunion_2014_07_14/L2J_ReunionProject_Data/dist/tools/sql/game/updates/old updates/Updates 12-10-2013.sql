UPDATE npc SET type = "L2Monster" WHERE id = "18911";

UPDATE character_quests SET name='Q00108_JumbleTumbleDiamondFuss' WHERE name='108_JumbleTumbleDiamondFuss';
UPDATE character_quests SET name='Q00325_GrimCollector' WHERE name='325_GrimCollector';
UPDATE character_quests SET name='Q00329_CuriosityOfADwarf' WHERE name='329_CuriosityOfDwarf';


DELETE FROM spawnlist WHERE npc_templateid="22848";
DELETE FROM spawnlist WHERE npc_templateid="22849";
DELETE FROM spawnlist WHERE npc_templateid="22850";
DELETE FROM spawnlist WHERE npc_templateid="22851";
DELETE FROM spawnlist WHERE npc_templateid="22852";
DELETE FROM spawnlist WHERE npc_templateid="22853";
DELETE FROM spawnlist WHERE npc_templateid="22857";
DELETE FROM spawnlist WHERE npc_templateid="32758";

INSERT INTO `spawnlist` VALUES ('monastery_of_silence', '1', '32758', '110805', '-81851', '-1588', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('monastery_of_silence', '1', '32758', '114956', '-71152', '-548', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22831', '113097', '110311', '-3000', '0', '0', '30799', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22832', '113482', '115162', '-3200', '0', '0', '64293', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22832', '122135', '108852', '-2961', '0', '0', '36885', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22833', '123997', '108488', '-2986', '0', '0', '58062', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22833', '114890', '115234', '-3217', '0', '0', '59684', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22834', '109329', '115546', '-3112', '0', '0', '18198', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22834', '119960', '109355', '-2962', '0', '0', '26432', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22834', '111281', '110266', '-3043', '0', '0', '31060', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22834', '89602', '108262', '-3032', '0', '0', '3788', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22834', '94790', '107669', '-3050', '0', '0', '54609', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22859', '86101', '107610', '-3187', '0', '0', '43555', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22859', '87129', '112224', '-3264', '0', '0', '0', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22859', '85752', '119612', '-2992', '0', '0', '43083', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22820', '80733', '109807', '-3056', '0', '0', '30185', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22820', '83346', '112987', '-3048', '0', '0', '64221', '60', '0', '0', '0');
INSERT INTO `spawnlist` VALUES ('dragon_valley', '1', '22820', '86980', '120934', '-3040', '0', '0', '25947', '60', '0', '0', '0');