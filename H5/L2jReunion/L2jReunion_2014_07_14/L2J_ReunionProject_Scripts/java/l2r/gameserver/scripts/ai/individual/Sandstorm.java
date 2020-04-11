package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class Sandstorm extends AbstractNpcAI
{
	private static final int Sandstorm = 32350;
	
	public Sandstorm(int questId, String name, String descr)
	{
		super(name, descr);
		super.addAttackId(Sandstorm);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == Sandstorm)
		{
			npc.setTarget(player);
			npc.doCast(SkillData.getInstance().getInfo(5435, 1));
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(String args[])
	{
		new Sandstorm(-1, Sandstorm.class.getSimpleName(), "ai");
	}
}
