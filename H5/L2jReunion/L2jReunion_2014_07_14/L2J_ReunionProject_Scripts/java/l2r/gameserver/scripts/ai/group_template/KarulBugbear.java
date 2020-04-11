package l2r.gameserver.scripts.ai.group_template;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class KarulBugbear extends AbstractNpcAI
{
	private static final int KARUL_BUGBEAR = 20600;
	
	public KarulBugbear(int questId, String name, String descr)
	{
		super(name, descr);
		addAttackId(KARUL_BUGBEAR);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getId() == KARUL_BUGBEAR)
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 70)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUR_REAR_IS_PRACTICALLY_UNGUARDED));
			}
			else if (Rnd.get(100) < 10)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_WATCH_YOUR_BACK));
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String args[])
	{
		new KarulBugbear(-1, KarulBugbear.class.getSimpleName(), "ai");
	}
}
