package ai.npc.Summons.MerchantGolem;

import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * Golem Trader AI.
 * @author Zoey76
 */
public class GolemTrader extends AbstractNpcAI
{
	// NPC
	private static final int GOLEM_TRADER = 13128;
	
	public GolemTrader()
	{
		super(GolemTrader.class.getSimpleName(), "ai/npc/Summons");
		addSpawnId(GOLEM_TRADER);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.scheduleDespawn(180000);
		return super.onSpawn(npc);
	}
}
