package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class Golkonda extends AbstractNpcAI
{
	private static final int GOLKONDA = 25126;
	private static final int z1 = 6900;
	private static final int z2 = 7500;
	
	public Golkonda(int questId, String name, String descr)
	{
		super(name, descr);
		int mobs[] =
		{
			GOLKONDA
		};
		registerMobs(mobs);
	}
	
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == GOLKONDA)
		{
			int z = npc.getZ();
			if (z > z2 || z < z1)
			{
				npc.teleToLocation(0x1c659, 15896, 6999);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String args[])
	{
		new Golkonda(-1, Golkonda.class.getSimpleName(), "ai");
	}
}
