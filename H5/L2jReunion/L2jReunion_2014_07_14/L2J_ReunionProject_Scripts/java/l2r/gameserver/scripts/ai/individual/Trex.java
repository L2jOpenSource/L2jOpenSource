package l2r.gameserver.scripts.ai.individual;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class Trex extends AbstractNpcAI
{
	private final int TREX[] =
	{
		22215,
		22216,
		22217
	};
	private final Map<Integer, int[]> SKILLS_HP = new HashMap<>();
	
	public Trex(int id, String name, String descr)
	{
		super(name, descr);
		SKILLS_HP.put(3626, new int[]
		{
			65,
			100
		});
		SKILLS_HP.put(3627, new int[]
		{
			25,
			65
		});
		SKILLS_HP.put(3628, new int[]
		{
			0,
			25
		});
		int arr$[] = TREX;
		int len$ = arr$.length;
		for (int i$ = 0; i$ < len$; i$++)
		{
			int npcId = arr$[i$];
			addSkillSeeId(npcId);
		}
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object targets[], boolean isPet)
	{
		boolean b = false;
		L2Object arr$[] = targets;
		int len$ = arr$.length;
		for (int i$ = 0; i$ < len$; i$++)
		{
			L2Object trg = arr$[i$];
			if (trg == npc)
				b = true;
		}
		
		if (!b)
			return super.onSkillSee(npc, player, skill, targets, isPet);
		int skillId = skill.getId();
		int trexHp = (int) npc.getCurrentHp();
		int trexMaxHp = npc.getMaxHp();
		if (skillId >= 3626 && skillId <= 3628)
		{
			int minHp = (SKILLS_HP.get(skillId)[0] * trexMaxHp) / 100;
			int maxHp = (SKILLS_HP.get(skillId)[1] * trexMaxHp) / 100;
			if (trexHp < minHp || trexHp > maxHp)
			{
				npc.stopSkillEffects(skillId);
				player.sendMessage("The conditions are not right to use this skill now.");
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isPet);
	}
	
	public static void main(String args[])
	{
		new Trex(-1, Trex.class.getSimpleName(), "ai");
	}
}
