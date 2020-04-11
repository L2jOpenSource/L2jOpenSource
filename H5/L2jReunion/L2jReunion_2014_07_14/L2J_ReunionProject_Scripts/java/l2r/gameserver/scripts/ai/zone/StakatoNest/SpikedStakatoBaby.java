package l2r.gameserver.scripts.ai.zone.StakatoNest;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class SpikedStakatoBaby extends AbstractNpcAI
{
	private static final int SPIKED_STAKATO_NURSE = 22630;
	private static final int SPIKED_STAKATO_CAPTAIN = 22629;
	
	public SpikedStakatoBaby(int questId, String name, String descr)
	{
		super(name, descr);
		
		addKillId(SPIKED_STAKATO_NURSE);
		addKillId(SPIKED_STAKATO_CAPTAIN);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2Npc baby = getBaby(npc);
		if ((baby != null) && (!baby.isDead()))
		{
			for (int i = 0; i < 3; i++)
			{
				L2Attackable captain = (L2Attackable) addSpawn(SPIKED_STAKATO_CAPTAIN, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 240000, true);
				captain.setRunning();
				captain.addDamageHate(killer, 1, 99999);
				captain.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public L2Npc getBaby(L2Npc couple)
	{
		if (((L2MonsterInstance) couple).getMinionList().getSpawnedMinions().size() > 0)
		{
			return ((L2MonsterInstance) couple).getMinionList().getSpawnedMinions().get(0);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new SpikedStakatoBaby(-1, "SpikedStakatoBaby", "ai");
	}
}