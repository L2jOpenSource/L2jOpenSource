package ai.individual.extra.ToiRaids;

import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class Golkonda extends AbstractNpcAI
{
	private static final int GOLKONDA = 25126;
	private static final int z1 = 6900;
	private static final int z2 = 7500;
	
	public Golkonda()
	{
		super(Golkonda.class.getSimpleName(), "ai");
		addAttackId(GOLKONDA);
	}
	
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == GOLKONDA)
		{
			int z = npc.getZ();
			if ((z > z2) || (z < z1))
			{
				npc.teleToLocation(0x1c659, 15896, 6999);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
