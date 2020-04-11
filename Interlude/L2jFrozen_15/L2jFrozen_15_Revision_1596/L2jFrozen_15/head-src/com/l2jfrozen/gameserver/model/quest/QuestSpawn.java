package com.l2jfrozen.gameserver.model.quest;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author programmos
 */
public final class QuestSpawn
{
	private final Logger LOGGER = Quest.LOGGER;
	private static QuestSpawn instance;
	
	public static QuestSpawn getInstance()
	{
		if (instance == null)
		{
			instance = new QuestSpawn();
		}
		
		return instance;
	}
	
	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2NpcInstance npc = null;
		
		public DeSpawnScheduleTimerTask(final L2NpcInstance npc)
		{
			this.npc = npc;
		}
		
		@Override
		public void run()
		{
			npc.onDecay();
		}
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Uses player's coords and heading. Adds a little randomization in the x y coords Return object id of newly spawned npc
	 * @param  npcId
	 * @param  cha
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, final L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0);
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param  npcId
	 * @param  x
	 * @param  y
	 * @param  z
	 * @param  heading
	 * @param  randomOffset
	 * @param  despawnDelay
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, int x, int y, final int z, final int heading, final boolean randomOffset, final int despawnDelay)
	{
		L2NpcInstance result = null;
		
		try
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			
			if (template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0! Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc! This will NOT work
				// with quest spawns! For both of the above cases, we need a fail-safe spawn. For this, we use the
				// default spawn location, which is at the player's loc.
				if (x == 0 && y == 0)
				{
					LOGGER.error("Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				
				if (randomOffset)
				{
					int offset;
					
					// Get the direction of the offset
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					
					// make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;
					
					// Get the direction of the offset
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					
					// make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);
				spawn.stopRespawn();
				result = spawn.spawnOne();
				spawn = null;
				
				if (despawnDelay > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
				}
				
				return result;
			}
		}
		catch (final Exception e1)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e1.printStackTrace();
			}
			
			LOGGER.warn("Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
}
