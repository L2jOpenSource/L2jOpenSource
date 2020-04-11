package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class Lematan extends AbstractNpcAI
{
	private static final int LEMATAN = 18633;
	private static final int MINION = 18634;
	public int status;
	
	public Lematan(int id, String name, String descr)
	{
		super(name, descr);
		status = 0;
		int mob[] =
		{
			LEMATAN,
			MINION
		};
		registerMobs(mob);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc != null)
		{
			if (event.equalsIgnoreCase("first_anim"))
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 5756, 1, 2500, 0));
			else if (event.equalsIgnoreCase("lematanMinions"))
			{
				for (int i = 0; i < 6; i++)
				{
					int radius = 260;
					int x = (int) (radius * Math.cos(i * 0.91800000000000004D));
					int y = (int) (radius * Math.sin(i * 0.91800000000000004D));
					L2Npc mob = addSpawn(MINION, 0x14bf6 + x, 0xfffcd0ce + y, -3337, 0, false, 0L);
					mob.setInstanceId(npc.getInstanceId());
				}
			}
			else if (event.equalsIgnoreCase("lematanMinions1"))
			{
				if (player.getInstanceId() != 0)
				{
					L2Npc mob = addSpawn(MINION, player.getX() + 50, player.getY() - 50, player.getZ(), 0, false, 0L);
					mob.setInstanceId(npc.getInstanceId());
				}
				else
				{
					L2Npc mob = addSpawn(MINION, 0x14bf6, 0xfffcd0ce, -3337, 0, false, 0L);
					mob.setInstanceId(npc.getInstanceId());
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getId() == LEMATAN)
		{
			int maxHp = npc.getMaxHp();
			double nowHp = npc.getStatus().getCurrentHp();
			if (nowHp < maxHp * 0.5D && status == 0)
			{
				status = 1;
				attacker.teleToLocation(0x14a5a, 0xfffcd239, -3337);
				L2Summon pet = attacker.getSummon();
				if (pet != null)
					pet.teleToLocation(0x14a5a, 0xfffcd239, -3337, true);
				npc.teleToLocation(0x14bf6, 0xfffcd0ce, -3337);
				startQuestTimer("lematanMinions", 3000L, npc, null);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == LEMATAN)
			status = 0;
		else if (npcId == MINION && status == 1)
			startQuestTimer("lematanMinions1", 10000L, npc, killer);
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		int npcId = npc.getId();
		if (npcId == MINION)
			startQuestTimer("first_anim", 1000L, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String args[])
	{
		new Lematan(-1, Lematan.class.getSimpleName(), "ai");
	}
}
