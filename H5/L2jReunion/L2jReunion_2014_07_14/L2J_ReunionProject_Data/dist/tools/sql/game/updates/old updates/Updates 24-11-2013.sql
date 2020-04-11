UPDATE character_quests SET name='Q00327_RecoverTheFarmland' WHERE name='327_ReclaimTheLand';

DELETE FROM spawnlist WHERE npc_templateid BETWEEN "20494" AND "20501";
DELETE FROM spawnlist WHERE npc_templateid="20546";
