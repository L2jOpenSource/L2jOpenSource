package l2r.gameserver.scripts.ai.zone.StakatoNest;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class MaleSpikedStakato extends AbstractNpcAI
{
	private static final int FEMALE_SPIKED_STAKATO = 22620;
	private static final int MALE_SPIKED_STAKATO_2ND_FORM = 22622;
	
	public MaleSpikedStakato(int questId, String name, String descr)
	{
		super(name, descr);
		
		addKillId(FEMALE_SPIKED_STAKATO);
		addKillId(MALE_SPIKED_STAKATO_2ND_FORM);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2Npc couple = getCouple(npc);
		if ((couple != null) && (!couple.isDead()))
		{
			couple.doDie(couple);
			L2Attackable newForm = (L2Attackable) addSpawn(MALE_SPIKED_STAKATO_2ND_FORM, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 0L, true);
			newForm.setRunning();
			newForm.addDamageHate(killer, 1, 99999);
			newForm.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public L2Npc getCouple(L2Npc couple)
	{
		return ((L2MonsterInstance) couple).getLeader();
	}
	
	public static void main(String[] args)
	{
		new MaleSpikedStakato(-1, "MaleSpikedStakato", "ai");
	}
}