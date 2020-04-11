package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class Kernon extends AbstractNpcAI
{
	private static final int KERNON = 25054;
	private static final int z1 = 3900;
	private static final int z2 = 4300;
	
	public Kernon(int questId, String name, String descr)
	{
		super(name, descr);
		int mobs[] =
		{
			KERNON
		};
		registerMobs(mobs);
	}
	
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == KERNON)
		{
			int z = npc.getZ();
			if (z > z2 || z < z1)
			{
				npc.teleToLocation(0x1bb0c, 16424, 3969);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String args[])
	{
		new Kernon(-1, Kernon.class.getSimpleName(), "ai");
	}
}
