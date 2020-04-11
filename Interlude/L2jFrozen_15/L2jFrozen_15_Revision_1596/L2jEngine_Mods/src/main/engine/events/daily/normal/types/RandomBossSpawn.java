package main.engine.events.daily.normal.types;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.util.random.Rnd;

import main.data.properties.ConfigData;
import main.engine.events.daily.AbstractEvent;
import main.enums.TeamType;
import main.holders.LocationHolder;
import main.holders.RewardHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.UtilSpawn;

/**
 * @author fissban
 */
public class RandomBossSpawn extends AbstractEvent
{
	private static final String[] LOCATIONS =
	{
		"in the colliseum",
		"near the entrance of the Garden of Eva",
		"close to the western entrance of the Cemetary",
		"at Gludin's Harbor"
	};
	
	private static final LocationHolder[] SPAWNS =
	{
		new LocationHolder(150086, 46733, -3407),
		new LocationHolder(84805, 233832, -3669),
		new LocationHolder(161385, 21032, -3671),
		new LocationHolder(89199, 149962, -3581),
	};
	
	private static NpcHolder raid = null;
	
	/**
	 * Constructor
	 */
	public RandomBossSpawn()
	{
		registerMod(ConfigData.ENABLE_RandomBossSpawn);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				// Start random raid spawn
				startTimer("spawnRaids", ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60, null, null, true);
				break;
			case END:
				// Cancel random raid spawn
				cancelTimers("spawnRaids");
				break;
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder player)
	{
		switch (timerName)
		{
			case "spawnRaids":
				int rndLocation = Rnd.get(LOCATIONS.length - 1);
				int rndBoss = Rnd.get(ConfigData.RANDOM_BOSS_NPC_ID.size());
				// Spawn raid
				LocationHolder loc = SPAWNS[rndLocation];
				raid = UtilSpawn.npc(ConfigData.RANDOM_BOSS_NPC_ID.get(rndBoss), loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60, TeamType.NONE, 0);
				// Announcement spawn
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Raid " + raid.getInstance().getName() + " spawn " + LOCATIONS[rndLocation]);
				// Announcement lef time
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Have " + ConfigData.RANDOM_BOSS_SPWNNED_TIME + " minutes to kill");
				break;
		}
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		// Check if kill raid event
		if (victim == raid)
		{
			// Give reward and send message
			for (RewardHolder reward : ConfigData.RANDOM_BOSS_REWARDS)
			{
				if (Rnd.get(100) <= reward.getRewardChance())
				{
					killer.getActingPlayer().getInstance().sendMessage("Have won " + reward.getRewardCount() + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
					UtilInventory.giveItems(killer.getActingPlayer(), reward.getRewardId(), reward.getRewardCount(), 0);
				}
			}
		}
	}
}
