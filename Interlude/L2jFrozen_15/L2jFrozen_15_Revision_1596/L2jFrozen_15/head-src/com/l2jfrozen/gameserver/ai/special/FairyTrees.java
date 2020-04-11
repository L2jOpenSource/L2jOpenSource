package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.util.random.Rnd;

public class FairyTrees extends Quest implements Runnable
{
	private static final int[] trees =
	{
		27185,
		27186,
		27187,
		27188
	};
	
	public FairyTrees(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		for (final int mob : trees)
		{
			addEventId(mob, QuestEventType.ON_KILL);
		}
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getNpcId();
		for (final int treeId : trees)
		{
			if (npcId == treeId)
			{
				for (int i = 0; i < 20; i++)
				{
					final L2Attackable newNpc = (L2Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
					final L2Character originalKiller = isPet ? killer.getPet() : killer;
					newNpc.setRunning();
					newNpc.addDamageHate(originalKiller, 0, 999);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
					if (Rnd.nextBoolean())
					{
						if (originalKiller != null)
						{
							final L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
							if (skill != null)
							{
								skill.getEffects(newNpc, originalKiller, false, false, false);
							}
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
		
	}
}
