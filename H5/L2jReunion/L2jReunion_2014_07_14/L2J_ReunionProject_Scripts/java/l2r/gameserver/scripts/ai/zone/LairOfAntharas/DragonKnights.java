package l2r.gameserver.scripts.ai.zone.LairOfAntharas;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class DragonKnights extends AbstractNpcAI
{
	private static final int DRAGON_KNIGHT_1 = 22844;
	private static final int DRAGON_KNIGHT_2 = 22845;
	private static final int ELITE_DRAGON_KNIGHT = 22846;
	private static final int DRAGON_KNIGHT_WARRIOR = 22847;
	
	public DragonKnights(String name, String descr)
	{
		super(name, descr);
		
		addKillId(DRAGON_KNIGHT_1);
		addKillId(DRAGON_KNIGHT_2);
		addKillId(ELITE_DRAGON_KNIGHT);
		addKillId(DRAGON_KNIGHT_WARRIOR);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == DRAGON_KNIGHT_1)
		{
			if (getRandom(1000) < 400)
			{
				final L2Npc warrior = addSpawn(DRAGON_KNIGHT_2, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warrior.broadcastPacket(new NpcSay(warrior.getObjectId(), 0, warrior.getId(), NpcStringId.THOSE_WHO_SET_FOOT_IN_THIS_PLACE_SHALL_NOT_LEAVE_ALIVE));
				warrior.setRunning();
				((L2Attackable) warrior).addDamageHate(killer, 1, 99999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
				
				final L2Npc warrior1 = addSpawn(DRAGON_KNIGHT_2, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warrior1.setRunning();
				((L2Attackable) warrior1).addDamageHate(killer, 1, 99999);
				warrior1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		
		if (npc.getId() == DRAGON_KNIGHT_2)
		{
			if (getRandom(1000) < 350)
			{
				final L2Npc knight = addSpawn(ELITE_DRAGON_KNIGHT, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				knight.broadcastPacket(new NpcSay(knight.getObjectId(), 0, knight.getId(), NpcStringId.IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH));
				knight.setRunning();
				((L2Attackable) knight).addDamageHate(killer, 1, 99999);
				knight.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
			
			if (getRandom(1000) < 350)
			{
				final L2Npc warior = addSpawn(DRAGON_KNIGHT_WARRIOR, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warior.broadcastPacket(new NpcSay(warior.getObjectId(), 0, warior.getId(), NpcStringId.IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH));
				warior.setRunning();
				((L2Attackable) warior).addDamageHate(killer, 1, 99999);
				warior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new DragonKnights(DragonKnights.class.getSimpleName(), "ai");
	}
}