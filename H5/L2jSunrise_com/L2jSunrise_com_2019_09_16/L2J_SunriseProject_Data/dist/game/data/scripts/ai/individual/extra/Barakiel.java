package ai.individual.extra;

import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class Barakiel extends AbstractNpcAI
{
	private static final int BARAKIEL = 25325;
	private static final int x1 = 0x15ec8;
	private static final int x2 = 0x16c10;
	private static final int y1 = 0xfffeac02;
	
	public Barakiel()
	{
		super(Barakiel.class.getSimpleName(), "ai/individual/extra");
		registerMobs(BARAKIEL);
	}
	
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			if ((x < x1) || (x > x2) || (y < y1))
			{
				npc.teleToLocation(0x16380, 0xfffeb070, -2736);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
