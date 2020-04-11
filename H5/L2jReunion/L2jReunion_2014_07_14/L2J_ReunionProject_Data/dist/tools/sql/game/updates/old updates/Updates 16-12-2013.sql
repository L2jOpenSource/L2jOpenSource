ALTER TABLE `characters` ALTER `title_color` SET DEFAULT 0xECF9A2;
UPDATE `characters` SET `title_color` = 0xECF9A2 WHERE `title_color` = 0xFFFFFF;

UPDATE character_quests SET name='Q00116_BeyondTheHillsOfWinter' WHERE name='116_BeyondTheHillsOfWinter';
UPDATE character_quests SET name='Q00362_BardsMandolin' WHERE name='362_BardsMandolin';
UPDATE character_quests SET name='Q00363_SorrowfulSoundOfFlute' WHERE name='363_SorrowfulSoundofFlute';
UPDATE character_quests SET name='Q00364_JovialAccordion' WHERE name='364_JovialAccordion';