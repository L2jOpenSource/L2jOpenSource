package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class CabalBuffers extends L2AttackableAIScript
{
	private static final NpcStringId[] PREACHER_OF_DOOM_RANDOM_CHAT =
	{
		NpcStringId.ID_1000303,
		NpcStringId.ID_1000415,
		NpcStringId.ID_1000416,
		NpcStringId.ID_1000417
	};
	
	private static final NpcStringId[] PREACHER_OF_DOOM_CAST_CHAT =
	{
		NpcStringId.ID_1000420,
		NpcStringId.ID_1000304,
		NpcStringId.ID_1000418,
		NpcStringId.ID_1000419
	};
	
	private static final int[] PREACHER_OF_DOOM_IDS =
	{
		31093,
		31172,
		31174,
		31176,
		31178,
		31180,
		31182,
		31184,
		31186,
		31188,
		31190,
		31192,
		31194,
		31196,
		31198,
		31200,
		31231,
		31232,
		31233,
		31234,
		31235,
		31236,
		31237,
		31238,
		31239,
		31240,
		31241,
		31242,
		31243,
		31244,
		31245,
		31246,
		31713,
		31714,
		31715,
		31716,
		31717,
		31718,
		31719,
		31720,
		31999,
		32000,
		32001,
		32002
	};
	
	private static final NpcStringId[] ORATOR_OF_REVELATIONS_RANDOM_CHAT =
	{
		NpcStringId.ID_1000305,
		NpcStringId.ID_1000421,
		NpcStringId.ID_1000422,
		NpcStringId.ID_1000423
	};
	
	private static final NpcStringId[] ORATOR_OF_REVELATIONS_CAST_CHAT =
	{
		NpcStringId.ID_1000306,
		NpcStringId.ID_1000424,
		NpcStringId.ID_1000426,
		NpcStringId.ID_1000425
	};
	
	private static final int[] ORATOR_OF_REVELATIONS_IDS =
	{
		31094,
		31173,
		31175,
		31177,
		31179,
		31181,
		31183,
		31185,
		31187,
		31189,
		31191,
		31193,
		31195,
		31197,
		31199,
		31201,
		31247,
		31248,
		31249,
		31250,
		31251,
		31252,
		31253,
		31254,
		31721,
		31722,
		31723,
		31724,
		31725,
		31726,
		31727,
		31728,
		32003,
		32004,
		32005,
		32006
	};
	
	public CabalBuffers()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(PREACHER_OF_DOOM_IDS, ScriptEventType.ON_CREATURE_SEE, ScriptEventType.ON_DECAY, ScriptEventType.ON_SPAWN);
		addEventIds(ORATOR_OF_REVELATIONS_IDS, ScriptEventType.ON_CREATURE_SEE, ScriptEventType.ON_DECAY, ScriptEventType.ON_SPAWN);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature instanceof Player)
		{
			final Player player = creature.getActingPlayer();
			
			// Don't bother about invisible GMs.
			if (player.isGM() && !player.isVisible())
				return super.onCreatureSee(npc, creature);
			
			// Don't bother about Players who didn't participated.
			final CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
			if (playerCabal == CabalType.NORMAL)
				return super.onCreatureSee(npc, creature);
			
			final int i0 = Rnd.get(100);
			final int i1 = Rnd.get(10000);
			
			if (isPreacher(npc))
			{
				if (playerCabal == SevenSignsManager.getInstance().getLosingCabal())
				{
					final int skillId = (!player.isMageClass()) ? 4361 : 4362;
					
					if (player.getFirstEffect(skillId) == null)
					{
						if (i1 < 1)
							npc.broadcastNpcSay((!player.isMageClass()) ? PREACHER_OF_DOOM_CAST_CHAT[0] : PREACHER_OF_DOOM_CAST_CHAT[2], player.getName());
						
						npc.setTarget(player);
						npc.getCast().doCast(skillId, 1);
					}
					else if (i0 < 5)
					{
						if (i1 < 500)
							npc.broadcastNpcSay((!player.isMageClass()) ? PREACHER_OF_DOOM_CAST_CHAT[1] : PREACHER_OF_DOOM_CAST_CHAT[3], player.getName());
						
						npc.setTarget(player);
						npc.getCast().doCast(skillId, 2);
					}
				}
			}
			else
			{
				if (playerCabal == SevenSignsManager.getInstance().getWinningCabal())
				{
					final int skillId = (!player.isMageClass()) ? 4364 : 4365;
					
					if (player.getFirstEffect(skillId) == null)
					{
						if (i1 < 1)
							npc.broadcastNpcSay((!player.isMageClass()) ? ORATOR_OF_REVELATIONS_CAST_CHAT[0] : ORATOR_OF_REVELATIONS_CAST_CHAT[2], player.getName());
						
						npc.setTarget(player);
						npc.getCast().doCast(skillId, 1);
					}
					else if (i0 < 5)
					{
						if (i1 < 500)
							npc.broadcastNpcSay((!player.isMageClass()) ? ORATOR_OF_REVELATIONS_CAST_CHAT[1] : ORATOR_OF_REVELATIONS_CAST_CHAT[3], player.getName());
						
						npc.setTarget(player);
						npc.getCast().doCast(skillId, 2);
					}
				}
			}
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onDecay(Npc npc)
	{
		cancelQuestTimers("5097", npc);
		return super.onDecay(npc);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("5097"))
		{
			npc.broadcastNpcSay(Rnd.get((isPreacher(npc)) ? PREACHER_OF_DOOM_RANDOM_CHAT : ORATOR_OF_REVELATIONS_RANDOM_CHAT));
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimerAtFixedRate("5097", npc, null, 60000, 60000);
		return super.onSpawn(npc);
	}
	
	/**
	 * @param npc : The {@link Npc} to test.
	 * @return True if the {@link Npc} set as parameter is a Preacher of Doom. If false, it can only be an Orator of Revelations.
	 */
	private static boolean isPreacher(Npc npc)
	{
		return ArraysUtil.contains(PREACHER_OF_DOOM_IDS, npc.getNpcId());
	}
}