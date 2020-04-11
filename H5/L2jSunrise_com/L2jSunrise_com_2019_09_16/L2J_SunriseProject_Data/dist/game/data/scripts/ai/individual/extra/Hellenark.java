package ai.individual.extra;

import java.util.ArrayList;
import java.util.Iterator;

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

import ai.npc.AbstractNpcAI;

public class Hellenark extends AbstractNpcAI
{
	private static final int Hellenark = 22326;
	private static final int naia = 18484;
	private int status;
	public ArrayList<Object> spawnnaia;
	private static final int naialoc[][] =
	{
		{
			-24542,
			0x3c020,
			-3133,
			19078
		},
		{
			-23839,
			0x3c128,
			-3133,
			17772
		},
		{
			-23713,
			0x3ba86,
			-3133,
			53369
		},
		{
			-23224,
			0x3bb2c,
			-3133,
			57472
		},
		{
			-24709,
			0x3bdc2,
			-3133,
			63974
		},
		{
			-24394,
			0x3ba9b,
			-3133,
			5923
		}
	};
	
	public Hellenark()
	{
		super(Hellenark.class.getSimpleName(), "ai/individual/extra");
		status = 0;
		spawnnaia = new ArrayList<>();
		addAttackId(Hellenark);
		addTalkId(naia);
		addFirstTalkId(naia);
		addStartNpc(naia);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getId() == Hellenark)
		{
			if (status == 0)
			{
				startQuestTimer("spawn", 20000L, npc, null, false);
			}
			status = 1;
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (event.equalsIgnoreCase("spawn"))
		{
			if (status == 1)
			{
				status = 3;
			}
			startQuestTimer("check", 30000L, npc, null, false);
			for (int i = 0; i < 6; i++)
			{
				L2Npc mob = addSpawn(naia, naialoc[i][0], naialoc[i][1], naialoc[i][2], naialoc[i][3], false, 0L);
				spawnnaia.add(mob);
				mob.setIsInvul(true);
				mob.setIsImmobilized(true);
				mob.setIsOverloaded(true);
			}
			
			startQuestTimer("cast", 5000L, npc, null, false);
		}
		if (event.equalsIgnoreCase("check"))
		{
			if (status == 1)
			{
				startQuestTimer("check", 0x2bf20L, npc, null, false);
			}
			if (status == 3)
			{
				startQuestTimer("desp", 0x2bf20L, npc, null, false);
			}
			status = 3;
		}
		if (event.equalsIgnoreCase("desp"))
		{
			cancelQuestTimers("cast");
			L2Npc npc1;
			for (Iterator<Object> i$ = spawnnaia.iterator(); i$.hasNext(); npc1.deleteMe())
			{
				npc1 = (L2Npc) i$.next();
			}
			
			status = 0;
		}
		if (event.equalsIgnoreCase("cast"))
		{
			L2Npc npc1;
			for (Iterator<Object> i$ = spawnnaia.iterator(); i$.hasNext(); npc1.doCast(SkillData.getInstance().getInfo(5765, 1)))
			{
				npc1 = (L2Npc) i$.next();
				npc1.setTarget(player);
			}
			
			startQuestTimer("cast", 5000L, npc, null, false);
		}
		return htmltext;
	}
}
