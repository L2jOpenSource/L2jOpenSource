UPDATE character_quests SET name='Q00235_MimirsElixir' WHERE name='235_MimirsElixir';
UPDATE character_quests SET name='Q00648_AnIceMerchantsDream' WHERE name='648_AnIceMerchantsDream';
INSERT IGNORE INTO global_variables (`var`, `value`) VALUES ('nextTWStartDate', (SELECT `value` FROM quest_global_data  WHERE var = "nextTWStartDate"));